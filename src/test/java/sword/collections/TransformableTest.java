package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.SortUtils.equal;

abstract class TransformableTest<T, B extends TransformableBuilder<T>> extends TraversableTest<T, B> {

    @Test
    public void testToListWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().build();
            assertTrue(transformable.isEmpty());
            assertTrue(transformable.toList().isEmpty());
        });
    }

    @Test
    public void testToList() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().add(a).add(b).build();
            final List<T> list = transformable.toList();

            final Transformer<T> transformer = transformable.iterator();
            for (T value : list) {
                assertTrue(transformer.hasNext());
                assertSame(value, transformer.next());
            }
            assertFalse(transformer.hasNext());
        })));
    }

    @Test
    public void testToSetWhenEmpty() {
        withBuilderSupplier(supplier -> assertTrue(supplier.newBuilder().build().toSet().isEmpty()));
    }

    @Test
    public void testToSetForASingleElement() {
        withValue(a -> withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().add(a).build();
            final Set<T> set = transformable.toSet();
            assertEquals(1, set.size());
            assertEquals(a, set.valueAt(0));
        }));
    }

    @Test
    public void testToSetForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().add(a).add(b).add(c).build();
            final Set<T> set = transformable.toSet();
            int count = 0;
            for (T setValue : set) {
                boolean found = false;
                for (T transValue : transformable) {
                    if (equal(setValue, transValue)) {
                        count++;
                        found = true;
                    }
                }
                assertTrue(found);
            }

            assertEquals(count, transformable.size());
        }))));
    }

    @Test
    public void testIndexesWhenEmpty() {
        withBuilderSupplier(supplier -> assertTrue(supplier.newBuilder().build().indexes().isEmpty()));
    }

    @Test
    public void testIndexesForSingleValue() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Iterator<Integer> indexIterator = supplier.newBuilder().add(value).build().indexes().iterator();
            assertTrue(indexIterator.hasNext());
            assertEquals(0, indexIterator.next().intValue());
            assertFalse(indexIterator.hasNext());
        }));
    }

    @Test
    public void testIndexesForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().add(a).add(b).add(c).build();
            final Iterator<T> it = transformable.iterator();
            int length = 0;
            while (it.hasNext()) {
                length++;
                it.next();
            }

            final Iterator<Integer> indexIterator = transformable.indexes().iterator();
            for (int i = 0; i < length; i++) {
                assertTrue(indexIterator.hasNext());
                assertEquals(i, indexIterator.next().intValue());
            }
            assertFalse(indexIterator.hasNext());
        }))));
    }

    @Test
    public void testFilterWhenEmpty() {
        final Predicate<T> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().filter(f).iterator().hasNext());
        });
    }

    @Test
    public void testFilterForSingleElement() {
        withFilterFunc(f -> withValue(value -> withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().add(value).build();
            final Transformable<T> filtered = transformable.filter(f);

            if (f.apply(value)) {
                assertEquals(transformable, filtered);
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        })));
    }

    @Test
    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final Transformable<T> iterable = supplier.newBuilder().add(a).add(b).build();
            final Transformable<T> filtered = iterable.filter(f);

            final Transformer<T> tr = filtered.iterator();
            for (T value : iterable) {
                if (f.apply(value)) {
                    assertTrue(tr.hasNext());
                    assertSame(value, tr.next());
                }
            }
            assertFalse(tr.hasNext());
        }))));
    }

    @Test
    public void testFilterNotWhenEmpty() {
        final Predicate<T> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withBuilderSupplier(supplier-> {
            assertFalse(supplier.newBuilder().build().filterNot(f).iterator().hasNext());
        });
    }

    @Test
    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withValue(value -> withBuilderSupplier(supplier -> {
            final Transformable<T> collection = supplier.newBuilder().add(value).build();
            final Transformable<T> filtered = collection.filterNot(f);

            if (f.apply(value)) {
                assertFalse(filtered.iterator().hasNext());
            }
            else {
                assertEquals(collection, filtered);
            }
        })));
    }

    @Test
    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final Transformable<T> iterable = supplier.newBuilder().add(a).add(b).build();
            final Transformable<T> filtered = iterable.filterNot(f);

            final boolean aRemoved = f.apply(a);
            final boolean bRemoved = f.apply(b);

            if (aRemoved && bRemoved) {
                assertFalse(filtered.iterator().hasNext());
            }
            else if (aRemoved) {
                Iterator<T> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(b, iterator.next());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<T> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.next());
                assertFalse(iterator.hasNext());
            }
            else {
                assertEquals(iterable, filtered);
            }
        }))));
    }
}

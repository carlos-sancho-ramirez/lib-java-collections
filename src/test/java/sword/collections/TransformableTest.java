package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;

interface TransformableTest<T, B extends TransformableBuilder<T>> extends TraversableTest<T, B> {

    @Test
    default void testToListWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().build();
            assertTrue(transformable.isEmpty());
            assertTrue(transformable.toList().isEmpty());
        });
    }

    @Test
    default void testToList() {
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
    default void testToSetWhenEmpty() {
        withBuilderSupplier(supplier -> assertTrue(supplier.newBuilder().build().toSet().isEmpty()));
    }

    @Test
    default void testToSetForASingleElement() {
        withValue(a -> withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().add(a).build();
            final Set<T> set = transformable.toSet();
            assertEquals(1, set.size());
            assertEquals(a, set.valueAt(0));
        }));
    }

    @Test
    default void testToSetForMultipleElements() {
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
    default void testIndexesWhenEmpty() {
        withBuilderSupplier(supplier -> assertTrue(supplier.newBuilder().build().indexes().isEmpty()));
    }

    @Test
    default void testIndexesForSingleValue() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Iterator<Integer> indexIterator = supplier.newBuilder().add(value).build().indexes().iterator();
            assertTrue(indexIterator.hasNext());
            assertEquals(0, indexIterator.next().intValue());
            assertFalse(indexIterator.hasNext());
        }));
    }

    @Test
    default void testIndexesForMultipleValues() {
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
    default void testFilterWhenEmpty() {
        final Predicate<T> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().filter(f).iterator().hasNext());
        });
    }

    @Test
    default void testFilterForSingleElement() {
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
    default void testFilterForMultipleElements() {
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
    default void testFilterNotWhenEmpty() {
        final Predicate<T> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withBuilderSupplier(supplier-> {
            assertFalse(supplier.newBuilder().build().filterNot(f).iterator().hasNext());
        });
    }

    @Test
    default void testFilterNotForSingleElement() {
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
    default void testFilterNotForMultipleElements() {
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

    @Test
    default void testCountWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntValueMap<T> map = supplier.newBuilder().build().count();
            assertTrue(map.isEmpty());
        });
    }

    @Test
    default void testCountForSingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final IntValueMap<T> map = supplier.newBuilder().add(value).build().count();
            assertEquals(1, map.size());
            assertSame(value, map.keyAt(0));
            assertEquals(1, map.valueAt(0));
        }));
    }

    @Test
    default void testCountForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().add(a).add(b).add(c).build();
            final IntValueMap<T> map = transformable.count();

            final MutableIntValueMap<T> expected = MutableIntValueHashMap.empty();
            for (T value : transformable) {
                final int count = expected.get(value, 0);
                expected.put(value, count + 1);
            }

            assertEquals(expected.size(), map.size());
            for (T value : expected.keySet()) {
                assertEquals(expected.get(value), map.get(value));
            }
        }))));
    }

    @Test
    default void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Transformable<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final T first = set.valueAt(0);
            final T second = (size >= 2)? set.valueAt(1) : null;
            final T third = (size >= 3)? set.valueAt(2) : null;

            final Transformable<T> sliceA = set.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(first, sliceA.valueAt(0));

            final Transformable<T> sliceB = set.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(second, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final Transformable<T> sliceC = set.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(third, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final Transformable<T> sliceAB = set.slice(new ImmutableIntRange(0, 1));
            if (size >= 2) {
                assertEquals(2, sliceAB.size());
                assertSame(second, sliceAB.valueAt(1));
            }
            else {
                assertEquals(1, sliceAB.size());
            }
            assertEquals(first, sliceAB.valueAt(0));

            final Transformable<T> sliceBC = set.slice(new ImmutableIntRange(1, 2));
            assertEquals(size - 1, sliceBC.size());
            if (size == 2) {
                assertSame(second, sliceBC.valueAt(0));
            }
            else if (size == 3) {
                assertSame(second, sliceBC.valueAt(0));
                assertSame(third, sliceBC.valueAt(1));
            }

            final Transformable<T> sliceABC = set.slice(new ImmutableIntRange(0, 2));
            assertEquals(size, sliceABC.size());
            assertSame(first, sliceABC.valueAt(0));
            if (size >= 2) {
                assertSame(second, sliceABC.valueAt(1));
                if (size >= 3) {
                    assertSame(third, sliceABC.valueAt(2));
                }
            }

            final Transformable<T> sliceABCD = set.slice(new ImmutableIntRange(0, 3));
            assertEquals(size, sliceABCD.size());
            assertSame(first, sliceABCD.valueAt(0));
            if (size >= 2) {
                assertSame(second, sliceABCD.valueAt(1));
                if (size >= 3) {
                    assertSame(third, sliceABCD.valueAt(2));
                }
            }
        }))));
    }

    @Test
    default void testSkipWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().build();
            assertSame(transformable, transformable.skip(0));
            assertTrue(transformable.skip(1).isEmpty());
            assertTrue(transformable.skip(20).isEmpty());
        });
    }

    @Test
    default void testSkip() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = transformable.size();
            final T second = (size >= 2)? transformable.valueAt(1) : null;
            final T third = (size == 3)? transformable.valueAt(2) : null;

            assertSame(transformable, transformable.skip(0));

            final Transformable<T> skip1 = transformable.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertSame(second, skip1.valueAt(0));
                if (size == 3) {
                    assertSame(third, skip1.valueAt(1));
                }
            }

            final Transformable<T> skip2 = transformable.skip(2);
            if (size == 3) {
                assertSame(third, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertEquals(0, skip2.size());
            }

            assertEquals(0, transformable.skip(3).size());
            assertEquals(0, transformable.skip(4).size());
            assertEquals(0, transformable.skip(24).size());
        }))));
    }

    @Test
    default void testTakeWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().build();
            assertTrue(transformable.take(0).isEmpty());
            assertTrue(transformable.take(1).isEmpty());
            assertTrue(transformable.take(2).isEmpty());
            assertTrue(transformable.take(24).isEmpty());
        });
    }

    @Test
    default void testTake() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = transformable.size();
            final T first = transformable.valueAt(0);

            assertTrue(transformable.take(0).isEmpty());

            final Transformable<T> take1 = transformable.take(1);
            assertEquals(1, take1.size());
            assertSame(first, take1.valueAt(0));

            final Transformable<T> take2 = transformable.take(2);
            if (size == 1) {
                assertEquals(1, take2.size());
            }
            else {
                assertEquals(2, take2.size());
                assertSame(transformable.valueAt(1), take2.valueAt(1));
            }
            assertSame(first, take2.valueAt(0));

            final Transformable<T> take3 = transformable.take(3);
            assertEquals(size, take3.size());
            assertSame(first, take3.valueAt(0));
            if (size >= 2) {
                assertSame(transformable.valueAt(1), take3.valueAt(1));
                if (size == 3) {
                    assertSame(transformable.valueAt(2), take3.valueAt(2));
                }
            }

            final Transformable<T> take4 = transformable.take(4);
            assertEquals(size, take4.size());
            assertSame(first, take4.valueAt(0));
            if (size >= 2) {
                assertSame(transformable.valueAt(1), take4.valueAt(1));
                if (size == 3) {
                    assertSame(transformable.valueAt(2), take4.valueAt(2));
                }
            }
        }))));
    }

    @Test
    default void testSkipLastWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().build();
            assertSame(transformable, transformable.skipLast(0));
            assertTrue(transformable.skipLast(1).isEmpty());
            assertTrue(transformable.skipLast(2).isEmpty());
            assertTrue(transformable.skipLast(24).isEmpty());
        });
    }

    @Test
    default void testSkipLast() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Transformable<T> transformable = supplier.newBuilder().add(a).add(b).add(c).build();
            assertSame(transformable, transformable.skipLast(0));

            final int size = transformable.size();
            final T first = transformable.valueAt(0);
            final T second = (size >= 2)? transformable.valueAt(1) : null;

            final Transformable<T> transformable1 = transformable.skipLast(1);
            assertEquals(size - 1, transformable1.size());
            if (size >= 2) {
                assertSame(first, transformable1.valueAt(0));
                if (size == 3) {
                    assertSame(second, transformable1.valueAt(1));
                }
            }

            final Transformable<T> transformable2 = transformable.skipLast(2);
            if (size < 3) {
                assertTrue(transformable2.isEmpty());
            }
            else {
                assertEquals(1, transformable2.size());
                assertSame(first, transformable2.valueAt(0));
            }

            assertTrue(transformable.skipLast(3).isEmpty());
            assertTrue(transformable.skipLast(4).isEmpty());
            assertTrue(transformable.skipLast(24).isEmpty());
        }))));
    }
}

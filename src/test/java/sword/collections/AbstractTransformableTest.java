package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractTransformableTest<T> extends AbstractTraversableTest<T> {

    abstract TransformableBuilder<T> newIterableBuilder();

    @Test
    public void testIndexesWhenEmpty() {
        assertTrue(newIterableBuilder().build().indexes().isEmpty());
    }

    @Test
    public void testIndexesForSingleValue() {
        withValue(value -> {
            final Iterator<Integer> indexIterator = newIterableBuilder().add(value).build().indexes().iterator();
            assertTrue(indexIterator.hasNext());
            assertEquals(0, indexIterator.next().intValue());
            assertFalse(indexIterator.hasNext());
        });
    }

    @Test
    public void testIndexesForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> {
            final Transformable<T> transformable = newIterableBuilder().add(a).add(b).add(c).build();
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
        })));
    }

    @Test
    public void testFilterWhenEmpty() {
        final Predicate<T> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIterableBuilder().build().filter(f).iterator().hasNext());
    }

    @Test
    public void testFilterForSingleElement() {
        withFilterFunc(f -> withValue(value -> {
            final Transformable<T> transformable = newIterableBuilder().add(value).build();
            final Transformable<T> filtered = transformable.filter(f);

            if (f.apply(value)) {
                assertEquals(transformable, filtered);
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        }));
    }

    @Test
    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withValue(a -> withValue(b -> {
            final Transformable<T> iterable = newIterableBuilder().add(a).add(b).build();
            final Transformable<T> filtered = iterable.filter(f);

            final boolean aPassed = f.apply(a);
            final boolean bPassed = f.apply(b);

            if (aPassed && bPassed) {
                assertEquals(iterable, filtered);
            }
            else if (aPassed) {
                Iterator<T> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.next());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<T> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(b, iterator.next());
                assertFalse(iterator.hasNext());
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        })));
    }

    @Test
    public void testFilterNotWhenEmpty() {
        final Predicate<T> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIterableBuilder().build().filterNot(f).iterator().hasNext());
    }

    @Test
    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withValue(value -> {
            final Transformable<T> collection = newIterableBuilder().add(value).build();
            final Transformable<T> filtered = collection.filterNot(f);

            if (f.apply(value)) {
                assertFalse(filtered.iterator().hasNext());
            }
            else {
                assertEquals(collection, filtered);
            }
        }));
    }

    @Test
    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withValue(a -> withValue(b -> {
            final Transformable<T> iterable = newIterableBuilder().add(a).add(b).build();
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
        })));
    }
}

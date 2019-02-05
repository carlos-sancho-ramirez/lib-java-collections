package sword.collections;

import java.util.Iterator;

abstract class AbstractTransformableTest<T> extends AbstractIterableTest<T> {

    abstract TransformableBuilder<T> newIterableBuilder();
    abstract void assertEmptyCollection(Transformable<T> collection);
    abstract void assertNotChanged(Object expected, Object given);

    public void testIndexesWhenEmpty() {
        assertTrue(newIterableBuilder().build().indexes().isEmpty());
    }

    public void testIndexesForSingleValue() {
        withValue(value -> {
            final Iterator<Integer> indexIterator = newIterableBuilder().add(value).build().indexes().iterator();
            assertTrue(indexIterator.hasNext());
            assertEquals(0, indexIterator.next().intValue());
            assertFalse(indexIterator.hasNext());
        });
    }

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

    public void testFilterWhenEmpty() {
        final Predicate<T> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertEmptyCollection(newIterableBuilder().build().filter(f));
    }

    public void testFilterForSingleElement() {
        withFilterFunc(f -> withValue(value -> {
            final Transformable<T> transformable = newIterableBuilder().add(value).build();
            final Transformable<T> filtered = transformable.filter(f);

            if (f.apply(value)) {
                assertNotChanged(transformable, filtered);
            }
            else {
                assertEmptyCollection(filtered);
            }
        }));
    }

    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withValue(a -> withValue(b -> {
            final Transformable<T> iterable = newIterableBuilder().add(a).add(b).build();
            final Transformable<T> filtered = iterable.filter(f);

            final boolean aPassed = f.apply(a);
            final boolean bPassed = f.apply(b);

            if (aPassed && bPassed) {
                assertNotChanged(iterable, filtered);
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
                assertEmptyCollection(filtered);
            }
        })));
    }

    public void testFilterNotWhenEmpty() {
        final Predicate<T> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertEmptyCollection(newIterableBuilder().build().filterNot(f));
    }

    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withValue(value -> {
            final Transformable<T> collection = newIterableBuilder().add(value).build();
            final Transformable<T> filtered = collection.filterNot(f);

            if (f.apply(value)) {
                assertEmptyCollection(filtered);
            }
            else {
                assertNotChanged(collection, filtered);
            }
        }));
    }

    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withValue(a -> withValue(b -> {
            final Transformable<T> iterable = newIterableBuilder().add(a).add(b).build();
            final Transformable<T> filtered = iterable.filterNot(f);

            final boolean aRemoved = f.apply(a);
            final boolean bRemoved = f.apply(b);

            if (aRemoved && bRemoved) {
                assertEmptyCollection(filtered);
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
                assertNotChanged(iterable, filtered);
            }
        })));
    }
}

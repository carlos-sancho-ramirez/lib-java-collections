package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class AbstractIterableImmutableTest<T> extends AbstractIterableTest<T> {

    abstract <E> AbstractIterable<E> emptyCollection();
    abstract ImmutableCollectionBuilder<T> newIterableBuilder();
    abstract void withValue(Procedure<T> procedure);
    abstract void withFilterFunc(Procedure<Predicate<T>> procedure);
    abstract void withMapFunc(Procedure<Function<T, String>> procedure);

    public void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final IterableImmutableCollection<T> list = newIterableBuilder().build();
            assertSame(list, list.filter(f));
        });
    }

    public void testFilterForSingleElement() {
        withFilterFunc(f -> withValue(value -> {
            final IterableImmutableCollection<T> list = newIterableBuilder().add(value).build();
            final IterableCollection<T> filtered = list.filter(f);

            if (f.apply(value)) {
                assertSame(list, filtered);
            }
            else {
                assertSame(emptyCollection(), filtered);
            }
        }));
    }

    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withValue(a -> withValue(b -> {
            final AbstractImmutableIterable<T> iterable = (AbstractImmutableIterable<T>) newIterableBuilder().add(a).add(b).build();
            final AbstractIterable<T> filtered = (AbstractImmutableIterable<T>) iterable.filter(f);

            final boolean aPassed = f.apply(a);
            final boolean bPassed = f.apply(b);

            if (aPassed && bPassed) {
                assertSame(iterable, filtered);
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
                assertSame(emptyCollection(), filtered);
            }
        })));
    }

    public void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final IterableImmutableCollection<T> list = newIterableBuilder().build();
            assertSame(list, list.filterNot(f));
        });
    }

    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withValue(value -> {
            final IterableImmutableCollection<T> collection = newIterableBuilder().add(value).build();
            final IterableImmutableCollection<T> filtered = collection.filterNot(f);

            if (f.apply(value)) {
                assertSame(emptyCollection(), filtered);
            }
            else {
                assertSame(collection, filtered);
            }
        }));
    }

    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withValue(a -> withValue(b -> {
            final AbstractImmutableIterable<T> iterable = (AbstractImmutableIterable<T>) newIterableBuilder().add(a).add(b).build();
            final AbstractImmutableIterable<T> filtered = (AbstractImmutableIterable<T>) iterable.filterNot(f);

            final boolean aRemoved = f.apply(a);
            final boolean bRemoved = f.apply(b);

            if (aRemoved && bRemoved) {
                assertSame(emptyCollection(), filtered);
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
                assertSame(iterable, filtered);
            }
        })));
    }

    public void testMapWhenEmpty() {
        withMapFunc(f -> {
            final IterableImmutableCollection<T> collection = newIterableBuilder().build();
            assertSame(emptyCollection(), collection.map(f));
        });
    }

    public void testMapForSingleElement() {
        withMapFunc(f -> withValue(value -> {
            final IterableImmutableCollection<T> collection = newIterableBuilder().add(value).build();
            final IterableImmutableCollection<String> mapped = collection.map(f);
            final Iterator<String> iterator = mapped.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next());
            assertFalse(iterator.hasNext());
        }));
    }

    public void testMapForMultipleElements() {
        withMapFunc(f -> withValue(a -> withValue(b -> {
            final IterableImmutableCollection<T> collection = newIterableBuilder().add(a).add(b).build();
            final IterableImmutableCollection<String> mapped = collection.map(f);
            final Iterator<String> iterator = mapped.iterator();

            final String mappedA = f.apply(a);
            final String mappedB = f.apply(b);

            assertTrue(iterator.hasNext());
            final boolean sameMappedValue = equal(mappedA, mappedB);
            final String first = iterator.next();

            if (sameMappedValue) {
                assertEquals(mappedA, first);
            }
            else if (equal(a, first)) {
                assertTrue(iterator.hasNext());
                assertEquals(b, iterator.hasNext());
            }
            else if (equal(b, first)) {
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.hasNext());
            }
            else {
                fail("Expected either " + mappedA + " or " + mappedB + " but found " + first);
            }

            assertFalse(iterator.hasNext());
        })));
    }
}

package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class AbstractImmutableIntIterableTest extends AbstractIntIterableTest {

    abstract AbstractIntIterable emptyCollection();
    abstract AbstractIterable<String> mapTargetEmptyCollection();

    abstract ImmutableIntCollectionBuilder newIntBuilder();
    abstract void withItem(IntProcedure procedure);
    abstract void withFilterFunc(Procedure<IntPredicate> procedure);
    abstract void withMapFunc(Procedure<IntFunction<String>> procedure);

    public void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final IterableImmutableIntCollection list = newIntBuilder().build();
            assertSame(list, list.filter(f));
        });
    }

    public void testFilterForSingleElement() {
        withFilterFunc(f -> withItem(value -> {
            final IterableImmutableIntCollection list = newIntBuilder().add(value).build();
            final IterableImmutableIntCollection filtered = list.filter(f);

            if (f.apply(value)) {
                assertSame(list, filtered);
            }
            else {
                assertSame(emptyCollection(), filtered);
            }
        }));
    }

    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withItem(a -> withItem(b -> {
            final AbstractImmutableIntIterable iterable = (AbstractImmutableIntIterable) newIntBuilder().add(a).add(b).build();
            final AbstractImmutableIntIterable filtered = (AbstractImmutableIntIterable) iterable.filter(f);

            final boolean aPassed = f.apply(a);
            final boolean bPassed = f.apply(b);

            if (aPassed && bPassed) {
                assertSame(iterable, filtered);
            }
            else if (aPassed) {
                Iterator<Integer> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.next().intValue());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<Integer> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(b, iterator.next().intValue());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(emptyCollection(), filtered);
            }
        })));
    }

    public void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final IterableImmutableIntCollection list = newIntBuilder().build();
            assertSame(list, list.filterNot(f));
        });
    }

    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withItem(value -> {
            final IterableImmutableIntCollection collection = newIntBuilder().add(value).build();
            final IterableImmutableIntCollection filtered = collection.filterNot(f);

            if (f.apply(value)) {
                assertSame(emptyCollection(), filtered);
            }
            else {
                assertSame(collection, filtered);
            }
        }));
    }

    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withItem(a -> withItem(b -> {
            final AbstractImmutableIntIterable iterable = (AbstractImmutableIntIterable) newIntBuilder().add(a).add(b).build();
            final AbstractImmutableIntIterable filtered = (AbstractImmutableIntIterable) iterable.filterNot(f);

            final boolean aRemoved = f.apply(a);
            final boolean bRemoved = f.apply(b);

            if (aRemoved && bRemoved) {
                assertSame(emptyCollection(), filtered);
            }
            else if (aRemoved) {
                Iterator<Integer> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(b, iterator.next().intValue());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<Integer> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.next().intValue());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(iterable, filtered);
            }
        })));
    }

    public void testMapWhenEmpty() {
        withMapFunc(f -> {
            final IterableImmutableIntCollection collection = newIntBuilder().build();
            assertSame(mapTargetEmptyCollection(), collection.map(f));
        });
    }

    public void testMapForSingleElement() {
        withMapFunc(f -> withItem(value -> {
            final IterableImmutableIntCollection collection = newIntBuilder().add(value).build();
            final IterableImmutableCollection<String> mapped = collection.map(f);
            final Iterator<String> iterator = mapped.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next());
            assertFalse(iterator.hasNext());
        }));
    }

    public void testMapForMultipleElements() {
        withMapFunc(f -> withItem(a -> withItem(b -> {
            final IterableImmutableIntCollection collection = newIntBuilder().add(a).add(b).build();
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
            else if (equal(mappedA, first)) {
                assertTrue(iterator.hasNext());
                assertEquals(mappedB, iterator.next());
            }
            else if (equal(mappedB, first)) {
                assertTrue(iterator.hasNext());
                assertEquals(mappedA, iterator.next());
            }
            else {
                fail("Expected either " + mappedA + " or " + mappedB + " but found " + first);
            }

            assertFalse(iterator.hasNext());
        })));
    }
}

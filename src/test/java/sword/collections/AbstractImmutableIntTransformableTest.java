package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class AbstractImmutableIntTransformableTest extends AbstractIntTransformableTest {

    abstract AbstractIntIterable emptyCollection();
    abstract AbstractIterable<String> mapTargetEmptyCollection();

    abstract ImmutableIntCollectionBuilder newIntBuilder();
    abstract void withItem(IntProcedure procedure);
    abstract void withFilterFunc(Procedure<IntPredicate> procedure);
    abstract void withMapFunc(Procedure<IntFunction<String>> procedure);

    @Override
    void assertEmptyCollection(IntTransformable transformable) {
        assertSame(emptyCollection(), transformable);
    }

    @Override
    void assertNotChanged(Object expected, Object given) {
        assertSame(expected, given);
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

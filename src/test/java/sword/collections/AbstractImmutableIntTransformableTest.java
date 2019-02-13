package sword.collections;

import java.util.Iterator;

abstract class AbstractImmutableIntTransformableTest extends AbstractIntTransformableTest {

    abstract AbstractIntTraversable emptyCollection();

    abstract ImmutableIntCollectionBuilder newIntBuilder();
    abstract void withItem(IntProcedure procedure);
    abstract void withFilterFunc(Procedure<IntPredicate> procedure);
    abstract void withMapFunc(Procedure<IntFunction<String>> procedure);
    abstract void withMapToIntFunc(Procedure<IntToIntFunction> procedure);

    @Override
    void assertEmptyCollection(IntTransformable transformable) {
        assertSame(emptyCollection(), transformable);
    }

    @Override
    void assertNotChanged(Object expected, Object given) {
        assertSame(expected, given);
    }

    public void testMapWhenEmpty() {
        final IntFunction func = unused -> {
            throw new AssertionError("This function should not be called");
        };

        final ImmutableIntTraversable collection = newIntBuilder().build();
        assertSame(ImmutableList.empty(), collection.map(func));
    }

    public void testMapForSingleElement() {
        withMapFunc(f -> withItem(value -> {
            final ImmutableIntTraversable collection = newIntBuilder().add(value).build();
            final ImmutableTransformable<String> mapped = collection.map(f);
            final Iterator<String> iterator = mapped.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next());
            assertFalse(iterator.hasNext());
        }));
    }

    public void testMapForMultipleElements() {
        withMapFunc(f -> withItem(a -> withItem(b -> {
            final ImmutableIntTraversable collection = newIntBuilder().add(a).add(b).build();
            final ImmutableTransformable<String> mapped = collection.map(f);

            final Iterator<Integer> iterator = collection.iterator();
            final Iterator<String> mappedIterator = mapped.iterator();
            while (iterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(iterator.next()), mappedIterator.next());
            }

            assertFalse(mappedIterator.hasNext());
        })));
    }

    public void testMapToIntWhenEmpty() {
        final IntToIntFunction func = unused -> {
            throw new AssertionError("This function should not be called");
        };

        final ImmutableIntTraversable collection = newIntBuilder().build();
        assertSame(ImmutableIntList.empty(), collection.mapToInt(func));
    }

    public void testMapToIntForSingleElement() {
        withMapToIntFunc(f -> withItem(value -> {
            final Iterator<Integer> iterator = newIntBuilder().add(value).build().mapToInt(f).iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next().intValue());
            assertFalse(iterator.hasNext());
        }));
    }

    public void testMapToIntForMultipleElements() {
        withMapToIntFunc(f -> withItem(a -> withItem(b -> {
            final ImmutableIntTraversable collection = newIntBuilder().add(a).add(b).build();
            final ImmutableIntTraversable mapped = collection.mapToInt(f);

            final Iterator<Integer> iterator = collection.iterator();
            final Iterator<Integer> mappedIterator = mapped.iterator();
            while (iterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(iterator.next()), mappedIterator.next().intValue());
            }

            assertFalse(mappedIterator.hasNext());
        })));
    }
}

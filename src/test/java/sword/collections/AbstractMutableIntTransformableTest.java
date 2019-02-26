package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractMutableIntTransformableTest extends AbstractIntTransformableTest {

    abstract AbstractIntTraversable emptyCollection();

    abstract IntTransformableBuilder newIntBuilder();
    abstract void withItem(IntProcedure procedure);
    abstract void withFilterFunc(Procedure<IntPredicate> procedure);
    abstract void withMapFunc(Procedure<IntFunction<String>> procedure);
    abstract void withMapToIntFunc(Procedure<IntToIntFunction> procedure);

    @Override
    void assertEmptyCollection(IntTransformable transformable) {
        assertFalse(transformable.iterator().hasNext());
    }

    private void assertMapTargetEmptyCollection(Transformable<String> transformable) {
        assertFalse(transformable.iterator().hasNext());
    }

    @Override
    void assertNotChanged(Object expected, Object given) {
        assertEquals(expected, given);
    }

    @Test
    public void testMapWhenEmpty() {
        final IntFunction<String> func = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertMapTargetEmptyCollection(newIntBuilder().build().map(func));
    }

    @Test
    public void testMapForSingleElement() {
        withMapFunc(f -> withItem(value -> {
            final Iterator<String> iterator = newIntBuilder().add(value).build().map(f).iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next());
            assertFalse(iterator.hasNext());
        }));
    }

    @Test
    public void testMapForMultipleElements() {
        withMapFunc(f -> withItem(a -> withItem(b -> {
            final IntTransformable collection = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> iterator = collection.iterator();
            final Iterator<String> mappedIterator = collection.map(f).iterator();
            while (iterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(iterator.next()), mappedIterator.next());
            }

            assertFalse(mappedIterator.hasNext());
        })));
    }

    @Test
    public void testMapToIntWhenEmpty() {
        final IntToIntFunction func = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertEmptyCollection(newIntBuilder().build().mapToInt(func));
    }

    @Test
    public void testMapToIntForSingleElement() {
        withMapToIntFunc(f -> withItem(value -> {
            final Iterator<Integer> iterator = newIntBuilder().add(value).build().mapToInt(f).iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next().intValue());
            assertFalse(iterator.hasNext());
        }));
    }

    @Test
    public void testMapToIntForMultipleElements() {
        withMapToIntFunc(f -> withItem(a -> withItem(b -> {
            final IntTransformable collection = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> iterator = collection.iterator();
            final Iterator<Integer> mappedIterator = collection.mapToInt(f).iterator();
            while (iterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(iterator.next()), mappedIterator.next().intValue());
            }

            assertFalse(mappedIterator.hasNext());
        })));
    }
}

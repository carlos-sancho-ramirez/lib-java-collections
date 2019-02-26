package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractImmutableTransformableTest<T> extends AbstractTransformableTest<T> {

    abstract <E> AbstractTraversable<E> emptyCollection();
    abstract ImmutableTransformableBuilder<T> newIterableBuilder();
    abstract void withValue(Procedure<T> procedure);
    abstract void withFilterFunc(Procedure<Predicate<T>> procedure);
    abstract void withMapFunc(Procedure<Function<T, String>> procedure);
    abstract void withMapToIntFunc(Procedure<IntResultFunction<T>> procedure);

    @Override
    void assertEmptyCollection(Transformable<T> collection) {
        assertSame(emptyCollection(), collection);
    }

    @Override
    void assertNotChanged(Object expected, Object given) {
        assertSame(expected, given);
    }

    @Test
    public void testMapWhenEmpty() {
        withMapFunc(f -> {
            assertFalse(newIterableBuilder().build().map(f).iterator().hasNext());
        });
    }

    @Test
    public void testMapForSingleElement() {
        withMapFunc(f -> withValue(value -> {
            final ImmutableTransformable<T> collection = newIterableBuilder().add(value).build();
            final ImmutableTransformable<String> mapped = collection.map(f);
            final Iterator<String> iterator = mapped.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next());
            assertFalse(iterator.hasNext());
        }));
    }

    @Test
    public void testMapForMultipleElements() {
        withMapFunc(f -> withValue(a -> withValue(b -> {
            final ImmutableTransformable<T> collection = newIterableBuilder().add(a).add(b).build();
            final ImmutableTransformable<String> mapped = collection.map(f);

            final Iterator<T> collectionIterator = collection.iterator();
            final Iterator<String> mappedIterator = mapped.iterator();
            while (collectionIterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(collectionIterator.next()), mappedIterator.next());
            }

            assertFalse(mappedIterator.hasNext());
        })));
    }

    @Test
    public void testMapToIntWhenEmpty() {
        withMapToIntFunc(f -> {
            assertFalse(newIterableBuilder().build().mapToInt(f).iterator().hasNext());
        });
    }

    @Test
    public void testMapToIntForSingleElement() {
        withMapToIntFunc(f -> withValue(value -> {
            final ImmutableTransformable<T> collection = newIterableBuilder().add(value).build();
            final ImmutableIntTraversable mapped = collection.mapToInt(f);
            final Iterator<Integer> iterator = mapped.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), (int) iterator.next());
            assertFalse(iterator.hasNext());
        }));
    }

    @Test
    public void testMapToIntForMultipleElements() {
        withMapToIntFunc(f -> withValue(a -> withValue(b -> {
            final ImmutableTransformable<T> collection = newIterableBuilder().add(a).add(b).build();
            final ImmutableIntTraversable mapped = collection.mapToInt(f);

            final Iterator<T> collectionIterator = collection.iterator();
            final Iterator<Integer> mappedIterator = mapped.iterator();
            while (collectionIterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(collectionIterator.next()), (int) mappedIterator.next());
            }
            assertFalse(mappedIterator.hasNext());
        })));
    }
}

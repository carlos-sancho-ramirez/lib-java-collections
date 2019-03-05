package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;

public interface ImmutableTransformableTest<T> {

    void withTransformableBuilderSupplier(Procedure<BuilderSupplier<T, ImmutableTransformableBuilder<T>>> procedure);
    void withValue(Procedure<T> procedure);
    void withMapFunc(Procedure<Function<T, String>> procedure);
    void withMapToIntFunc(Procedure<IntResultFunction<T>> procedure);

    @Test
    default void testFilterWhenEmpty() {
        final Predicate<T> func = v -> {
            throw new AssertionError("Should not be called for empty collections");
        };

        withTransformableBuilderSupplier(supplier -> {
            final ImmutableTransformable<T> transformable = supplier.newBuilder().build();
            assertSame(transformable, transformable.filter(func));
            assertTrue(transformable.isEmpty());
        });
    }

    @Test
    default void testFilterNotWhenEmpty() {
        final Predicate<T> func = v -> {
            throw new AssertionError("Should not be called for empty collections");
        };

        withTransformableBuilderSupplier(supplier -> {
            final ImmutableTransformable<T> transformable = supplier.newBuilder().build();
            assertSame(transformable, transformable.filterNot(func));
            assertTrue(transformable.isEmpty());
        });
    }

    @Test
    default void testSameInstanceWhenFilteringAllValues() {
        withValue(a -> withValue(b -> withTransformableBuilderSupplier(supplier -> {
            final ImmutableTransformable<T> transformable = supplier.newBuilder().add(a).add(b).build();
            final Predicate<T> predicate = value -> equal(a, value) || equal(b, value);
            assertSame(transformable, transformable.filter(predicate));
        })));
    }

    @Test
    default void testSameInstanceWhenNonFilteringAnyValue() {
        withValue(a -> withValue(b -> withTransformableBuilderSupplier(supplier -> {
            final ImmutableTransformable<T> transformable = supplier.newBuilder().add(a).add(b).build();
            final Predicate<T> predicate = value -> !equal(a, value) && !equal(b, value);
            assertSame(transformable, transformable.filterNot(predicate));
        })));
    }

    @Test
    default void testMapWhenEmpty() {
        withMapFunc(f -> withTransformableBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().map(f).iterator().hasNext());
        }));
    }

    @Test
    default void testMapForSingleElement() {
        withMapFunc(f -> withValue(value -> withTransformableBuilderSupplier(supplier -> {
            final ImmutableTransformable<T> collection = supplier.newBuilder().add(value).build();
            final ImmutableTransformable<String> mapped = collection.map(f);

            final Iterator<String> iterator = mapped.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next());
            assertFalse(iterator.hasNext());
        })));
    }

    @Test
    default void testMapForMultipleElements() {
        withMapFunc(f -> withValue(a -> withValue(b -> withTransformableBuilderSupplier(supplier -> {
            final ImmutableTransformable<T> collection = supplier.newBuilder().add(a).add(b).build();
            final ImmutableTransformable<String> mapped = collection.map(f);

            final Iterator<T> collectionIterator = collection.iterator();
            final Iterator<String> mappedIterator = mapped.iterator();
            while (collectionIterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(collectionIterator.next()), mappedIterator.next());
            }

            assertFalse(mappedIterator.hasNext());
        }))));
    }

    @Test
    default void testMapToIntWhenEmpty() {
        withMapToIntFunc(f -> withTransformableBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().mapToInt(f).iterator().hasNext());
        }));
    }

    @Test
    default void testMapToIntForSingleElement() {
        withMapToIntFunc(f -> withValue(value -> withTransformableBuilderSupplier(supplier -> {
            final ImmutableTransformable<T> collection = supplier.newBuilder().add(value).build();
            final ImmutableIntTraversable mapped = collection.mapToInt(f);

            final Iterator<Integer> iterator = mapped.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), (int) iterator.next());
            assertFalse(iterator.hasNext());
        })));
    }

    @Test
    default void testMapToIntForMultipleElements() {
        withMapToIntFunc(f -> withValue(a -> withValue(b -> withTransformableBuilderSupplier(supplier -> {
            final ImmutableTransformable<T> collection = supplier.newBuilder().add(a).add(b).build();
            final ImmutableIntTraversable mapped = collection.mapToInt(f);

            final Iterator<T> collectionIterator = collection.iterator();
            final Iterator<Integer> mappedIterator = mapped.iterator();
            while (collectionIterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(collectionIterator.next()), (int) mappedIterator.next());
            }
            assertFalse(mappedIterator.hasNext());
        }))));
    }
}

package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.SortUtils.equal;

public interface ImmutableIntTransformableTest<C extends ImmutableIntTransformable> {

    void withTransformableBuilderSupplier(Procedure<IntBuilderSupplier<C, ImmutableIntTransformableBuilder<C>>> procedure);
    void withValue(IntProcedure procedure);
    void withMapFunc(Procedure<IntFunction<String>> procedure);
    void withMapToIntFunc(Procedure<IntToIntFunction> procedure);

    @Test
    default void testFilterWhenEmpty() {
        final IntPredicate func = v -> {
            throw new AssertionError("Should not be called for empty collections");
        };

        withTransformableBuilderSupplier(supplier -> {
            final ImmutableIntTransformable transformable = supplier.newBuilder().build();
            assertSame(transformable, transformable.filter(func));
            assertTrue(transformable.isEmpty());
        });
    }

    @Test
    default void testFilterNotWhenEmpty() {
        final IntPredicate func = v -> {
            throw new AssertionError("Should not be called for empty collections");
        };

        withTransformableBuilderSupplier(supplier -> {
            final ImmutableIntTransformable transformable = supplier.newBuilder().build();
            assertSame(transformable, transformable.filterNot(func));
            assertTrue(transformable.isEmpty());
        });
    }

    @Test
    default void testSameInstanceWhenFilteringAllValues() {
        withValue(a -> withValue(b -> withTransformableBuilderSupplier(supplier -> {
            final ImmutableIntTransformable transformable = supplier.newBuilder().add(a).add(b).build();
            final IntPredicate predicate = value -> a == value || b == value;
            assertSame(transformable, transformable.filter(predicate));
        })));
    }

    @Test
    default void testSameInstanceWhenNonFilteringAnyValue() {
        withValue(a -> withValue(b -> withTransformableBuilderSupplier(supplier -> {
            final ImmutableIntTransformable transformable = supplier.newBuilder().add(a).add(b).build();
            final IntPredicate predicate = value -> a != value && b != value;
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
            final ImmutableIntTransformable collection = supplier.newBuilder().add(value).build();
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
            final ImmutableIntTransformable collection = supplier.newBuilder().add(a).add(b).build();
            final ImmutableTransformable<String> mapped = collection.map(f);

            final IntTransformer collectionIterator = collection.iterator();
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
            final ImmutableIntTransformable collection = supplier.newBuilder().add(value).build();
            final ImmutableIntTransformable mapped = collection.mapToInt(f);

            final IntTransformer iterator = mapped.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), (int) iterator.next());
            assertFalse(iterator.hasNext());
        })));
    }

    @Test
    default void testMapToIntForMultipleElements() {
        withMapToIntFunc(f -> withValue(a -> withValue(b -> withTransformableBuilderSupplier(supplier -> {
            final ImmutableIntTransformable collection = supplier.newBuilder().add(a).add(b).build();
            final ImmutableIntTransformable mapped = collection.mapToInt(f);

            final IntTransformer transformer = collection.iterator();
            final Iterator<Integer> mappedIterator = mapped.iterator();
            while (transformer.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(transformer.next()), (int) mappedIterator.next());
            }
            assertFalse(mappedIterator.hasNext());
        }))));
    }
}
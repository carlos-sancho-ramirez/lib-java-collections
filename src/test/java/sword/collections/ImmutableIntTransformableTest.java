package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface ImmutableIntTransformableTest<B extends ImmutableIntTransformableBuilder> extends IntTransformableTest<B> {

    void withBuilderSupplier(Procedure<IntBuilderSupplier<B>> procedure);
    void withValue(IntProcedure procedure);
    void withMapFunc(Procedure<IntFunction<String>> procedure);
    void withMapToIntFunc(Procedure<IntToIntFunction> procedure);

    @Test
    default void testFilterWhenEmptyOnImmutable() {
        final IntPredicate func = v -> {
            throw new AssertionError("Should not be called for empty collections");
        };

        withBuilderSupplier(supplier -> {
            final ImmutableIntTransformable transformable = supplier.newBuilder().build();
            assertSame(transformable, transformable.filter(func));
            assertTrue(transformable.isEmpty());
        });
    }

    @Test
    default void testFilterNotWhenEmptyOnImmutable() {
        final IntPredicate func = v -> {
            throw new AssertionError("Should not be called for empty collections");
        };

        withBuilderSupplier(supplier -> {
            final ImmutableIntTransformable transformable = supplier.newBuilder().build();
            assertSame(transformable, transformable.filterNot(func));
            assertTrue(transformable.isEmpty());
        });
    }

    @Test
    default void testSameInstanceWhenFilteringAllValues() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final ImmutableIntTransformable transformable = supplier.newBuilder().add(a).add(b).build();
            final IntPredicate predicate = value -> a == value || b == value;
            assertSame(transformable, transformable.filter(predicate));
        })));
    }

    @Test
    default void testSameInstanceWhenNonFilteringAnyValue() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final ImmutableIntTransformable transformable = supplier.newBuilder().add(a).add(b).build();
            final IntPredicate predicate = value -> a != value && b != value;
            assertSame(transformable, transformable.filterNot(predicate));
        })));
    }

    @Test
    default void testMapWhenEmptyOnImmutable() {
        withMapFunc(f -> withBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().map(f).iterator().hasNext());
        }));
    }

    @Test
    default void testMapForSingleElementOnImmutable() {
        withMapFunc(f -> withValue(value -> withBuilderSupplier(supplier -> {
            final ImmutableIntTransformable collection = supplier.newBuilder().add(value).build();
            final ImmutableTransformable<String> mapped = collection.map(f);

            final Iterator<String> iterator = mapped.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next());
            assertFalse(iterator.hasNext());
        })));
    }

    @Test
    default void testMapForMultipleElementsOnImmutable() {
        withMapFunc(f -> withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
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
    default void testMapToIntWhenEmptyOnImmutable() {
        withMapToIntFunc(f -> withBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().mapToInt(f).iterator().hasNext());
        }));
    }

    @Test
    default void testMapToIntForSingleElementOnImmutable() {
        withMapToIntFunc(f -> withValue(value -> withBuilderSupplier(supplier -> {
            final ImmutableIntTransformable collection = supplier.newBuilder().add(value).build();
            final ImmutableIntTransformable mapped = collection.mapToInt(f);

            final IntTransformer iterator = mapped.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), (int) iterator.next());
            assertFalse(iterator.hasNext());
        })));
    }

    @Test
    default void testMapToIntForMultipleElementsOnImmutable() {
        withMapToIntFunc(f -> withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
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

    @Test
    default void testRemoveAtForSingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final ImmutableIntTransformable collection = supplier.newBuilder().add(value).build();
            assertTrue(collection.removeAt(0).isEmpty());
        }));
    }

    @Test
    default void testRemoveAtForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableIntTransformable collection = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = collection.size();
            for (int index = 0; index < size; index++) {
                final IntTransformer origTransformer = collection.iterator();
                final IntTransformer removedTransformer = collection.removeAt(index).iterator();
                for (int i = 0; i < size; i++) {
                    final int value = origTransformer.next();
                    if (i != index) {
                        assertEquals(value, removedTransformer.next().intValue());
                    }
                }
                assertFalse(removedTransformer.hasNext());
            }
        }))));
    }
}

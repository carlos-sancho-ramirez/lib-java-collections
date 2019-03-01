package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface MutableTraversableTest<T> {

    void withTraversableBuilderSupplier(Procedure<BuilderSupplier<T, MutableTraversableBuilder<T>>> procedure);
    void withValue(Procedure<T> procedure);

    @Test
    default void testClearWhenEmpty() {
        withTraversableBuilderSupplier(supplier -> {
            final MutableTraversable<T> traversable = supplier.newBuilder().build();
            assertFalse(traversable.clear());
            assertTrue(traversable.isEmpty());
        });
    }

    @Test
    default void testClearForSingleItem() {
        withValue(value -> withTraversableBuilderSupplier(supplier -> {
            final MutableTraversable<T> traversable = supplier.newBuilder().add(value).build();
            assertTrue(traversable.clear());
            assertTrue(traversable.isEmpty());
        }));
    }

    @Test
    default void testClearForMultipleItems() {
        withValue(a -> withValue(b -> withTraversableBuilderSupplier(supplier -> {
            final MutableTraversable<T> traversable = supplier.newBuilder().add(a).add(b).build();
            assertTrue(traversable.clear());
            assertTrue(traversable.isEmpty());
        })));
    }
}

package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface MutableIntTraversableTest<B extends MutableIntTraversableBuilder> {

    void withBuilderSupplier(Procedure<IntBuilderSupplier<B>> procedure);
    void withValue(IntProcedure procedure);

    @Test
    default void testClearWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final MutableIntTraversable traversable = supplier.newBuilder().build();
            assertFalse(traversable.clear());
            assertTrue(traversable.isEmpty());
        });
    }

    @Test
    default void testClearForSingleItem() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final MutableIntTraversable traversable = supplier.newBuilder().add(value).build();
            assertTrue(traversable.clear());
            assertTrue(traversable.isEmpty());
        }));
    }

    @Test
    default void testClearForMultipleItems() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final MutableIntTraversable traversable = supplier.newBuilder().add(a).add(b).build();
            assertTrue(traversable.clear());
            assertTrue(traversable.isEmpty());
        })));
    }
}

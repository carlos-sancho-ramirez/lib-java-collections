package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface MutableIntTraversableTest<C extends MutableIntTraversable> {

    void withIntTraversableBuilderSupplier(Procedure<IntBuilderSupplier<C, MutableIntTraversableBuilder<C>>> procedure);
    void withValue(IntProcedure procedure);

    @Test
    default void testClearWhenEmpty() {
        withIntTraversableBuilderSupplier(supplier -> {
            final C traversable = supplier.newBuilder().build();
            assertFalse(traversable.clear());
            assertTrue(traversable.isEmpty());
        });
    }

    @Test
    default void testClearForSingleItem() {
        withValue(value -> withIntTraversableBuilderSupplier(supplier -> {
            final C traversable = supplier.newBuilder().add(value).build();
            assertTrue(traversable.clear());
            assertTrue(traversable.isEmpty());
        }));
    }

    @Test
    default void testClearForMultipleItems() {
        withValue(a -> withValue(b -> withIntTraversableBuilderSupplier(supplier -> {
            final C traversable = supplier.newBuilder().add(a).add(b).build();
            assertTrue(traversable.clear());
            assertTrue(traversable.isEmpty());
        })));
    }
}

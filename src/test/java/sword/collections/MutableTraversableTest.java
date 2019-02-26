package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface MutableTraversableTest<T> {

    MutableTraversableBuilder<T> newTraversableBuilder();
    void withValue(Procedure<T> procedure);

    @Test
    default void testClearWhenEmpty() {
        final MutableTraversable<T> traversable = newTraversableBuilder().build();
        assertFalse(traversable.clear());
        assertTrue(traversable.isEmpty());
    }

    @Test
    default void testClearForSingleItem() {
        withValue(value -> {
            final MutableTraversable<T> traversable = newTraversableBuilder().add(value).build();
            assertTrue(traversable.clear());
            assertTrue(traversable.isEmpty());
        });
    }

    @Test
    default void testClearForMultipleItems() {
        withValue(a -> withValue(b -> {
            final MutableTraversable<T> traversable = newTraversableBuilder().add(a).add(b).build();
            assertTrue(traversable.clear());
            assertTrue(traversable.isEmpty());
        }));
    }
}

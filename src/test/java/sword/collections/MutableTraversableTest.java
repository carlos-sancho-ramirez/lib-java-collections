package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public interface MutableTraversableTest<T, B extends MutableTraversableBuilder<T>> {

    void withBuilderSupplier(Procedure<BuilderSupplier<T, B>> procedure);
    void withValue(Procedure<T> procedure);

    @Test
    default void testClearWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final MutableTraversable<T> traversable = supplier.newBuilder().build();
            assertFalse(traversable.clear());
            assertTrue(traversable.isEmpty());
        });
    }

    @Test
    default void testClearForSingleItem() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final MutableTraversable<T> traversable = supplier.newBuilder().add(value).build();
            assertTrue(traversable.clear());
            assertTrue(traversable.isEmpty());
        }));
    }

    @Test
    default void testClearForMultipleItems() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final MutableTraversable<T> traversable = supplier.newBuilder().add(a).add(b).build();
            assertTrue(traversable.clear());
            assertTrue(traversable.isEmpty());
        })));
    }

    @Test
    default void testPickLastWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final MutableTraversable<T> traversable = supplier.newBuilder().build();
            try {
                traversable.pickLast();
                fail();
            }
            catch (EmptyCollectionException e) {
                // This is the expected path
            }
        });
    }

    @Test
    default void testPickLastForSingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final MutableTraversable<T> traversable = supplier.newBuilder().add(value).build();
            assertSame(value, traversable.pickLast());
            assertTrue(traversable.isEmpty());
        }));
    }

    @Test
    default void testPickLastForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final MutableTraversable<T> traversable = supplier.newBuilder().add(a).add(b).build();
            final int initialSize = traversable.size();
            final T first = traversable.pickLast();
            if (first == a) {
                if (initialSize > 1) {
                    assertSame(b, traversable.pickLast());
                }
            }
            else {
                assertSame(b, first);
                if (initialSize > 1) {
                    assertSame(a, traversable.pickLast());
                }
            }
            assertTrue(traversable.isEmpty());
        })));
    }
}

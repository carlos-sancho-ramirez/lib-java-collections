package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    default void testRemoveAtForSingleItem() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final MutableIntTraversable traversable = supplier.newBuilder().add(value).build();
            assertEquals(1, traversable.size());

            traversable.removeAt(0);
            assertEquals(0, traversable.size());
        }));
    }

    @Test
    default void testRemoveAtForMultipleItems() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final MutableIntTraversable original = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = original.size();

            for (int i = 0; i < size; i++) {
                final MutableIntTraversable traversable = supplier.newBuilder().add(a).add(b).add(c).build();
                traversable.removeAt(i);

                final IntTraverser origTraverser = original.iterator();
                final IntTraverser traverser = traversable.iterator();
                for (int j = 0; j < size; j++) {
                    assertTrue(origTraverser.hasNext());
                    if (j == i) {
                        origTraverser.next();
                    }
                    else {
                        assertTrue(traverser.hasNext());
                        assertEquals(origTraverser.next(), traverser.next());
                    }
                }

                assertFalse(origTraverser.hasNext());
                assertFalse(traverser.hasNext());
            }
        }))));
    }
}

package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

abstract class SetTest<T, B extends Set.Builder<T>> extends TransformableTest<T, B> {

    @Test
    public void testAssignWhenEmpty() {
        final Function<T, String> func = key -> {
            throw new AssertionError("This function should not be called");
        };

        withBuilderSupplier(supplier -> {
            final Set<T> set = supplier.newBuilder().build();
            final Map<T, String> map = set.assign(func);
            assertFalse(map.iterator().hasNext());
        });
    }

    @Test
    public void testAssign() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> withMapFunc(func -> {
            final Set<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();

            final Map<T, String> map = set.assign(func);
            assertEquals(size, map.size());

            for (int i = 0; i < size; i++) {
                final T value = set.valueAt(i);
                assertEquals(value, map.keyAt(i));
                assertEquals(func.apply(value), map.valueAt(i));
            }
        })))));
    }
}

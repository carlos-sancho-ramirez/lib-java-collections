package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

abstract class SetTest<T, B extends Set.Builder<T>> extends TransformableTest<T, B> {

    @Test
    void testAssignWhenEmpty() {
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
    void testAssign() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> withMapFunc(func -> {
            final Set<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();

            final Map<T, String> map = set.assign(func);
            assertEquals(size, map.size());

            for (int i = 0; i < size; i++) {
                final T value = set.valueAt(i);
                assertSame(value, map.keyAt(i));
                assertEquals(func.apply(value), map.valueAt(i));
            }
        })))));
    }

    @Test
    void testAssignToIntWhenEmpty() {
        final IntResultFunction<T> func = key -> {
            throw new AssertionError("This function should not be called");
        };

        withBuilderSupplier(supplier -> {
            final Set<T> set = supplier.newBuilder().build();
            final IntValueMap<T> map = set.assignToInt(func);
            assertFalse(map.iterator().hasNext());
        });
    }

    @Test
    void testAssignToInt() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> withMapToIntFunc(func -> {
            final Set<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();

            final IntValueMap<T> map = set.assignToInt(func);
            assertEquals(size, map.size());

            for (int i = 0; i < size; i++) {
                final T value = set.valueAt(i);
                assertSame(value, map.keyAt(i));
                assertEquals(func.apply(value), map.valueAt(i));
            }
        })))));
    }
}

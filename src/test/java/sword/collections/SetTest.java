package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class SetTest<T, B extends Set.Builder<T>> implements TransformableTest<T, B> {

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

    @Test
    @Override
    public void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Set<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final T first = set.valueAt(0);
            final T second = (size >= 2)? set.valueAt(1) : null;
            final T third = (size >= 3)? set.valueAt(2) : null;

            final Set<T> sliceA = set.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(first, sliceA.valueAt(0));

            final Set<T> sliceB = set.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(second, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final Set<T> sliceC = set.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(third, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final Set<T> sliceAB = set.slice(new ImmutableIntRange(0, 1));
            if (size >= 2) {
                assertEquals(2, sliceAB.size());
                assertSame(second, sliceAB.valueAt(1));
            }
            else {
                assertEquals(1, sliceAB.size());
            }
            assertEquals(first, sliceAB.valueAt(0));

            final Set<T> sliceBC = set.slice(new ImmutableIntRange(1, 2));
            assertEquals(size - 1, sliceBC.size());
            if (size == 2) {
                assertSame(second, sliceBC.valueAt(0));
            }
            else if (size == 3) {
                assertSame(second, sliceBC.valueAt(0));
                assertSame(third, sliceBC.valueAt(1));
            }

            final Set<T> sliceABC = set.slice(new ImmutableIntRange(0, 2));
            assertEquals(size, sliceABC.size());
            assertSame(first, sliceABC.valueAt(0));
            if (size >= 2) {
                assertSame(second, sliceABC.valueAt(1));
                if (size >= 3) {
                    assertSame(third, sliceABC.valueAt(2));
                }
            }

            final Set<T> sliceABCD = set.slice(new ImmutableIntRange(0, 3));
            assertEquals(size, sliceABCD.size());
            assertSame(first, sliceABCD.valueAt(0));
            if (size >= 2) {
                assertSame(second, sliceABCD.valueAt(1));
                if (size >= 3) {
                    assertSame(third, sliceABCD.valueAt(2));
                }
            }
        }))));
    }

    @Test
    @Override
    public void testSkipWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final Set<T> set = supplier.newBuilder().build();
            assertSame(set, set.skip(0));
            assertTrue(set.skip(1).isEmpty());
            assertTrue(set.skip(20).isEmpty());
        });
    }

    @Test
    @Override
    public void testSkip() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Set<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final T second = (size >= 2)? set.valueAt(1) : null;
            final T third = (size == 3)? set.valueAt(2) : null;

            assertSame(set, set.skip(0));

            final Set<T> skip1 = set.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertSame(second, skip1.valueAt(0));
                if (size == 3) {
                    assertSame(third, skip1.valueAt(1));
                }
            }

            final Set<T> skip2 = set.skip(2);
            if (size == 3) {
                assertSame(third, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertEquals(0, skip2.size());
            }

            assertEquals(0, set.skip(3).size());
            assertEquals(0, set.skip(4).size());
            assertEquals(0, set.skip(24).size());
        }))));
    }

    @Test
    @Override
    public void testTakeWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final Set<T> set = supplier.newBuilder().build();
            assertTrue(set.take(0).isEmpty());
            assertTrue(set.take(1).isEmpty());
            assertTrue(set.take(2).isEmpty());
            assertTrue(set.take(24).isEmpty());
        });
    }

    @Test
    @Override
    public void testTake() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Set<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final T first = set.valueAt(0);

            assertTrue(set.take(0).isEmpty());

            final Set<T> take1 = set.take(1);
            assertEquals(1, take1.size());
            assertSame(first, take1.valueAt(0));

            final Set<T> take2 = set.take(2);
            if (size == 1) {
                assertEquals(1, take2.size());
            }
            else {
                assertEquals(2, take2.size());
                assertSame(set.valueAt(1), take2.valueAt(1));
            }
            assertSame(first, take2.valueAt(0));

            final Set<T> take3 = set.take(3);
            assertEquals(size, take3.size());
            assertSame(first, take3.valueAt(0));
            if (size >= 2) {
                assertSame(set.valueAt(1), take3.valueAt(1));
                if (size == 3) {
                    assertSame(set.valueAt(2), take3.valueAt(2));
                }
            }

            final Set<T> take4 = set.take(4);
            assertEquals(size, take4.size());
            assertSame(first, take4.valueAt(0));
            if (size >= 2) {
                assertSame(set.valueAt(1), take4.valueAt(1));
                if (size == 3) {
                    assertSame(set.valueAt(2), take4.valueAt(2));
                }
            }
        }))));
    }

    @Test
    @Override
    public void testSkipLastWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final Set<T> set = supplier.newBuilder().build();
            assertSame(set, set.skipLast(0));
            assertTrue(set.skipLast(1).isEmpty());
            assertTrue(set.skipLast(2).isEmpty());
            assertTrue(set.skipLast(24).isEmpty());
        });
    }

    @Test
    @Override
    public void testSkipLast() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Set<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            assertSame(set, set.skipLast(0));

            final int size = set.size();
            final T first = set.valueAt(0);
            final T second = (size >= 2)? set.valueAt(1) : null;

            final Set<T> set1 = set.skipLast(1);
            assertEquals(size - 1, set1.size());
            if (size >= 2) {
                assertSame(first, set1.valueAt(0));
                if (size == 3) {
                    assertSame(second, set1.valueAt(1));
                }
            }

            final Set<T> set2 = set.skipLast(2);
            if (size < 3) {
                assertTrue(set2.isEmpty());
            }
            else {
                assertEquals(1, set2.size());
                assertSame(first, set2.valueAt(0));
            }

            assertTrue(set.skipLast(3).isEmpty());
            assertTrue(set.skipLast(4).isEmpty());
            assertTrue(set.skipLast(24).isEmpty());
        }))));
    }
}

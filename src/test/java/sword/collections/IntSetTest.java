package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class IntSetTest<B extends IntSet.Builder> implements IntTransformableTest<B> {

    @Test
    void testSliceWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().build();
            assertTrue(set.slice(new ImmutableIntRange(0, 0)).isEmpty());
            assertTrue(set.slice(new ImmutableIntRange(1, 1)).isEmpty());
            assertTrue(set.slice(new ImmutableIntRange(0, 1)).isEmpty());
            assertTrue(set.slice(new ImmutableIntRange(1, 2)).isEmpty());
            assertTrue(set.slice(new ImmutableIntRange(0, 2)).isEmpty());
        });
    }

    @Test
    public void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final int first = set.valueAt(0);
            final int second = (size >= 2)? set.valueAt(1) : 0;
            final int third = (size >= 3)? set.valueAt(2) : 0;

            final IntSet sliceA = set.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertEquals(first, sliceA.valueAt(0));

            final IntSet sliceB = set.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertEquals(second, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final IntSet sliceC = set.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertEquals(third, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final IntSet sliceAB = set.slice(new ImmutableIntRange(0, 1));
            if (size >= 2) {
                assertEquals(2, sliceAB.size());
                assertEquals(second, sliceAB.valueAt(1));
            }
            else {
                assertEquals(1, sliceAB.size());
            }
            assertEquals(first, sliceAB.valueAt(0));

            final IntSet sliceBC = set.slice(new ImmutableIntRange(1, 2));
            assertEquals(size - 1, sliceBC.size());
            if (size == 2) {
                assertEquals(second, sliceBC.valueAt(0));
            }
            else if (size == 3) {
                assertEquals(second, sliceBC.valueAt(0));
                assertEquals(third, sliceBC.valueAt(1));
            }

            final IntSet sliceABC = set.slice(new ImmutableIntRange(0, 2));
            assertEquals(size, sliceABC.size());
            assertEquals(first, sliceABC.valueAt(0));
            if (size >= 2) {
                assertEquals(second, sliceABC.valueAt(1));
                if (size >= 3) {
                    assertEquals(third, sliceABC.valueAt(2));
                }
            }

            final IntSet sliceABCD = set.slice(new ImmutableIntRange(0, 3));
            assertEquals(size, sliceABCD.size());
            assertEquals(first, sliceABCD.valueAt(0));
            if (size >= 2) {
                assertEquals(second, sliceABCD.valueAt(1));
                if (size >= 3) {
                    assertEquals(third, sliceABCD.valueAt(2));
                }
            }
        }))));
    }

    @Test
    @Override
    public void testSkipWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().build();
            assertSame(set, set.skip(0));
            assertTrue(set.skip(1).isEmpty());
            assertTrue(set.skip(20).isEmpty());
        });
    }

    @Test
    @Override
    public void testSkip() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final int second = (size >= 2)? set.valueAt(1) : 0;
            final int third = (size == 3)? set.valueAt(2) : 0;

            assertSame(set, set.skip(0));

            final IntSet skip1 = set.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertEquals(second, skip1.valueAt(0));
                if (size == 3) {
                    assertEquals(third, skip1.valueAt(1));
                }
            }

            final IntSet skip2 = set.skip(2);
            if (size == 3) {
                assertEquals(third, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertTrue(skip2.isEmpty());
            }

            assertTrue(set.skip(3).isEmpty());
            assertTrue(set.skip(4).isEmpty());
            assertTrue(set.skip(24).isEmpty());
        }))));
    }

    @Test
    public void testTakeWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().build();
            assertTrue(set.take(0).isEmpty());
            assertTrue(set.take(1).isEmpty());
            assertTrue(set.take(2).isEmpty());
            assertTrue(set.take(24).isEmpty());
        });
    }

    @Test
    public void testTake() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final int first = set.valueAt(0);

            assertTrue(set.take(0).isEmpty());

            final IntSet take1 = set.take(1);
            assertEquals(1, take1.size());
            assertEquals(first, take1.valueAt(0));

            final IntSet take2 = set.take(2);
            if (size == 1) {
                assertEquals(1, take2.size());
            }
            else {
                assertEquals(2, take2.size());
                assertEquals(set.valueAt(1), take2.valueAt(1));
            }
            assertEquals(first, take2.valueAt(0));

            final IntSet take3 = set.take(3);
            assertEquals(size, take3.size());
            assertEquals(first, take3.valueAt(0));
            if (size >= 2) {
                assertEquals(set.valueAt(1), take3.valueAt(1));
                if (size == 3) {
                    assertEquals(set.valueAt(2), take3.valueAt(2));
                }
            }

            final IntSet take4 = set.take(4);
            assertEquals(size, take4.size());
            assertEquals(first, take4.valueAt(0));
            if (size >= 2) {
                assertEquals(set.valueAt(1), take4.valueAt(1));
                if (size == 3) {
                    assertEquals(set.valueAt(2), take4.valueAt(2));
                }
            }
        }))));
    }

    @Test
    public void testSkipLastWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().build();
            assertSame(set, set.skipLast(0));
            assertTrue(set.skipLast(1).isEmpty());
            assertTrue(set.skipLast(2).isEmpty());
            assertTrue(set.skipLast(24).isEmpty());
        });
    }

    @Test
    public void testSkipLast() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().add(a).add(b).add(c).build();
            assertSame(set, set.skipLast(0));

            final int size = set.size();
            final int first = set.valueAt(0);
            final int second = (size >= 2)? set.valueAt(1) : 0;

            final IntSet set1 = set.skipLast(1);
            assertEquals(size - 1, set1.size());
            if (size >= 2) {
                assertEquals(first, set1.valueAt(0));
                if (size == 3) {
                    assertEquals(second, set1.valueAt(1));
                }
            }

            final IntSet set2 = set.skipLast(2);
            if (size < 3) {
                assertTrue(set2.isEmpty());
            }
            else {
                assertEquals(1, set2.size());
                assertEquals(first, set2.valueAt(0));
            }

            assertTrue(set.skipLast(3).isEmpty());
            assertTrue(set.skipLast(4).isEmpty());
            assertTrue(set.skipLast(24).isEmpty());
        }))));
    }

    @Test
    void testMutate() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().add(a).add(b).build();
            final MutableIntSet mutated = set.mutate();
            assertNotSame(set, mutated);

            mutated.add(c);
            assertTrue(mutated.contains(a));
            assertTrue(mutated.contains(b));
            assertTrue(mutated.contains(c));

            if (a == c || b == c) {
                assertEquals(set.size(), mutated.size());
            }
            else {
                assertEquals(set.size() + 1, mutated.size());
                assertFalse(set.contains(c));
            }
        }))));
    }

    @Test
    void testAssignWhenEmpty() {
        final IntFunction<String> func = key -> {
            throw new AssertionError("This function should not be called");
        };

        withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().build();
            final IntKeyMap<String> map = set.assign(func);
            assertFalse(map.iterator().hasNext());
        });
    }

    @Test
    void testAssign() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> withMapFunc(func -> {
            final IntSet set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();

            final IntKeyMap<String> map = set.assign(func);
            assertEquals(size, map.size());

            for (int i = 0; i < size; i++) {
                final int value = set.valueAt(i);
                assertEquals(value, map.keyAt(i));
                assertEquals(func.apply(value), map.valueAt(i));
            }
        })))));
    }

    @Test
    void testAssignToIntWhenEmpty() {
        final IntToIntFunction func = key -> {
            throw new AssertionError("This function should not be called");
        };

        withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().build();
            final IntPairMap map = set.assignToInt(func);
            assertFalse(map.iterator().hasNext());
        });
    }

    @Test
    void testAssignToInt() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> withMapToIntFunc(func -> {
            final IntSet set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();

            final IntPairMap map = set.assignToInt(func);
            assertEquals(size, map.size());

            for (int i = 0; i < size; i++) {
                final int value = set.valueAt(i);
                assertEquals(value, map.keyAt(i));
                assertEquals(func.apply(value), map.valueAt(i));
            }
        })))));
    }

    @Test
    void testEqualSet() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().add(a).add(b).add(c).build();
            assertTrue(set.equalSet(set));

            final IntSet.Builder setBuilder = supplier.newBuilder();
            final IntTransformer it = set.iterator();
            it.next();
            while (it.hasNext()) {
                setBuilder.add(it.next());
            }
            final IntSet reducedSet = setBuilder.build();

            assertFalse(set.equalSet(reducedSet));
            assertFalse(reducedSet.equalSet(set));

            assertTrue(set.equalSet(set.mutate()));
            assertTrue(set.equalSet(set.toImmutable()));
        }))));
    }
}

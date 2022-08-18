package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class IntListTest<B extends IntListBuilder> implements IntTransformableTest<B> {

    @Test
    void testSliceWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntList list = supplier.newBuilder().build();
            assertTrue(list.slice(new ImmutableIntRange(0, 0)).isEmpty());
            assertTrue(list.slice(new ImmutableIntRange(1, 1)).isEmpty());
            assertTrue(list.slice(new ImmutableIntRange(0, 1)).isEmpty());
            assertTrue(list.slice(new ImmutableIntRange(1, 2)).isEmpty());
            assertTrue(list.slice(new ImmutableIntRange(0, 2)).isEmpty());
        });
    }

    @Test
    public void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntList list = supplier.newBuilder().add(a).add(b).add(c).build();

            final IntList sliceA = list.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertEquals(a, sliceA.valueAt(0));

            final IntList sliceB = list.slice(new ImmutableIntRange(1, 1));
            assertEquals(1, sliceB.size());
            assertEquals(b, sliceB.valueAt(0));

            final IntList sliceC = list.slice(new ImmutableIntRange(2, 2));
            assertEquals(1, sliceC.size());
            assertEquals(c, sliceC.valueAt(0));

            final IntList sliceAB = list.slice(new ImmutableIntRange(0, 1));
            assertEquals(2, sliceAB.size());
            assertEquals(a, sliceAB.valueAt(0));
            assertEquals(b, sliceAB.valueAt(1));

            final IntList sliceBC = list.slice(new ImmutableIntRange(1, 2));
            assertEquals(2, sliceBC.size());
            assertEquals(b, sliceBC.valueAt(0));
            assertEquals(c, sliceBC.valueAt(1));

            final IntList sliceABC = list.slice(new ImmutableIntRange(0, 2));
            assertEquals(3, sliceABC.size());
            assertEquals(a, sliceABC.valueAt(0));
            assertEquals(b, sliceABC.valueAt(1));
            assertEquals(c, sliceABC.valueAt(2));

            final IntList sliceABCD = list.slice(new ImmutableIntRange(0, 3));
            assertEquals(3, sliceABCD.size());
            assertEquals(a, sliceABCD.valueAt(0));
            assertEquals(b, sliceABCD.valueAt(1));
            assertEquals(c, sliceABCD.valueAt(2));
        }))));
    }

    @Test
    @Override
    public void testSkipWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntList list = supplier.newBuilder().build();
            assertSame(list, list.skip(0));
            assertTrue(list.skip(1).isEmpty());
            assertTrue(list.skip(20).isEmpty());
        });
    }

    @Test
    @Override
    public void testSkip() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntList list = supplier.newBuilder().add(a).add(b).add(c).build();

            assertSame(list, list.skip(0));
            assertEquals(supplier.newBuilder().add(b).add(c).build(), list.skip(1));
            assertEquals(supplier.newBuilder().add(c).build(), list.skip(2));

            assertTrue(list.skip(3).isEmpty());
            assertTrue(list.skip(4).isEmpty());
            assertTrue(list.skip(24).isEmpty());
        }))));
    }

    @Test
    void testToImmutableWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final ImmutableIntList immutableList = supplier.newBuilder().build().toImmutable();
            assertTrue(immutableList.isEmpty());
            assertSame(immutableList, immutableList.toImmutable());
        });
    }

    @Test
    void testToImmutableForASingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final ImmutableIntList immutableList = supplier.newBuilder().add(value).build().toImmutable();
            assertEquals(1, immutableList.size());
            assertEquals(value, immutableList.valueAt(0));
            assertSame(immutableList, immutableList.toImmutable());
        }));
    }

    @Test
    void testToImmutableForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final ImmutableIntList immutableList = supplier.newBuilder().add(a).add(b).build().toImmutable();
            assertEquals(2, immutableList.size());
            assertEquals(a, immutableList.valueAt(0));
            assertEquals(b, immutableList.valueAt(1));
            assertSame(immutableList, immutableList.toImmutable());
        })));
    }

    @Test
    void testMutateWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntList list = supplier.newBuilder().build();
            final MutableIntList mutableList = list.mutate();
            assertTrue(mutableList.isEmpty());
            assertTrue(list.isEmpty());

            withValue(value -> {
                mutableList.append(value);
                assertTrue(list.isEmpty());
            });
        });
    }

    @Test
    void testMutateForASingleElement() {
        withValue(a -> withBuilderSupplier(supplier -> {
            final IntList list = supplier.newBuilder().add(a).build();
            final MutableIntList mutableList = list.mutate();
            assertEquals(1, list.size());
            assertEquals(a, list.valueAt(0));
            assertEquals(1, list.size());
            assertEquals(a, mutableList.valueAt(0));

            withValue(value -> {
                mutableList.append(value);
                assertEquals(1, list.size());
                assertEquals(a, list.valueAt(0));
            });
        }));
    }

    @Test
    void testMutateForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final IntList list = supplier.newBuilder().add(a).add(b).build();
            final MutableIntList mutableList = list.mutate();
            assertEquals(2, list.size());
            assertEquals(a, list.valueAt(0));
            assertEquals(b, list.valueAt(1));
            assertEquals(2, list.size());
            assertEquals(a, mutableList.valueAt(0));
            assertEquals(b, mutableList.valueAt(1));

            withValue(value -> {
                mutableList.append(value);
                assertEquals(2, list.size());
                assertEquals(a, list.valueAt(0));
                assertEquals(b, list.valueAt(1));
            });
        })));
    }
}

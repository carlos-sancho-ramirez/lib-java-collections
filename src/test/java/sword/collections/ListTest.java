package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class ListTest<T, B extends ListBuilder<T>> implements TransformableTest<T, B> {

    @Test
    void testToImmutableWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final ImmutableList<T> immutableList = supplier.newBuilder().build().toImmutable();
            assertTrue(immutableList.isEmpty());
            assertSame(immutableList, immutableList.toImmutable());
        });
    }

    @Test
    void testToImmutableForASingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final ImmutableList<T> immutableList = supplier.newBuilder().add(value).build().toImmutable();
            assertEquals(1, immutableList.size());
            assertSame(value, immutableList.valueAt(0));
            assertSame(immutableList, immutableList.toImmutable());
        }));
    }

    @Test
    void testToImmutableForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final ImmutableList<T> immutableList = supplier.newBuilder().add(a).add(b).build().toImmutable();
            assertEquals(2, immutableList.size());
            assertSame(a, immutableList.valueAt(0));
            assertSame(b, immutableList.valueAt(1));
            assertSame(immutableList, immutableList.toImmutable());
        })));
    }

    @Test
    void testMutateWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final List<T> list = supplier.newBuilder().build();
            final MutableList<T> mutableList = list.mutate();
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
            final List<T> list = supplier.newBuilder().add(a).build();
            final MutableList<T> mutableList = list.mutate();
            assertEquals(1, list.size());
            assertSame(a, list.valueAt(0));
            assertEquals(1, list.size());
            assertSame(a, mutableList.valueAt(0));

            withValue(value -> {
                mutableList.append(value);
                assertEquals(1, list.size());
                assertSame(a, list.valueAt(0));
            });
        }));
    }

    @Test
    void testMutateForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final List<T> list = supplier.newBuilder().add(a).add(b).build();
            final MutableList<T> mutableList = list.mutate();
            assertEquals(2, list.size());
            assertSame(a, list.valueAt(0));
            assertSame(b, list.valueAt(1));
            assertEquals(2, list.size());
            assertSame(a, mutableList.valueAt(0));
            assertSame(b, mutableList.valueAt(1));

            withValue(value -> {
                mutableList.append(value);
                assertEquals(2, list.size());
                assertSame(a, list.valueAt(0));
                assertSame(b, list.valueAt(1));
            });
        })));
    }

    @Test
    @Override
    public void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final List<T> list = supplier.newBuilder().append(a).append(b).append(c).build();

            final List<T> sliceA = list.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(a, sliceA.valueAt(0));

            final List<T> sliceB = list.slice(new ImmutableIntRange(1, 1));
            assertEquals(1, sliceB.size());
            assertSame(b, sliceB.valueAt(0));

            final List<T> sliceC = list.slice(new ImmutableIntRange(2, 2));
            assertEquals(1, sliceC.size());
            assertSame(c, sliceC.valueAt(0));

            final List<T> sliceAB = list.slice(new ImmutableIntRange(0, 1));
            assertEquals(2, sliceAB.size());
            assertSame(a, sliceAB.valueAt(0));
            assertSame(b, sliceAB.valueAt(1));

            final List<T> sliceBC = list.slice(new ImmutableIntRange(1, 2));
            assertEquals(2, sliceBC.size());
            assertSame(b, sliceBC.valueAt(0));
            assertSame(c, sliceBC.valueAt(1));

            final List<T> sliceABC = list.slice(new ImmutableIntRange(0, 2));
            assertEquals(3, sliceABC.size());
            assertSame(a, sliceABC.valueAt(0));
            assertSame(b, sliceABC.valueAt(1));
            assertSame(c, sliceABC.valueAt(2));

            final List<T> sliceABCD = list.slice(new ImmutableIntRange(0, 3));
            assertEquals(3, sliceABC.size());
            assertSame(a, sliceABCD.valueAt(0));
            assertSame(b, sliceABCD.valueAt(1));
            assertSame(c, sliceABCD.valueAt(2));
        }))));
    }

    @Test
    void testSkip() {
        withFilterFunc(f -> withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final List<T> list = supplier.newBuilder().add(a).add(b).add(c).build();

            assertSame(list, list.skip(0));
            assertEquals(supplier.newBuilder().add(b).add(c).build(), list.skip(1));
            assertEquals(supplier.newBuilder().add(c).build(), list.skip(2));

            assertTrue(list.skip(3).isEmpty());
            assertTrue(list.skip(4).isEmpty());
            assertTrue(list.skip(24).isEmpty());
        })))));
    }
}

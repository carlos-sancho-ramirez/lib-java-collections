package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ImmutableBitSetImplTest extends ImmutableIntSetTest {

    private static final int[] INT_VALUES = {
            0, 1, 2, 3, 31, 32, 33, 127, 128
    };

    @Override
    public void withValue(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    @Override
    public ImmutableBitSetImpl.Builder newIntBuilder() {
        return new ImmutableBitSetImpl.Builder();
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntSet.Builder>> procedure) {
        procedure.apply(ImmutableBitSetImpl.Builder::new);
    }

    @Test
    @Override
    void testSliceWhenEmpty() {
        final ImmutableBitSetImpl set = newIntBuilder().build();
        assertSame(set, set.slice(new ImmutableIntRange(0, 0)));
        assertSame(set, set.slice(new ImmutableIntRange(1, 1)));
        assertSame(set, set.slice(new ImmutableIntRange(2, 2)));
        assertSame(set, set.slice(new ImmutableIntRange(0, 1)));
        assertSame(set, set.slice(new ImmutableIntRange(1, 2)));
        assertSame(set, set.slice(new ImmutableIntRange(0, 2)));
        assertSame(set, set.slice(new ImmutableIntRange(0, 67)));
    }

    @Test
    @Override
    void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableBitSetImpl set = newIntBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final int first = set.valueAt(0);
            final int second = (size >= 2)? set.valueAt(1) : 0;
            final int third = (size >= 3)? set.valueAt(2) : 0;

            final ImmutableBitSetImpl sliceA = set.slice(new ImmutableIntRange(0, 0));
            if (size == 1) {
                assertSame(set, sliceA);
            }
            else {
                assertEquals(1, sliceA.size());
                assertEquals(first, sliceA.valueAt(0));
            }

            final ImmutableBitSetImpl sliceB = set.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertEquals(second, sliceB.valueAt(0));
            }
            else {
                assertTrue(sliceB.isEmpty());
            }

            final ImmutableBitSetImpl sliceC = set.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertEquals(third, sliceC.valueAt(0));
            }
            else {
                assertTrue(sliceC.isEmpty());
            }

            final ImmutableBitSetImpl sliceAB = set.slice(new ImmutableIntRange(0, 1));
            if (size == 1) {
                assertEquals(1, sliceAB.size());
            }
            else if (size == 2) {
                assertSame(set, sliceAB);
            }
            else {
                assertEquals(2, sliceAB.size());
                assertEquals(second, sliceAB.valueAt(1));
            }
            assertEquals(first, sliceAB.valueAt(0));

            final ImmutableBitSetImpl sliceBC = set.slice(new ImmutableIntRange(1, 2));
            if (size == 1) {
                assertTrue(sliceBC.isEmpty());
            }
            else if (size == 2) {
                assertEquals(1, sliceBC.size());
                assertEquals(second, sliceBC.valueAt(0));
            }
            else {
                assertEquals(2, sliceBC.size());
                assertEquals(second, sliceBC.valueAt(0));
                assertEquals(third, sliceBC.valueAt(1));
            }

            assertSame(set, set.slice(new ImmutableIntRange(0, 2)));
            assertSame(set, set.slice(new ImmutableIntRange(0, 3)));
            assertSame(set, set.slice(new ImmutableIntRange(0, 67)));
        })));
    }
}

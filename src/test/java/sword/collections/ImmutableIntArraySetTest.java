package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ImmutableIntArraySetTest extends ImmutableIntSetTest {

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE, -500, -2, -1, 0, 1, 3, 127, 128, Integer.MAX_VALUE
    };

    @Override
    public ImmutableIntArraySet.Builder newIntBuilder() {
        return new ImmutableIntArraySet.Builder();
    }

    @Override
    public void withValue(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntSet.Builder>> procedure) {
        procedure.apply(ImmutableIntArraySet.Builder::new);
    }

    @Test
    @Override
    void testSliceWhenEmpty() {
        final ImmutableIntArraySet set = newIntBuilder().build();
        assertSame(set, set.slice(new ImmutableIntRange(0, 0)));
        assertSame(set, set.slice(new ImmutableIntRange(1, 1)));
        assertSame(set, set.slice(new ImmutableIntRange(2, 2)));
        assertSame(set, set.slice(new ImmutableIntRange(0, 1)));
        assertSame(set, set.slice(new ImmutableIntRange(1, 2)));
        assertSame(set, set.slice(new ImmutableIntRange(0, 2)));
    }

    @Test
    @Override
    public void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntArraySet set = newIntBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final int first = set.valueAt(0);
            final int second = (size >= 2)? set.valueAt(1) : 0;
            final int third = (size >= 3)? set.valueAt(2) : 0;

            final ImmutableIntArraySet sliceA = set.slice(new ImmutableIntRange(0, 0));
            if (size == 1) {
                assertSame(set, sliceA);
            }
            else {
                assertEquals(1, sliceA.size());
                assertEquals(first, sliceA.valueAt(0));
            }

            final ImmutableIntArraySet sliceB = set.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertEquals(second, sliceB.valueAt(0));
            }
            else {
                assertTrue(sliceB.isEmpty());
            }

            final ImmutableIntArraySet sliceC = set.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertEquals(third, sliceC.valueAt(0));
            }
            else {
                assertTrue(sliceC.isEmpty());
            }

            final ImmutableIntArraySet sliceAB = set.slice(new ImmutableIntRange(0, 1));
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

            final ImmutableIntArraySet sliceBC = set.slice(new ImmutableIntRange(1, 2));
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
        })));
    }

    @Test
    @Override
    public void testSkipWhenEmpty() {
        final ImmutableIntArraySet set = newIntBuilder().build();
        assertSame(set, set.skip(0));
        assertSame(set, set.skip(1));
        assertSame(set, set.skip(20));
    }

    @Test
    @Override
    public void testSkip() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntArraySet set = newIntBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final int second = (size >= 2)? set.valueAt(1) : 0;
            final int third = (size == 3)? set.valueAt(2) : 0;

            assertSame(set, set.skip(0));

            final ImmutableIntArraySet skip1 = set.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertEquals(second, skip1.valueAt(0));
                if (size == 3) {
                    assertEquals(third, skip1.valueAt(1));
                }
            }

            final ImmutableIntArraySet empty = ImmutableIntArraySet.empty();
            final ImmutableIntArraySet skip2 = set.skip(2);
            if (size == 3) {
                assertEquals(third, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertSame(empty, skip2);
            }

            assertSame(empty, set.skip(3));
            assertSame(empty, set.skip(4));
            assertSame(empty, set.skip(24));
        })));
    }

    @Test
    @Override
    public void testTakeWhenEmpty() {
        final ImmutableIntArraySet set = newIntBuilder().build();
        assertSame(set, set.take(0));
        assertSame(set, set.take(1));
        assertSame(set, set.take(2));
        assertSame(set, set.take(24));
    }

    @Test
    @Override
    public void testTake() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntArraySet set = newIntBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final int first = set.valueAt(0);

            assertSame(ImmutableIntArraySet.empty(), set.take(0));

            final ImmutableIntArraySet take1 = set.take(1);
            if (size > 1) {
                assertEquals(1, take1.size());
                assertEquals(first, take1.valueAt(0));
            }
            else {
                assertSame(set, take1);
            }

            final ImmutableIntArraySet take2 = set.take(2);
            if (size > 2) {
                assertEquals(2, take2.size());
                assertEquals(first, take2.valueAt(0));
                assertEquals(set.valueAt(1), take2.valueAt(1));
            }
            else {
                assertSame(set, take2);
            }

            assertSame(set, set.take(3));
            assertSame(set, set.take(4));
            assertSame(set, set.take(24));
        })));
    }
}

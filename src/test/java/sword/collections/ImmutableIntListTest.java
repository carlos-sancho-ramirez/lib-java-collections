package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ImmutableIntListTest extends IntListTest<ImmutableIntList.Builder> implements ImmutableIntTransformableTest<ImmutableIntList.Builder> {

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE, -1023, -2, -1, 0, 1, 2, 7, 108, Integer.MAX_VALUE
    };

    @Override
    public void withValue(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntList.Builder>> procedure) {
        procedure.apply(ImmutableIntList.Builder::new);
    }

    @Override
    public void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    @Override
    public void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v * v);
        procedure.apply(v -> v + 1);
    }

    private boolean isPositiveValue(int value) {
        return value >= 0;
    }

    @Override
    public void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::isPositiveValue);
    }

    private int moduleFour(int value) {
        return value & 3;
    }

    private void withGroupingFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(value -> Integer.toString(moduleFour(value)));
    }

    private void withGroupingIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(this::moduleFour);
    }

    @Override
    public ImmutableIntList.Builder newIntBuilder() {
        return new ImmutableIntList.Builder();
    }

    @Test
    void testIteratingForMultipleElements() {
        withValue(a -> withValue(b -> {
            final ImmutableIntList list = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> iterator = list.iterator();

            assertTrue(iterator.hasNext());
            assertEquals(a, iterator.next().intValue());

            assertTrue(iterator.hasNext());
            assertEquals(b, iterator.next().intValue());

            assertFalse(iterator.hasNext());
        }));
    }

    @Test
    void testAppendWhenEmpty() {
        withValue(value -> {
            final ImmutableIntList empty = ImmutableIntList.empty();
            final ImmutableIntList list = empty.append(value);
            assertNotSame(empty, list);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    @Test
    void testAppendForASingleElement() {
        withValue(a -> withValue(value -> {
            final ImmutableIntList initList = new ImmutableIntList.Builder().append(a).build();
            final ImmutableIntList list = initList.append(value);
            assertEquals(2, list.size());
            assertEquals(a, list.get(0));
            assertEquals(value, list.get(1));
        }));
    }

    @Test
    void testAppendAllWhenBothEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        final ImmutableIntList result = empty.appendAll(empty);
        assertSame(empty, result);
    }

    @Test
    void testAppendANonEmptyListWhenEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        withValue(value -> {
            final ImmutableIntList list = new ImmutableIntList.Builder().append(value).build();
            final ImmutableIntList result = empty.appendAll(list);
            assertSame(list, result);
        });
    }

    @Test
    void testAppendAnEmptyListWhenNoEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        withValue(value -> {
            final ImmutableIntList list = new ImmutableIntList.Builder().append(value).build();
            final ImmutableIntList result = list.appendAll(empty);
            assertSame(list, result);
        });
    }

    @Test
    void testAppendAll() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntList list1 = new ImmutableIntList.Builder().append(a).append(b).build();
            final ImmutableIntList list2 = new ImmutableIntList.Builder().append(c).build();

            final ImmutableIntList result12 = list1.appendAll(list2);
            assertEquals(3, result12.size());
            assertEquals(a, result12.get(0));
            assertEquals(b, result12.get(1));
            assertEquals(c, result12.get(2));

            final ImmutableIntList result21 = list2.appendAll(list1);
            assertEquals(3, result21.size());
            assertEquals(c, result21.get(0));
            assertEquals(a, result21.get(1));
            assertEquals(b, result21.get(2));
        })));
    }

    @Test
    void testReverseWhenEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        assertSame(empty, empty.reverse());
    }

    @Test
    void testReverseForSingleElement() {
        withValue(value -> {
            final ImmutableIntList list = newIntBuilder().append(value).build();
            assertSame(list, list.reverse());
        });
    }

    @Test
    void testReverseForTwoElements() {
        withValue(a -> withValue(b -> {
            final ImmutableIntList list = newIntBuilder().append(a).append(b).build();
            final ImmutableIntList reversed = list.reverse();
            assertEquals(2, reversed.size());
            assertEquals(b, reversed.valueAt(0));
            assertEquals(a, reversed.valueAt(1));
        }));
    }

    @Test
    void testReverseForThreeElements() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntList list = newIntBuilder().append(a).append(b).append(c).build();
            final ImmutableIntList reversed = list.reverse();
            assertEquals(3, reversed.size());
            assertEquals(c, reversed.valueAt(0));
            assertEquals(b, reversed.valueAt(1));
            assertEquals(a, reversed.valueAt(2));
        })));
    }

    @Test
    void testGroupByWhenEmpty() {
        final IntFunction<Integer> func = str -> {
            throw new AssertionError("This function should not be executed");
        };
        final ImmutableIntList list = newIntBuilder().build();
        assertTrue(list.groupBy(func).isEmpty());
    }

    @Test
    void testGroupBy() {
        withGroupingFunc(func -> withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntList list = newIntBuilder().add(a).add(b).add(c).build();
            final String aGroup = func.apply(a);
            final String bGroup = func.apply(b);
            final String cGroup = func.apply(c);

            final ImmutableMap<String, ImmutableIntList> map = list.groupBy(func);
            if (aGroup.equals(bGroup)) {
                if (aGroup.equals(cGroup)) {
                    assertEquals(1, map.size());
                    assertEquals(aGroup, map.keyAt(0));
                    assertSame(list, map.valueAt(0));
                }
                else {
                    assertEquals(2, map.size());
                    if (aGroup.equals(map.keyAt(0))) {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(newIntBuilder().add(a).add(b).build(), map.valueAt(0));
                        assertEquals(newIntBuilder().add(c).build(), map.valueAt(1));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(0));
                        assertEquals(aGroup, map.keyAt(1));
                        assertEquals(newIntBuilder().add(c).build(), map.valueAt(0));
                        assertEquals(newIntBuilder().add(a).add(b).build(), map.valueAt(1));
                    }
                }
            }
            else if (aGroup.equals(cGroup)) {
                assertEquals(2, map.size());
                if (aGroup.equals(map.keyAt(0))) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertEquals(newIntBuilder().add(a).add(c).build(), map.valueAt(0));
                    assertEquals(newIntBuilder().add(b).build(), map.valueAt(1));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertEquals(newIntBuilder().add(b).build(), map.valueAt(0));
                    assertEquals(newIntBuilder().add(a).add(c).build(), map.valueAt(1));
                }
            }
            else if (bGroup.equals(cGroup)) {
                assertEquals(2, map.size());
                if (aGroup.equals(map.keyAt(0))) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertEquals(newIntBuilder().add(a).build(), map.valueAt(0));
                    assertEquals(newIntBuilder().add(b).add(c).build(), map.valueAt(1));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertEquals(newIntBuilder().add(b).add(c).build(), map.valueAt(0));
                    assertEquals(newIntBuilder().add(a).build(), map.valueAt(1));
                }
            }
            else {
                assertEquals(3, map.size());
                if (aGroup.equals(map.keyAt(0))) {
                    assertEquals(newIntBuilder().add(a).build(), map.valueAt(0));
                    if (bGroup.equals(map.keyAt(1))) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertEquals(newIntBuilder().add(b).build(), map.valueAt(1));
                        assertEquals(newIntBuilder().add(c).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(bGroup, map.keyAt(2));
                        assertEquals(newIntBuilder().add(c).build(), map.valueAt(1));
                        assertEquals(newIntBuilder().add(b).build(), map.valueAt(2));
                    }
                }
                else if (bGroup.equals(map.keyAt(0))) {
                    assertEquals(newIntBuilder().add(b).build(), map.valueAt(0));
                    if (aGroup.equals(map.keyAt(1))) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertEquals(newIntBuilder().add(a).build(), map.valueAt(1));
                        assertEquals(newIntBuilder().add(c).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertEquals(newIntBuilder().add(c).build(), map.valueAt(1));
                        assertEquals(newIntBuilder().add(a).build(), map.valueAt(2));
                    }
                }
                else {
                    assertEquals(cGroup, map.keyAt(0));
                    assertEquals(newIntBuilder().add(c).build(), map.valueAt(0));
                    if (aGroup.equals(map.keyAt(1))) {
                        assertEquals(bGroup, map.keyAt(2));
                        assertEquals(newIntBuilder().add(a).build(), map.valueAt(1));
                        assertEquals(newIntBuilder().add(b).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(bGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertEquals(newIntBuilder().add(b).build(), map.valueAt(1));
                        assertEquals(newIntBuilder().add(a).build(), map.valueAt(2));
                    }
                }
            }
        }))));
    }

    @Test
    void testGroupByIntWhenEmpty() {
        final IntToIntFunction func = str -> {
            throw new AssertionError("This function should not be executed");
        };
        final ImmutableIntList list = newIntBuilder().build();
        assertTrue(list.groupByInt(func).isEmpty());
    }

    @Test
    void testGroupByInt() {
        withGroupingIntFunc(func -> withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntList list = newIntBuilder().add(a).add(b).add(c).build();
            final int aGroup = func.apply(a);
            final int bGroup = func.apply(b);
            final int cGroup = func.apply(c);

            final ImmutableIntKeyMap<ImmutableIntList> map = list.groupByInt(func);
            if (aGroup == bGroup) {
                if (aGroup == cGroup) {
                    assertEquals(1, map.size());
                    assertEquals(aGroup, map.keyAt(0));
                    assertSame(list, map.valueAt(0));
                }
                else {
                    assertEquals(2, map.size());
                    if (aGroup == map.keyAt(0)) {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(newIntBuilder().add(a).add(b).build(), map.valueAt(0));
                        assertEquals(newIntBuilder().add(c).build(), map.valueAt(1));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(0));
                        assertEquals(aGroup, map.keyAt(1));
                        assertEquals(newIntBuilder().add(c).build(), map.valueAt(0));
                        assertEquals(newIntBuilder().add(a).add(b).build(), map.valueAt(1));
                    }
                }
            }
            else if (aGroup == cGroup) {
                assertEquals(2, map.size());
                if (aGroup == map.keyAt(0)) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertEquals(newIntBuilder().add(a).add(c).build(), map.valueAt(0));
                    assertEquals(newIntBuilder().add(b).build(), map.valueAt(1));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertEquals(newIntBuilder().add(b).build(), map.valueAt(0));
                    assertEquals(newIntBuilder().add(a).add(c).build(), map.valueAt(1));
                }
            }
            else if (bGroup == cGroup) {
                assertEquals(2, map.size());
                if (aGroup == map.keyAt(0)) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertEquals(newIntBuilder().add(a).build(), map.valueAt(0));
                    assertEquals(newIntBuilder().add(b).add(c).build(), map.valueAt(1));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertEquals(newIntBuilder().add(b).add(c).build(), map.valueAt(0));
                    assertEquals(newIntBuilder().add(a).build(), map.valueAt(1));
                }
            }
            else {
                assertEquals(3, map.size());
                if (aGroup == map.keyAt(0)) {
                    assertEquals(newIntBuilder().add(a).build(), map.valueAt(0));
                    if (bGroup == map.keyAt(1)) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertEquals(newIntBuilder().add(b).build(), map.valueAt(1));
                        assertEquals(newIntBuilder().add(c).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(bGroup, map.keyAt(2));
                        assertEquals(newIntBuilder().add(c).build(), map.valueAt(1));
                        assertEquals(newIntBuilder().add(b).build(), map.valueAt(2));
                    }
                }
                else if (bGroup == map.keyAt(0)) {
                    assertEquals(newIntBuilder().add(b).build(), map.valueAt(0));
                    if (aGroup == map.keyAt(1)) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertEquals(newIntBuilder().add(a).build(), map.valueAt(1));
                        assertEquals(newIntBuilder().add(c).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertEquals(newIntBuilder().add(c).build(), map.valueAt(1));
                        assertEquals(newIntBuilder().add(a).build(), map.valueAt(2));
                    }
                }
                else {
                    assertEquals(cGroup, map.keyAt(0));
                    assertEquals(newIntBuilder().add(c).build(), map.valueAt(0));
                    if (aGroup == map.keyAt(1)) {
                        assertEquals(bGroup, map.keyAt(2));
                        assertEquals(newIntBuilder().add(a).build(), map.valueAt(1));
                        assertEquals(newIntBuilder().add(b).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(bGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertEquals(newIntBuilder().add(b).build(), map.valueAt(1));
                        assertEquals(newIntBuilder().add(a).build(), map.valueAt(2));
                    }
                }
            }
        }))));
    }

    @Test
    @Override
    void testSliceWhenEmpty() {
        final ImmutableIntList list = newIntBuilder().build();
        assertSame(list, list.slice(new ImmutableIntRange(0, 0)));
        assertSame(list, list.slice(new ImmutableIntRange(1, 1)));
        assertSame(list, list.slice(new ImmutableIntRange(0, 1)));
        assertSame(list, list.slice(new ImmutableIntRange(1, 2)));
        assertSame(list, list.slice(new ImmutableIntRange(0, 2)));
    }

    @Test
    @Override
    public void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntList list = newIntBuilder().append(a).append(b).append(c).build();

            final ImmutableIntList sliceA = list.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertEquals(a, sliceA.valueAt(0));

            final ImmutableIntList sliceB = list.slice(new ImmutableIntRange(1, 1));
            assertEquals(1, sliceB.size());
            assertEquals(b, sliceB.valueAt(0));

            final ImmutableIntList sliceC = list.slice(new ImmutableIntRange(2, 2));
            assertEquals(1, sliceC.size());
            assertEquals(c, sliceC.valueAt(0));

            final ImmutableIntList sliceAB = list.slice(new ImmutableIntRange(0, 1));
            assertEquals(2, sliceAB.size());
            assertEquals(a, sliceAB.valueAt(0));
            assertEquals(b, sliceAB.valueAt(1));

            final ImmutableIntList sliceBC = list.slice(new ImmutableIntRange(1, 2));
            assertEquals(2, sliceBC.size());
            assertEquals(b, sliceBC.valueAt(0));
            assertEquals(c, sliceBC.valueAt(1));

            assertSame(list, list.slice(new ImmutableIntRange(0, 2)));
            assertSame(list, list.slice(new ImmutableIntRange(0, 3)));
        })));
    }

    @Test
    void testSkip() {
        withFilterFunc(f -> withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntList list = newIntBuilder().add(a).add(b).add(c).build();

            assertSame(list, list.skip(0));
            assertEquals(newIntBuilder().add(b).add(c).build(), list.skip(1));
            assertEquals(newIntBuilder().add(c).build(), list.skip(2));

            final ImmutableIntList emptyList = ImmutableIntList.empty();
            assertSame(emptyList, list.skip(3));
            assertSame(emptyList, list.skip(4));
            assertSame(emptyList, list.skip(24));
        }))));
    }

    @Test
    void testSkipLast() {
        withFilterFunc(f -> withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntList list = newIntBuilder().add(a).add(b).add(c).build();

            assertSame(list, list.skipLast(0));
            assertEquals(newIntBuilder().add(a).add(b).build(), list.skipLast(1));
            assertEquals(newIntBuilder().add(a).build(), list.skipLast(2));

            final ImmutableIntList emptyList = ImmutableIntList.empty();
            assertSame(emptyList, list.skipLast(3));
            assertSame(emptyList, list.skipLast(4));
            assertSame(emptyList, list.skipLast(24));
        }))));
    }
}

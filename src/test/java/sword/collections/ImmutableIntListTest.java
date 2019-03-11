package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public final class ImmutableIntListTest extends IntTransformableTest implements ImmutableIntTransformableTest<ImmutableIntList> {

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
    public void withTransformableBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntList, ImmutableIntTransformableBuilder<ImmutableIntList>>> procedure) {
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
    void withFilterFunc(Procedure<IntPredicate> procedure) {
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
    ImmutableIntList.Builder newIntBuilder() {
        return new ImmutableIntList.Builder();
    }

    @Test
    public void testIteratingForMultipleElements() {
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
    public void testAppendWhenEmpty() {
        withValue(value -> {
            final ImmutableIntList empty = ImmutableIntList.empty();
            final ImmutableIntList list = empty.append(value);
            assertNotSame(empty, list);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    @Test
    public void testAppendForASingleElement() {
        withValue(a -> withValue(value -> {
            final ImmutableIntList initList = new ImmutableIntList.Builder().append(a).build();
            final ImmutableIntList list = initList.append(value);
            assertEquals(2, list.size());
            assertEquals(a, list.get(0));
            assertEquals(value, list.get(1));
        }));
    }

    @Test
    public void testAppendAllWhenBothEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        final ImmutableList<String> result = empty.appendAll(empty);
        assertSame(empty, result);
    }

    @Test
    public void testAppendANonEmptyListWhenEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        withValue(value -> {
            final ImmutableIntList list = new ImmutableIntList.Builder().append(value).build();
            final ImmutableIntList result = empty.appendAll(list);
            assertSame(list, result);
        });
    }

    @Test
    public void testAppendAnEmptyListWhenNoEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        withValue(value -> {
            final ImmutableIntList list = new ImmutableIntList.Builder().append(value).build();
            final ImmutableIntList result = list.appendAll(empty);
            assertSame(list, result);
        });
    }

    @Test
    public void testAppendAll() {
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
    public void testGroupByWhenEmpty() {
        final IntFunction<Integer> func = str -> {
            throw new AssertionError("This function should not be executed");
        };
        final ImmutableIntList list = newIntBuilder().build();
        assertTrue(list.groupBy(func).isEmpty());
    }

    @Test
    public void testGroupBy() {
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
    public void testGroupByIntWhenEmpty() {
        final IntToIntFunction func = str -> {
            throw new AssertionError("This function should not be executed");
        };
        final ImmutableIntList list = newIntBuilder().build();
        assertTrue(list.groupByInt(func).isEmpty());
    }

    @Test
    public void testGroupByInt() {
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
}

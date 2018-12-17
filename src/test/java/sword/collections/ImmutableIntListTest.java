package sword.collections;

import java.util.Iterator;

public final class ImmutableIntListTest extends AbstractImmutableIntIterableTest {

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE, -1023, -2, -1, 0, 1, 2, 7, 108, Integer.MAX_VALUE
    };

    @Override
    void withItem(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    @Override
    void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
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
    ImmutableIntList emptyCollection() {
        return ImmutableIntList.empty();
    }

    @Override
    ImmutableList<String> mapTargetEmptyCollection() {
        return ImmutableList.empty();
    }

    @Override
    ImmutableIntList.Builder newIntBuilder() {
        return new ImmutableIntList.Builder();
    }

    public void testSizeForTwoElements() {
        withItem(a -> withItem(b -> {
            final ImmutableIntList list = newIntBuilder().add(a).add(b).build();
            final int size = list.size();
            if (size != 2) {
                fail("Expected size 2 after building it adding values " + a + " and " + b +
                        ". But it was " + size);
            }
        }));
    }

    public void testIteratingForMultipleElements() {
        withItem(a -> withItem(b -> {
            final ImmutableIntList list = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> iterator = list.iterator();

            assertTrue(iterator.hasNext());
            assertEquals(a, iterator.next().intValue());

            assertTrue(iterator.hasNext());
            assertEquals(b, iterator.next().intValue());

            assertFalse(iterator.hasNext());
        }));
    }

    public void testMapForMultipleElements() {
        withMapFunc(f -> withItem(a -> withItem(b -> {
            final ImmutableIntList collection = newIntBuilder().add(a).add(b).build();
            final ImmutableList<String> mapped = collection.map(f);
            final Iterator<String> iterator = mapped.iterator();

            for (int item : collection) {
                assertTrue(iterator.hasNext());
                assertEquals(f.apply(item), iterator.next());
            }
            assertFalse(iterator.hasNext());
        })));
    }

    public void testFindFirstWhenEmpty() {
        withFilterFunc(f -> withItem(defaultValue -> {
            final ImmutableIntList list = newIntBuilder().build();
            assertEquals(defaultValue, list.findFirst(f, defaultValue));
        }));
    }

    public void testFindFirstForSingleElement() {
        withFilterFunc(f -> withItem(defaultValue -> withItem(value -> {
            final ImmutableIntList list = newIntBuilder().append(value).build();
            final int first = list.findFirst(f, defaultValue);

            if (f.apply(value)) {
                assertEquals(value, first);
            }
            else {
                assertEquals(defaultValue, first);
            }
        })));
    }

    public void testFindFirstForMultipleElements() {
        withFilterFunc(f -> withItem(defaultValue -> withItem(a -> withItem(b -> {
            final ImmutableIntList list = newIntBuilder().append(a).append(b).build();
            final int first = list.findFirst(f, defaultValue);

            if (f.apply(a)) {
                assertEquals(a, first);
            }
            else if (f.apply(b)) {
                assertEquals(b, first);
            }
            else {
                assertEquals(defaultValue, first);
            }
        }))));
    }

    public void testAppendWhenEmpty() {
        withItem(value -> {
            final ImmutableIntList empty = ImmutableIntList.empty();
            final ImmutableIntList list = empty.append(value);
            assertNotSame(empty, list);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    public void testAppendForASingleElement() {
        withItem(a -> withItem(value -> {
            final ImmutableIntList initList = new ImmutableIntList.Builder().append(a).build();
            final ImmutableIntList list = initList.append(value);
            assertEquals(2, list.size());
            assertEquals(a, list.get(0));
            assertEquals(value, list.get(1));
        }));
    }

    public void testAppendAllWhenBothEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        final ImmutableList<String> result = empty.appendAll(empty);
        assertSame(empty, result);
    }

    public void testAppendANonEmptyListWhenEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        withItem(value -> {
            final ImmutableIntList list = new ImmutableIntList.Builder().append(value).build();
            final ImmutableIntList result = empty.appendAll(list);
            assertSame(list, result);
        });
    }

    public void testAppendAnEmptyListWhenNoEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        withItem(value -> {
            final ImmutableIntList list = new ImmutableIntList.Builder().append(value).build();
            final ImmutableIntList result = list.appendAll(empty);
            assertSame(list, result);
        });
    }

    public void testAppendAll() {
        withItem(a -> withItem(b -> withItem(c -> {
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

    public void testFilterForSingleElement() {
        super.testFilterForSingleElement();
    }

    public void testToSetWhenEmpty() {
        final ImmutableIntList list = newIntBuilder().build();
        assertTrue(list.toSet().isEmpty());
    }

    public void testToSetWithSingleElement() {
        withItem(a -> {
            final ImmutableIntList list = newIntBuilder().add(a).build();
            final ImmutableIntSet set = list.toSet();
            assertEquals(1, set.size());
            assertEquals(a, set.valueAt(0));
        });
    }

    public void testToSetWithTwoElements() {
        withItem(a -> withItem(b -> {
            final ImmutableIntList list = newIntBuilder().add(a).add(b).build();
            final ImmutableIntSet set = list.toSet();
            if (a == b) {
                assertEquals(1, set.size());
                assertEquals(a, set.valueAt(0));
            }
            else {
                assertEquals(2, set.size());
                if (a < b) {
                    assertEquals(a, set.valueAt(0));
                    assertEquals(b, set.valueAt(1));
                }
                else {
                    assertEquals(a, set.valueAt(1));
                    assertEquals(b, set.valueAt(0));
                }
            }
        }));
    }

    public void testGroupByWhenEmpty() {
        final IntFunction<Integer> func = str -> {
            throw new AssertionError("This function should not be executed");
        };
        final ImmutableIntList list = newIntBuilder().build();
        assertTrue(list.groupBy(func).isEmpty());
    }

    public void testGroupBy() {
        withGroupingFunc(func -> withItem(a -> withItem(b -> withItem(c -> {
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

    public void testGroupByIntWhenEmpty() {
        final IntToIntFunction func = str -> {
            throw new AssertionError("This function should not be executed");
        };
        final ImmutableIntList list = newIntBuilder().build();
        assertTrue(list.groupByInt(func).isEmpty());
    }

    public void testGroupByInt() {
        withGroupingIntFunc(func -> withItem(a -> withItem(b -> withItem(c -> {
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

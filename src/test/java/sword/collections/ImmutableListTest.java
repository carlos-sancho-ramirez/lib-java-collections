package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sword.collections.TestUtils.withInt;

public final class ImmutableListTest extends ListTest<String, ImmutableList.Builder<String>> implements ImmutableTransformableTest<String, ImmutableList.Builder<String>> {

    private static final String[] STRING_VALUES = {
            null, "", "_", "0", "abcd"
    };

    private void withString(Procedure<String> procedure) {
        for (String str : STRING_VALUES) {
            procedure.apply(str);
        }
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableList.Builder<String>>> procedure) {
        procedure.apply(ImmutableList.Builder::new);
    }

    @Override
    public void withValue(Procedure<String> procedure) {
        for (String str : STRING_VALUES) {
            procedure.apply(str);
        }
    }

    private String reduceFunc(String left, String right) {
        return String.valueOf(left) + '-' + String.valueOf(right);
    }

    @Override
    public void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
        procedure.apply(this::reduceFunc);
    }

    private String prefixUnderscore(String value) {
        return "_" + value;
    }

    private String charCounter(String value) {
        final int length = (value != null)? value.length() : 0;
        return Integer.toString(length);
    }

    @Override
    public void withMapFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(this::prefixUnderscore);
        procedure.apply(this::charCounter);
    }

    @Override
    public void withMapToIntFunc(Procedure<IntResultFunction<String>> procedure) {
        procedure.apply(str -> (str == null)? 0 : str.hashCode());
    }

    private boolean filterFunc(String value) {
        return value != null && !value.isEmpty();
    }

    @Override
    public void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::filterFunc);
    }

    private int takeStringLength(String str) {
        return (str == null)? 0 : str.length();
    }

    void withGroupingFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(str -> Integer.toString(takeStringLength(str)));
    }

    void withGroupingIntFunc(Procedure<IntResultFunction<String>> procedure) {
        procedure.apply(this::takeStringLength);
    }

    private boolean sortAlphabetically(String a, String b) {
        if (b == null) {
            return false;
        }

        if (a == null) {
            return true;
        }

        final int aLength = a.length();
        final int bLength = b.length();
        for (int i = 0; i < aLength; i++) {
            if (bLength == i) {
                return false;
            }

            final char charA = a.charAt(i);
            final char charB = b.charAt(i);
            if (charA < charB) {
                return true;
            }
            else if (charA > charB) {
                return false;
            }
        }

        return bLength > aLength;
    }

    ImmutableList.Builder<String> newBuilder() {
        return new ImmutableList.Builder<>();
    }

    @Test
    void testSizeForTwoElements() {
        withValue(a -> withValue(b -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).build();
            final int size = list.size();
            if (size != 2) {
                fail("Expected size 2 after building it adding values " + a + " and " + b +
                        ". But it was " + size);
            }
        }));
    }

    @Test
    void testIteratingForMultipleElements() {
        withValue(a -> withValue(b -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).build();
            final Iterator<String> iterator = list.iterator();

            assertTrue(iterator.hasNext());
            assertEquals(a, iterator.next());

            assertTrue(iterator.hasNext());
            assertEquals(b, iterator.next());

            assertFalse(iterator.hasNext());
        }));
    }

    @Test
    public void testSkip() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).add(c).build();

            assertSame(list, list.skip(0));
            assertEquals(newBuilder().add(b).add(c).build(), list.skip(1));
            assertEquals(newBuilder().add(c).build(), list.skip(2));

            final ImmutableList<String> emptyList = ImmutableList.empty();
            assertSame(emptyList, list.skip(3));
            assertSame(emptyList, list.skip(4));
            assertSame(emptyList, list.skip(24));
        })));
    }

    @Test
    void testSkipLast() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).add(c).build();

            assertSame(list, list.skipLast(0));
            assertEquals(newBuilder().add(a).add(b).build(), list.skipLast(1));
            assertEquals(newBuilder().add(a).build(), list.skipLast(2));

            final ImmutableList<String> emptyList = ImmutableList.empty();
            assertSame(emptyList, list.skipLast(3));
            assertSame(emptyList, list.skipLast(4));
            assertSame(emptyList, list.skipLast(24));
        })));
    }

    @Test
    @Override
    public void testTakeWhenEmpty() {
        final ImmutableList<String> list = newBuilder().build();
        assertSame(list, list.take(0));
        assertSame(list, list.take(1));
        assertSame(list, list.take(2));
        assertSame(list, list.take(24));
    }

    @Test
    @Override
    public void testTake() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).add(c).build();

            assertSame(ImmutableList.empty(), list.take(0));

            final ImmutableList<String> take1 = list.take(1);
            assertEquals(1, take1.size());
            assertSame(a, take1.valueAt(0));

            final ImmutableList<String> take2 = list.take(2);
            assertEquals(2, take2.size());
            assertSame(a, take2.valueAt(0));
            assertSame(b, take2.valueAt(1));

            assertSame(list, list.take(3));
            assertSame(list, list.take(4));
            assertSame(list, list.take(24));
        })));
    }

    @Test
    void testReverseWhenEmpty() {
        final ImmutableList empty = ImmutableList.empty();
        assertSame(empty, empty.reverse());
    }

    @Test
    void testReverseForSingleElement() {
        withValue(value -> {
            final ImmutableList<String> list = newBuilder().append(value).build();
            assertSame(list, list.reverse());
        });
    }

    @Test
    void testReverseForTwoElements() {
        withValue(a -> withValue(b -> {
            final ImmutableList<String> list = newBuilder().append(a).append(b).build();
            final ImmutableList<String> reversed = list.reverse();
            assertEquals(2, reversed.size());
            assertSame(b, reversed.valueAt(0));
            assertSame(a, reversed.valueAt(1));
        }));
    }

    @Test
    void testReverseForThreeElements() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableList<String> list = newBuilder().append(a).append(b).append(c).build();
            final ImmutableList<String> reversed = list.reverse();
            assertEquals(3, reversed.size());
            assertSame(c, reversed.valueAt(0));
            assertSame(b, reversed.valueAt(1));
            assertSame(a, reversed.valueAt(2));
        })));
    }

    @Test
    void testSpan() {
        withFilterFunc(f -> withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).add(c).build();
            final ImmutableList<String> filtered = list.filter(f);
            final ImmutableList<String> filteredNot = list.filterNot(f);

            ImmutablePair<ImmutableList<String>, ImmutableList<String>> pair = list.span(f);
            assertEquals(filtered, pair.left);
            assertEquals(filteredNot, pair.right);

            if (filtered.isEmpty()) {
                assertSame(ImmutableList.empty(), pair.left);
                assertSame(list, pair.right);
            }

            if (filteredNot.isEmpty()) {
                assertSame(list, pair.left);
                assertSame(ImmutableList.empty(), pair.right);
            }
        }))));
    }

    @Test
    void testReduceFor1Item() {
        withString(value ->  {
            final ImmutableList<String> list = new ImmutableList.Builder<String>()
                    .add(value)
                    .build();

            final ReduceFunction<String> func = (l, r) -> {
                fail("Should not be called for a single item");
                return l;
            };

            assertEquals(list.get(0), list.reduce(func));
        });
    }

    @Test
    void testReduceFor2Items() {
        withInt(a -> withInt(b -> {
            final ImmutableList<Integer> list = new ImmutableList.Builder<Integer>()
                    .add(a)
                    .add(b)
                    .build();

            final ReduceFunction<Integer> func = (l, r) -> l + r;
            final Integer result = list.reduce(func);
            assertEquals(func.apply(a, b), result);
        }));
    }

    @Test
    void testReduceFor3Items() {
        withInt(a -> withInt(b -> withInt(c -> {
            final ImmutableList<String> list = new ImmutableList.Builder<String>()
                    .add(Integer.toString(a))
                    .add(Integer.toString(b))
                    .add(Integer.toString(c))
                    .build();

            final ReduceFunction<String> func = (l, r) -> l + ", " + r;
            final String result = list.reduce(func);
            assertEquals(func.apply(func.apply(Integer.toString(a), Integer.toString(b)), Integer.toString(c)), result);
        })));
    }

    @Test
    void testAppendWhenEmpty() {
        withString(value -> {
            final ImmutableList<String> empty = ImmutableList.empty();
            final ImmutableList<String> list = empty.append(value);
            assertNotSame(empty, list);
            assertEquals(1, list.size());
            assertSame(value, list.get(0));
        });
    }

    @Test
    void testAppendForASingleElement() {
        withString(a -> withString(value -> {
            final ImmutableList<String> initList = new ImmutableList.Builder<String>().append(a).build();
            final ImmutableList<String> list = initList.append(value);
            assertEquals(2, list.size());
            assertSame(a, list.get(0));
            assertSame(value, list.get(1));
        }));
    }

    @Test
    void testAppendAllWhenBothEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        final ImmutableList<String> result = empty.appendAll(empty);
        assertSame(empty, result);
    }

    @Test
    void testPrependWhenEmpty() {
        withString(value -> {
            final ImmutableList<String> empty = ImmutableList.empty();
            final ImmutableList<String> list = empty.prepend(value);
            assertNotSame(empty, list);
            assertEquals(1, list.size());
            assertSame(value, list.get(0));
        });
    }

    @Test
    void testPrependForASingleElement() {
        withString(a -> withString(value -> {
            final ImmutableList<String> initList = new ImmutableList.Builder<String>().append(a).build();
            final ImmutableList<String> list = initList.prepend(value);
            assertEquals(2, list.size());
            assertSame(value, list.get(0));
            assertSame(a, list.get(1));
        }));
    }

    @Test
    void testAppendANonEmptyListWhenEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        withString(value -> {
            final ImmutableList<String> list = new ImmutableList.Builder<String>().append(value).build();
            final ImmutableList<String> result = empty.appendAll(list);
            assertSame(list, result);
        });
    }

    @Test
    void testAppendAnEmptyListWhenNoEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        withString(value -> {
            final ImmutableList<String> list = new ImmutableList.Builder<String>().append(value).build();
            final ImmutableList<String> result = list.appendAll(empty);
            assertSame(list, result);
        });
    }

    @Test
    void testAppendAllForImmutableListArgument() {
        withString(a -> withString(b -> withString(c -> {
            final ImmutableList<String> list1 = new ImmutableList.Builder<String>().append(a).append(b).build();
            final ImmutableList<String> list2 = new ImmutableList.Builder<String>().append(c).build();

            final ImmutableList<String> result12 = list1.appendAll(list2);
            assertEquals(3, result12.size());
            assertEquals(a, result12.get(0));
            assertEquals(b, result12.get(1));
            assertEquals(c, result12.get(2));

            final ImmutableList<String> result21 = list2.appendAll(list1);
            assertEquals(3, result21.size());
            assertEquals(c, result21.get(0));
            assertEquals(a, result21.get(1));
            assertEquals(b, result21.get(2));
        })));
    }

    @Test
    void testAppendAll() {
        withString(a -> withString(b -> withString(c -> {
            final ImmutableList<String> list1 = new ImmutableList.Builder<String>().append(a).append(b).build();
            final MutableMap<Integer, String> arg1 = new MutableHashMap.Builder<Integer, String>().put(1, c).build();
            final ImmutableList<String> result12 = list1.appendAll(arg1);
            assertEquals(3, result12.size());
            assertEquals(a, result12.get(0));
            assertEquals(b, result12.get(1));
            assertEquals(c, result12.get(2));

            final ImmutableList<String> list2 = new ImmutableList.Builder<String>().append(c).build();
            final MutableMap<Integer, String> arg2 = new MutableHashMap.Builder<Integer, String>().put(1, a).put(2, b).build();

            final ImmutableList<String> result21 = list2.appendAll(arg2);
            assertEquals(3, result21.size());
            assertEquals(c, result21.get(0));
            assertEquals(a, result21.get(1));
            assertEquals(b, result21.get(2));
        })));
    }

    @Test
    void testSort() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).add(c).build();
            final ImmutableList<String> sortedList = list.sort(this::sortAlphabetically);

            if (sortAlphabetically(b, a)) {
                if (sortAlphabetically(c, b)) {
                    assertEquals(c, sortedList.valueAt(0));
                    assertEquals(b, sortedList.valueAt(1));
                    assertEquals(a, sortedList.valueAt(2));
                }
                else if (sortAlphabetically(c, a)) {
                    assertEquals(b, sortedList.valueAt(0));
                    assertEquals(c, sortedList.valueAt(1));
                    assertEquals(a, sortedList.valueAt(2));
                }
                else {
                    assertEquals(b, sortedList.valueAt(0));
                    assertEquals(a, sortedList.valueAt(1));
                    assertEquals(c, sortedList.valueAt(2));
                }
            }
            else {
                if (sortAlphabetically(c, a)) {
                    assertEquals(c, sortedList.valueAt(0));
                    assertEquals(a, sortedList.valueAt(1));
                    assertEquals(b, sortedList.valueAt(2));
                }
                else if (sortAlphabetically(c, b)) {
                    assertEquals(a, sortedList.valueAt(0));
                    assertEquals(c, sortedList.valueAt(1));
                    assertEquals(b, sortedList.valueAt(2));
                }
                else {
                    assertSame(list, sortedList);
                }
            }
        })));
    }

    @Test
    void testGroupByWhenEmpty() {
        final Function<String, Integer> func = str -> {
            throw new AssertionError("This function should not be executed");
        };
        final ImmutableList<String> list = newBuilder().build();
        assertTrue(list.groupBy(func).isEmpty());
    }

    @Test
    void testGroupBy() {
        withGroupingFunc(func -> withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).add(c).build();
            final String aGroup = func.apply(a);
            final String bGroup = func.apply(b);
            final String cGroup = func.apply(c);

            final ImmutableMap<String, ImmutableList<String>> map = list.groupBy(func);
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
                        assertEquals(newBuilder().add(a).add(b).build(), map.valueAt(0));
                        assertEquals(newBuilder().add(c).build(), map.valueAt(1));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(0));
                        assertEquals(aGroup, map.keyAt(1));
                        assertEquals(newBuilder().add(c).build(), map.valueAt(0));
                        assertEquals(newBuilder().add(a).add(b).build(), map.valueAt(1));
                    }
                }
            }
            else if (aGroup.equals(cGroup)) {
                assertEquals(2, map.size());
                if (aGroup.equals(map.keyAt(0))) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertEquals(newBuilder().add(a).add(c).build(), map.valueAt(0));
                    assertEquals(newBuilder().add(b).build(), map.valueAt(1));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertEquals(newBuilder().add(b).build(), map.valueAt(0));
                    assertEquals(newBuilder().add(a).add(c).build(), map.valueAt(1));
                }
            }
            else if (bGroup.equals(cGroup)) {
                assertEquals(2, map.size());
                if (aGroup.equals(map.keyAt(0))) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertEquals(newBuilder().add(a).build(), map.valueAt(0));
                    assertEquals(newBuilder().add(b).add(c).build(), map.valueAt(1));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertEquals(newBuilder().add(b).add(c).build(), map.valueAt(0));
                    assertEquals(newBuilder().add(a).build(), map.valueAt(1));
                }
            }
            else {
                assertEquals(3, map.size());
                if (aGroup.equals(map.keyAt(0))) {
                    assertEquals(newBuilder().add(a).build(), map.valueAt(0));
                    if (bGroup.equals(map.keyAt(1))) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertEquals(newBuilder().add(b).build(), map.valueAt(1));
                        assertEquals(newBuilder().add(c).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(bGroup, map.keyAt(2));
                        assertEquals(newBuilder().add(c).build(), map.valueAt(1));
                        assertEquals(newBuilder().add(b).build(), map.valueAt(2));
                    }
                }
                else if (bGroup.equals(map.keyAt(0))) {
                    assertEquals(newBuilder().add(b).build(), map.valueAt(0));
                    if (aGroup.equals(map.keyAt(1))) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertEquals(newBuilder().add(a).build(), map.valueAt(1));
                        assertEquals(newBuilder().add(c).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertEquals(newBuilder().add(c).build(), map.valueAt(1));
                        assertEquals(newBuilder().add(a).build(), map.valueAt(2));
                    }
                }
                else {
                    assertEquals(cGroup, map.keyAt(0));
                    assertEquals(newBuilder().add(c).build(), map.valueAt(0));
                    if (aGroup.equals(map.keyAt(1))) {
                        assertEquals(bGroup, map.keyAt(2));
                        assertEquals(newBuilder().add(a).build(), map.valueAt(1));
                        assertEquals(newBuilder().add(b).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(bGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertEquals(newBuilder().add(b).build(), map.valueAt(1));
                        assertEquals(newBuilder().add(a).build(), map.valueAt(2));
                    }
                }
            }
        }))));
    }

    @Test
    void testGroupByIntWhenEmpty() {
        final IntResultFunction<String> func = str -> {
            throw new AssertionError("This function should not be executed");
        };
        final ImmutableList<String> list = newBuilder().build();
        assertTrue(list.groupByInt(func).isEmpty());
    }

    @Test
    void testGroupByInt() {
        withGroupingIntFunc(func -> withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).add(c).build();
            final int aGroup = func.apply(a);
            final int bGroup = func.apply(b);
            final int cGroup = func.apply(c);

            final ImmutableIntKeyMap<ImmutableList<String>> map = list.groupByInt(func);
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
                        assertEquals(newBuilder().add(a).add(b).build(), map.valueAt(0));
                        assertEquals(newBuilder().add(c).build(), map.valueAt(1));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(0));
                        assertEquals(aGroup, map.keyAt(1));
                        assertEquals(newBuilder().add(c).build(), map.valueAt(0));
                        assertEquals(newBuilder().add(a).add(b).build(), map.valueAt(1));
                    }
                }
            }
            else if (aGroup == cGroup) {
                assertEquals(2, map.size());
                if (aGroup == map.keyAt(0)) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertEquals(newBuilder().add(a).add(c).build(), map.valueAt(0));
                    assertEquals(newBuilder().add(b).build(), map.valueAt(1));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertEquals(newBuilder().add(b).build(), map.valueAt(0));
                    assertEquals(newBuilder().add(a).add(c).build(), map.valueAt(1));
                }
            }
            else if (bGroup == cGroup) {
                assertEquals(2, map.size());
                if (aGroup == map.keyAt(0)) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertEquals(newBuilder().add(a).build(), map.valueAt(0));
                    assertEquals(newBuilder().add(b).add(c).build(), map.valueAt(1));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertEquals(newBuilder().add(b).add(c).build(), map.valueAt(0));
                    assertEquals(newBuilder().add(a).build(), map.valueAt(1));
                }
            }
            else {
                assertEquals(3, map.size());
                if (aGroup == map.keyAt(0)) {
                    assertEquals(newBuilder().add(a).build(), map.valueAt(0));
                    if (bGroup == map.keyAt(1)) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertEquals(newBuilder().add(b).build(), map.valueAt(1));
                        assertEquals(newBuilder().add(c).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(bGroup, map.keyAt(2));
                        assertEquals(newBuilder().add(c).build(), map.valueAt(1));
                        assertEquals(newBuilder().add(b).build(), map.valueAt(2));
                    }
                }
                else if (bGroup == map.keyAt(0)) {
                    assertEquals(newBuilder().add(b).build(), map.valueAt(0));
                    if (aGroup == map.keyAt(1)) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertEquals(newBuilder().add(a).build(), map.valueAt(1));
                        assertEquals(newBuilder().add(c).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertEquals(newBuilder().add(c).build(), map.valueAt(1));
                        assertEquals(newBuilder().add(a).build(), map.valueAt(2));
                    }
                }
                else {
                    assertEquals(cGroup, map.keyAt(0));
                    assertEquals(newBuilder().add(c).build(), map.valueAt(0));
                    if (aGroup == map.keyAt(1)) {
                        assertEquals(bGroup, map.keyAt(2));
                        assertEquals(newBuilder().add(a).build(), map.valueAt(1));
                        assertEquals(newBuilder().add(b).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(bGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertEquals(newBuilder().add(b).build(), map.valueAt(1));
                        assertEquals(newBuilder().add(a).build(), map.valueAt(2));
                    }
                }
            }
        }))));
    }

    @Test
    @Override
    public void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableList<String> list = newBuilder().append(a).append(b).append(c).build();

            final ImmutableList<String> sliceA = list.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(a, sliceA.valueAt(0));

            final ImmutableList<String> sliceB = list.slice(new ImmutableIntRange(1, 1));
            assertEquals(1, sliceB.size());
            assertSame(b, sliceB.valueAt(0));

            final ImmutableList<String> sliceC = list.slice(new ImmutableIntRange(2, 2));
            assertEquals(1, sliceC.size());
            assertSame(c, sliceC.valueAt(0));

            final ImmutableList<String> sliceAB = list.slice(new ImmutableIntRange(0, 1));
            assertEquals(2, sliceAB.size());
            assertSame(a, sliceAB.valueAt(0));
            assertSame(b, sliceAB.valueAt(1));

            final ImmutableList<String> sliceBC = list.slice(new ImmutableIntRange(1, 2));
            assertEquals(2, sliceBC.size());
            assertSame(b, sliceBC.valueAt(0));
            assertSame(c, sliceBC.valueAt(1));

            assertSame(list, list.slice(new ImmutableIntRange(0, 2)));
            assertSame(list, list.slice(new ImmutableIntRange(0, 3)));
        })));
    }
}

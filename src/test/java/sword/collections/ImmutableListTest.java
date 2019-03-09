package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;

public final class ImmutableListTest extends TransformableTest<String> implements ImmutableTransformableTest<String> {

    private static final String[] STRING_VALUES = {
            null, "", "_", "0", "abcd"
    };

    private void withString(Procedure<String> procedure) {
        for (String str : STRING_VALUES) {
            procedure.apply(str);
        }
    }

    @Override
    public void withTransformableBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableTransformableBuilder<String>>> procedure) {
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
    void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
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
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
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

    @Override
    ImmutableList.Builder<String> newIterableBuilder() {
        return newBuilder();
    }

    @Test
    public void testSizeForTwoElements() {
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
    public void testIteratingForMultipleElements() {
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
        withFilterFunc(f -> withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).add(c).build();

            assertSame(list, list.skip(0));
            assertEquals(newBuilder().add(b).add(c).build(), list.skip(1));
            assertEquals(newBuilder().add(c).build(), list.skip(2));

            final ImmutableList<String> emptyList = ImmutableList.empty();
            assertSame(emptyList, list.skip(3));
            assertSame(emptyList, list.skip(4));
            assertSame(emptyList, list.skip(24));
        }))));
    }

    @Test
    public void testSpan() {
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
    public void testReduceFor1Item() {
        withString(value ->  {
            final ImmutableList<String> list = new ImmutableList.Builder<String>()
                    .add(value)
                    .build();

            final ReduceFunction<String> func = (l,r) -> {
                fail("Should not be called for a single item");
                return l;
            };

            assertEquals(list.get(0), list.reduce(func));
        });
    }

    @Test
    public void testReduceFor2Items() {
        withInt(a -> withInt(b -> {
            final ImmutableList<Integer> list = new ImmutableList.Builder<Integer>()
                    .add(a)
                    .add(b)
                    .build();

            final ReduceFunction<Integer> func = (l,r) -> l + r;
            final Integer result = list.reduce(func);
            assertEquals(func.apply(a, b), result);
        }));
    }

    @Test
    public void testReduceFor3Items() {
        withInt(a -> withInt(b -> withInt(c -> {
            final ImmutableList<String> list = new ImmutableList.Builder<String>()
                    .add(Integer.toString(a))
                    .add(Integer.toString(b))
                    .add(Integer.toString(c))
                    .build();

            final ReduceFunction<String> func = (l,r) -> l + ", " + r;
            final String result = list.reduce(func);
            assertEquals(func.apply(func.apply(Integer.toString(a), Integer.toString(b)), Integer.toString(c)), result);
        })));
    }

    @Test
    public void testAppendWhenEmpty() {
        withString(value -> {
            final ImmutableList<String> empty = ImmutableList.empty();
            final ImmutableList<String> list = empty.append(value);
            assertNotSame(empty, list);
            assertEquals(1, list.size());
            assertSame(value, list.get(0));
        });
    }

    @Test
    public void testAppendForASingleElement() {
        withString(a -> withString(value -> {
            final ImmutableList<String> initList = new ImmutableList.Builder<String>().append(a).build();
            final ImmutableList<String> list = initList.append(value);
            assertEquals(2, list.size());
            assertSame(a, list.get(0));
            assertSame(value, list.get(1));
        }));
    }

    @Test
    public void testAppendAllWhenBothEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        final ImmutableList<String> result = empty.appendAll(empty);
        assertSame(empty, result);
    }

    @Test
    public void testPrependWhenEmpty() {
        withString(value -> {
            final ImmutableList<String> empty = ImmutableList.empty();
            final ImmutableList<String> list = empty.prepend(value);
            assertNotSame(empty, list);
            assertEquals(1, list.size());
            assertSame(value, list.get(0));
        });
    }

    @Test
    public void testPrependForASingleElement() {
        withString(a -> withString(value -> {
            final ImmutableList<String> initList = new ImmutableList.Builder<String>().append(a).build();
            final ImmutableList<String> list = initList.prepend(value);
            assertEquals(2, list.size());
            assertSame(value, list.get(0));
            assertSame(a, list.get(1));
        }));
    }

    @Test
    public void testAppendANonEmptyListWhenEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        withString(value -> {
            final ImmutableList<String> list = new ImmutableList.Builder<String>().append(value).build();
            final ImmutableList<String> result = empty.appendAll(list);
            assertSame(list, result);
        });
    }

    @Test
    public void testAppendAnEmptyListWhenNoEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        withString(value -> {
            final ImmutableList<String> list = new ImmutableList.Builder<String>().append(value).build();
            final ImmutableList<String> result = list.appendAll(empty);
            assertSame(list, result);
        });
    }

    @Test
    public void testAppendAll() {
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
    public void testSort() {
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
    public void testGroupByWhenEmpty() {
        final Function<String, Integer> func = str -> {
            throw new AssertionError("This function should not be executed");
        };
        final ImmutableList<String> list = newBuilder().build();
        assertTrue(list.groupBy(func).isEmpty());
    }

    @Test
    public void testGroupBy() {
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
    public void testGroupByIntWhenEmpty() {
        final IntResultFunction<String> func = str -> {
            throw new AssertionError("This function should not be executed");
        };
        final ImmutableList<String> list = newBuilder().build();
        assertTrue(list.groupByInt(func).isEmpty());
    }

    @Test
    public void testGroupByInt() {
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
}

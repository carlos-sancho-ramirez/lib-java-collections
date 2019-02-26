package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.SortUtils.equal;

public final class ImmutableHashSetTest extends ImmutableSetTest<String, ImmutableHashSet.Builder<String>> {

    private static final String[] STRING_VALUES = {
            null, "", "_", "0", "abcd"
    };

    @Override
    void withValue(Procedure<String> procedure) {
        for (String str : STRING_VALUES) {
            procedure.apply(str);
        }
    }

    @Override
    boolean lessThan(String a, String b) {
        return b != null && (a == null || a.hashCode() < b.hashCode());
    }

    private boolean sortByLength(String a, String b) {
        return b != null && (a == null || a.length() < b.length());
    }

    @Override
    void withSortFunc(Procedure<SortFunction<String>> procedure) {
        procedure.apply(this::lessThan);
        procedure.apply(this::sortByLength);
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
    void withMapFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(this::prefixUnderscore);
        procedure.apply(this::charCounter);
    }

    @Override
    void withMapToIntFunc(Procedure<IntResultFunction<String>> procedure) {
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

    private void withGroupingFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(str -> Integer.toString(takeStringLength(str)));
    }

    private void withGroupingIntFunc(Procedure<IntResultFunction<String>> procedure) {
        procedure.apply(this::takeStringLength);
    }

    @Override
    void withBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableHashSet.Builder<String>>> procedure) {
        procedure.apply(ImmutableHashSet.Builder::new);
    }

    @Override
    ImmutableHashSet.Builder<String> newIterableBuilder() {
        return new ImmutableHashSet.Builder<>();
    }

    @Override
    <E> ImmutableHashSet<E> emptyCollection() {
        return ImmutableHashSet.empty();
    }

    @Test
    public void testIteratingForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().add(a).add(b).build();
            final Iterator<String> iterator = set.iterator();

            assertTrue(iterator.hasNext());
            final String first = iterator.next();
            final boolean sameValue = equal(a, b);

            if (sameValue) {
                assertEquals(a, first);
            }
            else if (equal(first, a)) {
                assertTrue(iterator.hasNext());
                assertEquals(b, iterator.next());
            }
            else if (equal(first, b)) {
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.next());
            }
            else {
                fail("Expected value " + a + " or " + b + " but iterator returned " + first);
            }

            assertFalse(iterator.hasNext());
        })));
    }

    @Test
    @Override
    public void testIndexOfForMultipleElements() {
        withValue(a -> withValue(b -> withValue(value -> {
            final int aHash = SortUtils.hashCode(a);
            final int bHash = SortUtils.hashCode(b);
            final boolean reversedOrder = bHash < aHash;
            final Traversable<String> set = newIterableBuilder().add(a).add(b).build();
            final int index = set.indexOf(value);

            if (aHash == bHash) {
                if (equal(a, value) || equal(b, value)) {
                    assertTrue(index == 0 || index == 1);
                }
                else {
                    assertEquals(-1, index);
                }
            }
            else {
                final int expectedIndex = (!reversedOrder && equal(a, value) || reversedOrder && equal(b, value)) ? 0 :
                        (!reversedOrder && equal(b, value) || reversedOrder && equal(a, value)) ? 1 : -1;
                assertEquals(expectedIndex, index);
            }
        })));
    }

    @Test
    @Override
    public void testFindFirstForMultipleElements() {
        withFilterFunc(f -> withValue(defaultValue -> withValue(a -> withValue(b -> {
            final int aHash = SortUtils.hashCode(a);
            final int bHash = SortUtils.hashCode(b);
            final boolean reversedOrder = bHash < aHash;
            final Traversable<String> collection = newIterableBuilder().add(a).add(b).build();
            final String first = collection.findFirst(f, defaultValue);

            if (aHash == bHash) {
                if (f.apply(a) || f.apply(b)) {
                    assertTrue(equal(a, first) || equal(b, first));
                }
                else {
                    assertSame(defaultValue, first);
                }
            }
            else {
                final String expected = (!reversedOrder && f.apply(a) || reversedOrder && !f.apply(b) && f.apply(a))? a :
                        (reversedOrder && f.apply(b) || !reversedOrder && !f.apply(a) && f.apply(b))? b : defaultValue;
                assertSame(expected, first);
            }
        }))));
    }

    @Test
    public void testToList() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().add(a).add(b).build();
            final ImmutableList<String> list = set.toList();

            if (equal(a, b)) {
                assertEquals(1, list.size());
                assertEquals(a, list.get(0));
            }
            else {
                assertEquals(2, list.size());

                if (equal(list.get(0), a)) {
                    assertEquals(b, list.get(1));
                }
                else {
                    assertEquals(a, list.get(1));
                    assertEquals(b, list.get(0));
                }
            }
        })));
    }

    @Test
    public void testGroupByWhenEmpty() {
        final Function<String, Integer> func = str -> {
            throw new AssertionError("This function should not be executed");
        };

        withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().build();
            assertTrue(set.groupBy(func).isEmpty());
        });
    }

    @Test
    public void testGroupBy() {
        withGroupingFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final String aGroup = func.apply(a);
            final String bGroup = func.apply(b);
            final String cGroup = func.apply(c);

            final ImmutableMap<String, ImmutableHashSet<String>> map = set.groupBy(func);
            if (aGroup.equals(bGroup)) {
                if (aGroup.equals(cGroup)) {
                    assertEquals(1, map.size());
                    assertEquals(aGroup, map.keyAt(0));
                    assertSame(set, map.valueAt(0));
                }
                else {
                    assertEquals(2, map.size());
                    if (aGroup.equals(map.keyAt(0))) {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(supplier.newBuilder().add(a).add(b).build(), map.valueAt(0));
                        assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(1));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(0));
                        assertEquals(aGroup, map.keyAt(1));
                        assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(0));
                        assertEquals(supplier.newBuilder().add(a).add(b).build(), map.valueAt(1));
                    }
                }
            }
            else if (aGroup.equals(cGroup)) {
                assertEquals(2, map.size());
                if (aGroup.equals(map.keyAt(0))) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertEquals(supplier.newBuilder().add(a).add(c).build(), map.valueAt(0));
                    assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(1));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(0));
                    assertEquals(supplier.newBuilder().add(a).add(c).build(), map.valueAt(1));
                }
            }
            else if (bGroup.equals(cGroup)) {
                assertEquals(2, map.size());
                if (aGroup.equals(map.keyAt(0))) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(0));
                    assertEquals(supplier.newBuilder().add(b).add(c).build(), map.valueAt(1));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertEquals(supplier.newBuilder().add(b).add(c).build(), map.valueAt(0));
                    assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(1));
                }
            }
            else {
                assertEquals(3, map.size());
                if (aGroup.equals(map.keyAt(0))) {
                    assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(0));
                    if (bGroup.equals(map.keyAt(1))) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(1));
                        assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(bGroup, map.keyAt(2));
                        assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(1));
                        assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(2));
                    }
                }
                else if (bGroup.equals(map.keyAt(0))) {
                    assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(0));
                    if (aGroup.equals(map.keyAt(1))) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(1));
                        assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(1));
                        assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(2));
                    }
                }
                else {
                    assertEquals(cGroup, map.keyAt(0));
                    assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(0));
                    if (aGroup.equals(map.keyAt(1))) {
                        assertEquals(bGroup, map.keyAt(2));
                        assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(1));
                        assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(bGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(1));
                        assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(2));
                    }
                }
            }
        })))));
    }

    @Test
    public void testGroupByIntWhenEmpty() {
        final IntResultFunction<String> func = str -> {
            throw new AssertionError("This function should not be executed");
        };

        withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().build();
            assertTrue(set.groupByInt(func).isEmpty());
        });
    }

    @Test
    public void testGroupByInt() {
        withGroupingIntFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int aGroup = func.apply(a);
            final int bGroup = func.apply(b);
            final int cGroup = func.apply(c);

            final ImmutableIntKeyMap<ImmutableHashSet<String>> map = set.groupByInt(func);
            if (aGroup == bGroup) {
                if (aGroup == cGroup) {
                    assertEquals(1, map.size());
                    assertEquals(aGroup, map.keyAt(0));
                    assertSame(set, map.valueAt(0));
                }
                else {
                    assertEquals(2, map.size());
                    if (aGroup == map.keyAt(0)) {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(supplier.newBuilder().add(a).add(b).build(), map.valueAt(0));
                        assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(1));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(0));
                        assertEquals(aGroup, map.keyAt(1));
                        assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(0));
                        assertEquals(supplier.newBuilder().add(a).add(b).build(), map.valueAt(1));
                    }
                }
            }
            else if (aGroup == cGroup) {
                assertEquals(2, map.size());
                if (aGroup == map.keyAt(0)) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertEquals(supplier.newBuilder().add(a).add(c).build(), map.valueAt(0));
                    assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(1));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(0));
                    assertEquals(supplier.newBuilder().add(a).add(c).build(), map.valueAt(1));
                }
            }
            else if (bGroup == cGroup) {
                assertEquals(2, map.size());
                if (aGroup == map.keyAt(0)) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(0));
                    assertEquals(supplier.newBuilder().add(b).add(c).build(), map.valueAt(1));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertEquals(supplier.newBuilder().add(b).add(c).build(), map.valueAt(0));
                    assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(1));
                }
            }
            else {
                assertEquals(3, map.size());
                if (aGroup == map.keyAt(0)) {
                    assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(0));
                    if (bGroup == map.keyAt(1)) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(1));
                        assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(bGroup, map.keyAt(2));
                        assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(1));
                        assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(2));
                    }
                }
                else if (bGroup == map.keyAt(0)) {
                    assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(0));
                    if (aGroup == map.keyAt(1)) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(1));
                        assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(1));
                        assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(2));
                    }
                }
                else {
                    assertEquals(cGroup, map.keyAt(0));
                    assertEquals(supplier.newBuilder().add(c).build(), map.valueAt(0));
                    if (aGroup == map.keyAt(1)) {
                        assertEquals(bGroup, map.keyAt(2));
                        assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(1));
                        assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(2));
                    }
                    else {
                        assertEquals(bGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertEquals(supplier.newBuilder().add(b).build(), map.valueAt(1));
                        assertEquals(supplier.newBuilder().add(a).build(), map.valueAt(2));
                    }
                }
            }
        })))));
    }
}

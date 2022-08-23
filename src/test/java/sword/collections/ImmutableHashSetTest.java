package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ImmutableHashSetTest extends ImmutableSetTest<String, ImmutableHashSet.Builder<String>> {

    private static final String[] STRING_VALUES = {
            null, "", "_", "0", "abcd"
    };

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableHashSet.Builder<String>>> procedure) {
        procedure.apply(ImmutableHashSet.Builder::new);
    }

    @Override
    public void withValue(Procedure<String> procedure) {
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

    private void withGroupingFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(str -> Integer.toString(takeStringLength(str)));
    }

    private void withGroupingIntFunc(Procedure<IntResultFunction<String>> procedure) {
        procedure.apply(this::takeStringLength);
    }

    @Test
    void testGroupByWhenEmpty() {
        final Function<String, Integer> func = str -> {
            throw new AssertionError("This function should not be executed");
        };

        withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().build();
            assertTrue(set.groupBy(func).isEmpty());
        });
    }

    @Test
    void testGroupBy() {
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
    void testGroupByIntWhenEmpty() {
        final IntResultFunction<String> func = str -> {
            throw new AssertionError("This function should not be executed");
        };

        withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().build();
            assertTrue(set.groupByInt(func).isEmpty());
        });
    }

    @Test
    void testGroupByInt() {
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

    @Test
    @Override
    public void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final String first = set.valueAt(0);
            final String second = (size >= 2)? set.valueAt(1) : null;
            final String third = (size >= 3)? set.valueAt(2) : null;

            final ImmutableHashSet<String> sliceA = set.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(first, sliceA.valueAt(0));

            final ImmutableHashSet<String> sliceB = set.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(second, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final ImmutableHashSet<String> sliceC = set.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(third, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final ImmutableHashSet<String> sliceAB = set.slice(new ImmutableIntRange(0, 1));
            if (size >= 2) {
                assertEquals(2, sliceAB.size());
                assertSame(second, sliceAB.valueAt(1));
            }
            else {
                assertEquals(1, sliceAB.size());
            }
            assertEquals(first, sliceAB.valueAt(0));

            final ImmutableHashSet<String> sliceBC = set.slice(new ImmutableIntRange(1, 2));
            if (size == 1) {
                assertEquals(0, sliceBC.size());
            }
            else if (size == 2) {
                assertEquals(1, sliceBC.size());
                assertSame(second, sliceBC.valueAt(0));
            }
            else {
                assertEquals(2, sliceBC.size());
                assertSame(second, sliceBC.valueAt(0));
                assertSame(third, sliceBC.valueAt(1));
            }

            final ImmutableHashSet<String> sliceABC = set.slice(new ImmutableIntRange(0, 2));
            assertEquals(size, sliceABC.size());
            assertSame(first, sliceABC.valueAt(0));
            if (size >= 2) {
                assertSame(second, sliceABC.valueAt(1));
                if (size >= 3) {
                    assertSame(third, sliceABC.valueAt(2));
                }
            }

            final ImmutableHashSet<String> sliceABCD = set.slice(new ImmutableIntRange(0, 3));
            assertEquals(size, sliceABCD.size());
            assertSame(first, sliceABCD.valueAt(0));
            if (size >= 2) {
                assertSame(second, sliceABCD.valueAt(1));
                if (size >= 3) {
                    assertSame(third, sliceABCD.valueAt(2));
                }
            }
        }))));
    }

    @Test
    @Override
    public void testSkip() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final String second = (size >= 2)? set.valueAt(1) : null;
            final String third = (size == 3)? set.valueAt(2) : null;

            assertSame(set, set.skip(0));

            final ImmutableHashSet<String> skip1 = set.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertSame(second, skip1.valueAt(0));
                if (size == 3) {
                    assertSame(third, skip1.valueAt(1));
                }
            }

            final ImmutableSet<String> empty = ImmutableHashSet.empty();
            final ImmutableHashSet<String> skip2 = set.skip(2);
            if (size == 3) {
                assertSame(third, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertSame(empty, skip2);
            }

            assertSame(empty, set.skip(3));
            assertSame(empty, set.skip(4));
            assertSame(empty, set.skip(24));
        }))));
    }

    @Test
    @Override
    public void testTakeWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().build();
            assertSame(set, set.take(0));
            assertSame(set, set.take(1));
            assertSame(set, set.take(2));
            assertSame(set, set.take(24));
        });
    }

    @Test
    @Override
    public void testTake() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final String first = set.valueAt(0);

            assertSame(ImmutableHashSet.empty(), set.take(0));

            final ImmutableHashSet<String> take1 = set.take(1);
            if (size > 1) {
                assertEquals(1, take1.size());
                assertSame(first, take1.valueAt(0));
            }
            else {
                assertSame(set, take1);
            }

            final ImmutableHashSet<String> take2 = set.take(2);
            if (size > 2) {
                assertEquals(2, take2.size());
                assertSame(first, take2.valueAt(0));
                assertSame(set.valueAt(1), take2.valueAt(1));
            }
            else {
                assertSame(set, take2);
            }

            assertSame(set, set.take(3));
            assertSame(set, set.take(4));
            assertSame(set, set.take(24));
        }))));
    }

    @Test
    void testSkipLastWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().build();
            assertSame(set, set.skipLast(0));
            assertSame(set, set.skipLast(1));
            assertSame(set, set.skipLast(2));
            assertSame(set, set.skipLast(24));
        });
    }

    @Test
    void testSkipLast() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableHashSet<String> set = supplier.newBuilder().add(a).add(b).add(c).build();
            assertSame(set, set.skipLast(0));

            final int size = set.size();
            final String first = set.valueAt(0);
            final String second = (size >= 2)? set.valueAt(1) : null;
            final ImmutableHashSet<String> empty = ImmutableHashSet.empty();

            final ImmutableHashSet<String> set1 = set.skipLast(1);
            if (size == 1) {
                assertSame(empty, set1);
            }
            else {
                assertEquals(size - 1, set1.size());
                assertSame(first, set1.valueAt(0));
                if (size == 3) {
                    assertSame(second, set1.valueAt(1));
                }
            }

            final ImmutableHashSet<String> set2 = set.skipLast(2);
            if (size < 3) {
                assertSame(empty, set2);
            }
            else {
                assertEquals(1, set2.size());
                assertSame(first, set2.valueAt(0));
            }

            assertSame(empty, set.skipLast(3));
            assertSame(empty, set.skipLast(4));
            assertSame(empty, set.skipLast(24));
        }))));
    }
}

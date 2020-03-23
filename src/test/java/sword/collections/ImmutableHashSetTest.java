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
}

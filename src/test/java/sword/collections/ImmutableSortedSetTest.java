package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public final class ImmutableSortedSetTest extends ImmutableSetTest<String, ImmutableSortedSet.Builder<String>> {

    private static final String[] STRING_VALUES = {
            null, "", "_", "0", "abcd"
    };

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableSortedSet.Builder<String>>> procedure) {
        withSortFunc(sortFunc -> {
            procedure.apply(() -> new ImmutableSortedSet.Builder<>(sortFunc));
        });
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

    @Test
    @Override
    public void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableSortedSet<String> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final String first = set.valueAt(0);
            final String second = (size >= 2)? set.valueAt(1) : null;
            final String third = (size >= 3)? set.valueAt(2) : null;

            final ImmutableSortedSet<String> sliceA = set.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(first, sliceA.valueAt(0));

            final ImmutableSortedSet<String> sliceB = set.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(second, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final ImmutableSortedSet<String> sliceC = set.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(third, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final ImmutableSortedSet<String> sliceAB = set.slice(new ImmutableIntRange(0, 1));
            if (size >= 2) {
                assertEquals(2, sliceAB.size());
                assertSame(second, sliceAB.valueAt(1));
            }
            else {
                assertEquals(1, sliceAB.size());
            }
            assertEquals(first, sliceAB.valueAt(0));

            final ImmutableSortedSet<String> sliceBC = set.slice(new ImmutableIntRange(1, 2));
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

            final ImmutableSortedSet<String> sliceABC = set.slice(new ImmutableIntRange(0, 2));
            assertEquals(size, sliceABC.size());
            assertSame(first, sliceABC.valueAt(0));
            if (size >= 2) {
                assertSame(second, sliceABC.valueAt(1));
                if (size >= 3) {
                    assertSame(third, sliceABC.valueAt(2));
                }
            }

            final ImmutableSortedSet<String> sliceABCD = set.slice(new ImmutableIntRange(0, 3));
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
    void testSkip() {
        withFilterFunc(f -> withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableSortedSet<String> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final String second = (size >= 2)? set.valueAt(1) : null;
            final String third = (size == 3)? set.valueAt(2) : null;

            assertSame(set, set.skip(0));

            final ImmutableSortedSet<String> skip1 = set.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertSame(second, skip1.valueAt(0));
                if (size == 3) {
                    assertSame(third, skip1.valueAt(1));
                }
            }

            final ImmutableSet<String> empty = supplier.newBuilder().build();
            final ImmutableSortedSet<String> skip2 = set.skip(2);
            if (size == 3) {
                assertSame(third, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertEquals(empty, skip2);
            }

            assertEquals(empty, set.skip(3));
            assertEquals(empty, set.skip(4));
            assertEquals(empty, set.skip(24));
        })))));
    }
}

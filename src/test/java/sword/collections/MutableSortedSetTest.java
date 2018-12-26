package sword.collections;

public final class MutableSortedSetTest extends MutableSetTest<String> {

    private static final String[] STRING_VALUES = {
            null, "", "_", "0", "abcd"
    };

    @Override
    void withValue(Procedure<String> procedure) {
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
    void withMapFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(this::prefixUnderscore);
        procedure.apply(this::charCounter);
    }

    private boolean filterFunc(String value) {
        return value != null && !value.isEmpty();
    }

    @Override
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::filterFunc);
    }

    @Override
    MutableSortedSet.Builder<String> newBuilder() {
        return new MutableSortedSet.Builder<>(this::lessThan);
    }

    @Override
    MutableSortedSet.Builder<String> newIterableBuilder() {
        return newBuilder();
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

    public void testHashCodeAndEquals() {
        withValue(a -> withValue(b -> {
            final MutableSet<String> set = newBuilder().add(a).add(b).build();
            final MutableSet<String> set1 = new MutableSortedSet.Builder<>(this::lessThan).add(a).add(b).build();
            final MutableSet<String> set2 = new MutableSortedSet.Builder<>(this::sortByLength).add(a).add(b).build();
            final MutableSet<String> set3 = new MutableHashSet.Builder<String>().add(a).add(b).build();

            assertEquals(set.hashCode(), set1.hashCode());
            assertEquals(set.hashCode(), set2.hashCode());
            assertEquals(set.hashCode(), set3.hashCode());

            assertEquals(set, set1);
            assertEquals(set, set2);
            assertEquals(set, set3);

            assertEquals(set.hashCode(), set1.mutate().hashCode());
            assertEquals(set.hashCode(), set2.mutate().hashCode());
            assertEquals(set.hashCode(), set3.mutate().hashCode());

            assertEquals(set, set1.mutate());
            assertEquals(set, set2.mutate());
            assertEquals(set, set3.mutate());
        }));
    }
}

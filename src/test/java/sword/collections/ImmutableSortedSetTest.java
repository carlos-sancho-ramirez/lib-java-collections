package sword.collections;

import static org.junit.jupiter.api.Assertions.assertFalse;

public final class ImmutableSortedSetTest extends ImmutableSetTest<String, ImmutableSortedSet.Builder<String>> {

    private static final String[] STRING_VALUES = {
            null, "", "_", "0", "abcd"
    };

    @Override
    public void withTransformableBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableTransformableBuilder<String>>> procedure) {
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

    @Override
    boolean lessThan(String a, String b) {
        return b != null && (a == null || a.hashCode() < b.hashCode());
    }

    @Override
    void withBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableSortedSet.Builder<String>>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new ImmutableSortedSet.Builder<>(sortFunc)));
    }

    @Override
    ImmutableSortedSet.Builder<String> newIterableBuilder() {
        return new ImmutableSortedSet.Builder<>(this::lessThan);
    }

    private boolean sortByLength(String a, String b) {
        return b != null && (a == null || a.length() < b.length());
    }

    @Override
    void withSortFunc(Procedure<SortFunction<String>> procedure) {
        procedure.apply(this::lessThan);
        procedure.apply(this::sortByLength);
    }

    @Override
    <E> ImmutableHashSet<E> emptyCollection() {
        return ImmutableHashSet.empty();
    }

    @Override
    void assertEmptyCollection(Transformable<String> collection) {
        assertFalse(collection.iterator().hasNext());
    }
}

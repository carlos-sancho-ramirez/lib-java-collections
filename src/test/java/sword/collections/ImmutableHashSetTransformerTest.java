package sword.collections;

import static sword.collections.TestUtils.withString;

final class ImmutableHashSetTransformerTest extends TransformerTest<String, ImmutableHashSet.Builder<String>> {

    @Override
    void withMapToIntFunc(Procedure<IntResultFunction<String>> procedure) {
        procedure.apply(SortUtils::hashCode);
    }

    @Override
    void withBuilder(Procedure<ImmutableHashSet.Builder<String>> procedure) {
        procedure.apply(new ImmutableHashSet.Builder<>());
    }

    @Override
    void withValue(Procedure<String> procedure) {
        withString(procedure);
    }

    @Override
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(SortUtils::isEmpty);
    }

    @Override
    void withMapFunc(Procedure<Function<String, Object>> procedure) {
        procedure.apply(str -> (str != null)? "_" + str : null);
    }

    private static String reduceFunc(String left, String right) {
        return String.valueOf(left) + '-' + String.valueOf(right);
    }

    @Override
    void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
        procedure.apply(ImmutableHashSetTransformerTest::reduceFunc);
    }
}

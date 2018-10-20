package sword.collections;

import static sword.collections.TestUtils.withString;

public final class ImmutableHashSetTraverserTest extends TransformerTest<String, ImmutableHashSet.Builder<String>> {

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

    private static String reduceFunc(String left, String right) {
        return String.valueOf(left) + '-' + String.valueOf(right);
    }

    @Override
    void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
        procedure.apply(ImmutableHashSetTraverserTest::reduceFunc);
    }
}

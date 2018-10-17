package sword.collections;

import static sword.collections.TestUtils.withString;

public final class ImmutableListTraverserTest extends TraverserTest<String, ImmutableList.Builder<String>> {

    @Override
    void withBuilder(Procedure<ImmutableList.Builder<String>> procedure) {
        procedure.apply(new ImmutableList.Builder<>());
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
        procedure.apply(ImmutableListTraverserTest::reduceFunc);
    }
}

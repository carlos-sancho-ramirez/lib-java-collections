package sword.collections;

import static sword.collections.TestUtils.withString;

final class MutableListTraverserTest extends TraverserTest<String, MutableList.Builder<String>> {

    @Override
    void withBuilder(Procedure<MutableList.Builder<String>> procedure) {
        procedure.apply(new MutableList.Builder<>());
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
        procedure.apply(MutableListTraverserTest::reduceFunc);
    }
}

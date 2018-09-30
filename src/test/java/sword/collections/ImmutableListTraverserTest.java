package sword.collections;

import static sword.collections.TestUtils.withString;

public final class ImmutableListTraverserTest extends TraverserTest<String> {

    @Override
    CollectionBuilder<String> newIterableBuilder() {
        return new ImmutableList.Builder<>();
    }

    @Override
    void withValue(Procedure<String> procedure) {
        withString(procedure);
    }

    @Override
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(SortUtils::isEmpty);
    }
}

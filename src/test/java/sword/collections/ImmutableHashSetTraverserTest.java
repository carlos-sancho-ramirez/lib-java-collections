package sword.collections;

import static sword.collections.TestUtils.withString;

public final class ImmutableHashSetTraverserTest extends TraverserTest<String> {

    @Override
    CollectionBuilder<String> newIterableBuilder() {
        return new ImmutableHashSet.Builder<>();
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

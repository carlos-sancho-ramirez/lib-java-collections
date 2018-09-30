package sword.collections;

import static sword.collections.TestUtils.withString;

public final class ImmutableMapTraverserTest extends TraverserTest<String> {

    @Override
    void withBuilder(Procedure<CollectionBuilder<String>> procedure) {
        procedure.apply(new SameKeyAndValueBuilder());
        procedure.apply(new IndexedKeyBuilder());
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
        procedure.apply(ImmutableMapTraverserTest::reduceFunc);
    }

    private static final class SameKeyAndValueBuilder implements CollectionBuilder<String> {
        private final ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();

        @Override
        public CollectionBuilder<String> add(String element) {
            builder.put(element, element);
            return this;
        }

        @Override
        public IterableCollection<String> build() {
            return builder.build();
        }
    }

    private static final class IndexedKeyBuilder implements CollectionBuilder<String> {
        private final ImmutableMap.Builder<Integer, String> builder = new ImmutableMap.Builder<>();
        private int key;

        @Override
        public CollectionBuilder<String> add(String element) {
            builder.put(key++, element);
            return this;
        }

        @Override
        public IterableCollection<String> build() {
            return builder.build();
        }
    }
}

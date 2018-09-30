package sword.collections;

import static sword.collections.TestUtils.withString;

public final class ImmutableIntKeyMapTraverserTest extends TraverserTest<String> {

    @Override
    void withBuilder(Procedure<CollectionBuilder<String>> procedure) {
        procedure.apply(new HashKeyBuilder());
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
        procedure.apply(ImmutableIntKeyMapTraverserTest::reduceFunc);
    }

    private static final class HashKeyBuilder implements CollectionBuilder<String> {
        private final ImmutableIntKeyMap.Builder<String> builder = new ImmutableIntKeyMap.Builder<>();

        @Override
        public CollectionBuilder<String> add(String element) {
            builder.put(SortUtils.hashCode(element), element);
            return this;
        }

        @Override
        public IterableCollection<String> build() {
            return builder.build();
        }
    }

    private static final class IndexedKeyBuilder implements CollectionBuilder<String> {
        private final ImmutableIntKeyMap.Builder<String> builder = new ImmutableIntKeyMap.Builder<>();
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

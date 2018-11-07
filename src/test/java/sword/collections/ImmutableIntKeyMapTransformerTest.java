package sword.collections;

import static sword.collections.TestUtils.withString;

public final class ImmutableIntKeyMapTransformerTest extends TransformerTest<String, TransformableBuilder<String>> {

    @Override
    void withBuilder(Procedure<TransformableBuilder<String>> procedure) {
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
        procedure.apply(ImmutableIntKeyMapTransformerTest::reduceFunc);
    }

    @Override
    void withMapToIntFunc(Procedure<IntResultFunction<String>> procedure) {
        procedure.apply(SortUtils::hashCode);
    }

    @Override
    void withMapFunc(Procedure<Function<String, Object>> procedure) {
        procedure.apply(str -> (str != null)? "_" + str : null);
    }

    private static final class HashKeyBuilder implements TransformableBuilder<String> {
        private final ImmutableIntKeyMap.Builder<String> builder = new ImmutableIntKeyMap.Builder<>();

        @Override
        public TransformableBuilder<String> add(String element) {
            builder.put(SortUtils.hashCode(element), element);
            return this;
        }

        @Override
        public Transformable<String> build() {
            return builder.build();
        }
    }

    private static final class IndexedKeyBuilder implements TransformableBuilder<String> {
        private final ImmutableIntKeyMap.Builder<String> builder = new ImmutableIntKeyMap.Builder<>();
        private int key;

        @Override
        public TransformableBuilder<String> add(String element) {
            builder.put(key++, element);
            return this;
        }

        @Override
        public Transformable<String> build() {
            return builder.build();
        }
    }
}

package sword.collections;

import static sword.collections.TestUtils.withString;

public final class ImmutableMapTraverserTest extends TransformerTest<String, TransformableBuilder<String>> {

    @Override
    void withBuilder(Procedure<TransformableBuilder<String>> procedure) {
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

    private static final class SameKeyAndValueBuilder implements TransformableBuilder<String> {
        private final ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();

        @Override
        public TransformableBuilder<String> add(String element) {
            builder.put(element, element);
            return this;
        }

        @Override
        public Transformable<String> build() {
            return builder.build();
        }
    }

    private static final class IndexedKeyBuilder implements TransformableBuilder<String> {
        private final ImmutableMap.Builder<Integer, String> builder = new ImmutableMap.Builder<>();
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

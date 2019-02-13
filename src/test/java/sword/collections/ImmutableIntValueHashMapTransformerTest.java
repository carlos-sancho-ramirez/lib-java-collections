package sword.collections;

public final class ImmutableIntValueHashMapTransformerTest extends IntTransformerTest<ImmutableIntValueHashMap, IntTraversableBuilder<ImmutableIntValueHashMap>> {

    @Override
    void withBuilder(Procedure<IntTraversableBuilder<ImmutableIntValueHashMap>> procedure) {
        procedure.apply(new SameKeyAndValueBuilder());
        procedure.apply(new IndexedKeyBuilder());
    }

    @Override
    void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v);
        procedure.apply(v -> v * v);
        procedure.apply(v -> -v - 1);
    }

    @Override
    void withMapFunc(Procedure<IntFunction<Object>> procedure) {
        procedure.apply(Integer::toString);
    }

    private static final class SameKeyAndValueBuilder implements IntTraversableBuilder<ImmutableIntValueHashMap> {
        private final ImmutableIntValueHashMap.Builder<String> builder = new ImmutableIntValueHashMap.Builder<>();

        @Override
        public SameKeyAndValueBuilder add(int element) {
            builder.put(Integer.toString(element), element);
            return this;
        }

        @Override
        public ImmutableIntValueHashMap build() {
            return builder.build();
        }
    }

    private static final class IndexedKeyBuilder implements IntTraversableBuilder<ImmutableIntValueHashMap> {
        private final ImmutableIntValueHashMap.Builder<Integer> builder = new ImmutableIntValueHashMap.Builder<>();
        private int key;

        @Override
        public IndexedKeyBuilder add(int element) {
            builder.put(key++, element);
            return this;
        }

        @Override
        public ImmutableIntValueHashMap build() {
            return builder.build();
        }
    }
}
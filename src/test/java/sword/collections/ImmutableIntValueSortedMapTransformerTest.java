package sword.collections;

final class ImmutableIntValueSortedMapTransformerTest extends IntTransformerTest<ImmutableIntTransformableBuilder> {

    @Override
    void withBuilder(Procedure<ImmutableIntTransformableBuilder> procedure) {
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

    private static final class SameKeyAndValueBuilder implements ImmutableIntTransformableBuilder {
        private final ImmutableIntValueSortedMap.Builder<String> builder = new ImmutableIntValueSortedMap.Builder<>(SortUtils::compareCharSequenceByUnicode);

        @Override
        public SameKeyAndValueBuilder add(int element) {
            builder.put(Integer.toString(element), element);
            return this;
        }

        @Override
        public ImmutableIntValueSortedMap build() {
            return builder.build();
        }
    }

    private static final class IndexedKeyBuilder implements ImmutableIntTransformableBuilder {
        private final ImmutableIntValueSortedMap.Builder<Integer> builder = new ImmutableIntValueSortedMap.Builder<>((a, b) -> a > b);
        private int key;

        @Override
        public IndexedKeyBuilder add(int element) {
            builder.put(key++, element);
            return this;
        }

        @Override
        public ImmutableIntValueSortedMap build() {
            return builder.build();
        }
    }
}

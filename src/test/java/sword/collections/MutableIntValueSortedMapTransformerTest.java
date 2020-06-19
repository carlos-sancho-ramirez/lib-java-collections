package sword.collections;

final class MutableIntValueSortedMapTransformerTest extends IntTransformerTest<MutableIntTransformableBuilder> {

    @Override
    void withBuilder(Procedure<MutableIntTransformableBuilder> procedure) {
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

    private static final class SameKeyAndValueBuilder implements MutableIntTransformableBuilder {
        private final MutableIntValueSortedMap.Builder<String> builder = new MutableIntValueSortedMap.Builder<>(SortUtils::compareCharSequenceByUnicode);

        @Override
        public SameKeyAndValueBuilder add(int element) {
            builder.put(Integer.toString(element), element);
            return this;
        }

        @Override
        public MutableIntValueSortedMap build() {
            return builder.build();
        }
    }

    private static final class IndexedKeyBuilder implements MutableIntTransformableBuilder {
        private final MutableIntValueSortedMap.Builder<Integer> builder = new MutableIntValueSortedMap.Builder<>((a, b) -> a > b);
        private int key;

        @Override
        public IndexedKeyBuilder add(int element) {
            builder.put(key++, element);
            return this;
        }

        @Override
        public MutableIntValueSortedMap build() {
            return builder.build();
        }
    }
}

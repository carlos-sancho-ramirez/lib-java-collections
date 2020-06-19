package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MutableIntValueHashMapTransformerTest extends IntTransformerTest<MutableIntTransformableBuilder> {

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

    @Test
    void testToMapWhenEmpty() {
        final ImmutableIntValueHashMap map = new ImmutableIntValueHashMap.Builder().build();
        assertTrue(map.iterator().toMap().isEmpty());
    }

    @Test
    void testToMapForSingleElement() {
        withValue(value -> {
            final ImmutableIntValueHashMap<String> transformable = new ImmutableIntValueHashMap.Builder<String>()
                    .put(Integer.toString(value), value)
                    .build();

            assertEquals(transformable, transformable.iterator().toMap());
        });
    }

    @Test
    void testToMapForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntValueHashMap<String> transformable = new ImmutableIntValueHashMap.Builder<String>()
                    .put(Integer.toString(a), a)
                    .put(Integer.toString(b), b)
                    .put(Integer.toString(c), c)
                    .build();

            assertEquals(transformable, transformable.iterator().toMap());
        })));
    }

    private static final class SameKeyAndValueBuilder implements MutableIntTransformableBuilder {
        private final MutableIntValueHashMap.Builder<String> builder = new MutableIntValueHashMap.Builder<>();

        @Override
        public SameKeyAndValueBuilder add(int element) {
            builder.put(Integer.toString(element), element);
            return this;
        }

        @Override
        public MutableIntValueHashMap build() {
            return builder.build();
        }
    }

    private static final class IndexedKeyBuilder implements MutableIntTransformableBuilder {
        private final MutableIntValueHashMap.Builder<Integer> builder = new MutableIntValueHashMap.Builder<>();
        private int key;

        @Override
        public IndexedKeyBuilder add(int element) {
            builder.put(key++, element);
            return this;
        }

        @Override
        public MutableIntValueHashMap build() {
            return builder.build();
        }
    }
}

package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ImmutableIntValueHashMapTransformerTest extends IntTransformerTest<ImmutableIntTransformableBuilder> {

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

    @Test
    public void testToMapWhenEmpty() {
        final ImmutableIntValueHashMap map = new ImmutableIntValueHashMap.Builder().build();
        assertTrue(map.iterator().toMap().isEmpty());
    }

    @Test
    public void testToMapForSingleElement() {
        withValue(value -> {
            final ImmutableIntValueHashMap<String> transformable = new ImmutableIntValueHashMap.Builder<String>()
                    .put(Integer.toString(value), value)
                    .build();

            assertEquals(transformable, transformable.iterator().toMap());
        });
    }

    @Test
    public void testToMapForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntValueHashMap<String> transformable = new ImmutableIntValueHashMap.Builder<String>()
                    .put(Integer.toString(a), a)
                    .put(Integer.toString(b), b)
                    .put(Integer.toString(c), c)
                    .build();

            assertEquals(transformable, transformable.iterator().toMap());
        })));
    }

    private static final class SameKeyAndValueBuilder implements ImmutableIntTransformableBuilder {
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

    private static final class IndexedKeyBuilder implements ImmutableIntTransformableBuilder {
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

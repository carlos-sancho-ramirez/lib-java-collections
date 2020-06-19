package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withString;

public final class MutableHashMapTransformerTest extends TransformerTest<String, TransformableBuilder<String>> {

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
        procedure.apply(MutableHashMapTransformerTest::reduceFunc);
    }

    @Override
    void withMapFunc(Procedure<Function<String, Object>> procedure) {
        procedure.apply(str -> (str != null)? "_" + str : null);
    }

    @Override
    void withMapToIntFunc(Procedure<IntResultFunction<String>> procedure) {
        procedure.apply(SortUtils::hashCode);
    }

    private void withMapBuilder(Procedure<MapBuilder<String, String>> procedure) {
        procedure.apply(new ImmutableHashMap.Builder<>());
    }

    private String keyFromValue(String value) {
        return "_" + value;
    }

    private static final class SameKeyAndValueBuilder implements TransformableBuilder<String> {
        private final MutableHashMap.Builder<String, String> builder = new MutableHashMap.Builder<>();

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
        private final MutableHashMap.Builder<Integer, String> builder = new MutableHashMap.Builder<>();
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

    @Test
    void testToMapWhenEmpty() {
        withMapBuilder(builder -> assertTrue(builder.build().iterator().toMap().isEmpty()));
    }

    @Test
    void testToMap() {
        withValue(a -> withValue(b -> withValue(c -> withMapBuilder(builder -> {
            final Map<String, String> map = builder
                    .put(keyFromValue(a), a)
                    .put(keyFromValue(b), b)
                    .put(keyFromValue(c), c)
                    .build();

            assertEquals(map, map.iterator().toMap());
        }))));
    }

    @Test
    @Override
    public void testMapForSingleValue() {
        withMapFunc(func -> withValue(a -> withMapBuilder(builder -> {
            final String keyA = keyFromValue(a);
            final Map<String, String> map = builder
                    .put(keyA, a)
                    .build();

            final TransformerWithKey<String, Object> transformer = map.iterator().map(func);
            assertTrue(transformer.hasNext());
            assertEquals(func.apply(a), transformer.next());
            assertSame(keyA, transformer.key());
            assertFalse(transformer.hasNext());
        })));
    }

    @Test
    @Override
    public void testMapForMultipleValues() {
        withMapFunc(func -> withValue(a -> withValue(b -> withValue(c -> withMapBuilder(builder -> {
            final Map<String, String> transformable = builder
                    .put(keyFromValue(a), a)
                    .put(keyFromValue(b), b)
                    .put(keyFromValue(c), c)
                    .build();
            final TransformerWithKey<String, Object> transformer = transformable.iterator().map(func);
            final int length = transformable.size();
            for (int i = 0; i < length; i++) {
                final String key = transformable.keyAt(i);
                final String value = transformable.valueAt(i);

                assertTrue(transformer.hasNext());
                assertEquals(func.apply(value), transformer.next());
                assertSame(key, transformer.key());
            }

            assertFalse(transformer.hasNext());
        })))));
    }
}

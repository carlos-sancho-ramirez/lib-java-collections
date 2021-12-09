package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class ImmutableSortedMapTest extends MapTest<Integer, String, ImmutableTransformableBuilder<String>> implements ImmutableMapTest<Integer, String, ImmutableTransformableBuilder<String>> {

    private static boolean sortInDescendantOrder(int a, int b) {
        return b > a;
    }

    @Override
    public ImmutableSortedMap.Builder<Integer, String> newBuilder() {
        return new ImmutableSortedMap.Builder<>(ImmutableSortedMapTest::sortInDescendantOrder);
    }

    @Override
    public void withKey(Procedure<Integer> procedure) {
        withInt(procedure::apply);
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableTransformableBuilder<String>>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new HashCodeKeyTransformableBuilder(sortFunc)));
    }

    @Override
    public void withValue(Procedure<String> procedure) {
        withString(procedure);
    }

    private String prefixUnderscore(String value) {
        return "_" + value;
    }

    private String charCounter(String value) {
        final int length = (value != null)? value.length() : 0;
        return Integer.toString(length);
    }

    @Override
    public void withMapFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(this::prefixUnderscore);
        procedure.apply(this::charCounter);
    }

    @Override
    public void withMapToIntFunc(Procedure<IntResultFunction<String>> procedure) {
        procedure.apply(str -> (str == null)? 0 : str.hashCode());
    }

    @Override
    void withSortFunc(Procedure<SortFunction<Integer>> procedure) {
        procedure.apply((a, b) -> a < b);
        procedure.apply((a, b) -> a > b);
    }

    @Override
    void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
        procedure.apply((a, b) -> a + b);
    }

    @Override
    String getTestValue() {
        return "value";
    }

    @Override
    Integer keyFromInt(int value) {
        return value;
    }

    @Override
    public String valueFromKey(Integer key) {
        return (key == null)? null : Integer.toString(key);
    }

    @Override
    void withMapBuilderSupplier(Procedure<MapBuilderSupplier<Integer, String, MapBuilder<Integer, String>>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new ImmutableSortedMap.Builder<>(sortFunc)));
    }

    @Test
    void testToImmutableMethod() {
        withKey(a -> withKey(b -> {
            final ImmutableMap<Integer, String> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            assertSame(map, map.toImmutable());
        }));
    }

    @Test
    void testPutMethod() {
        withKey(a -> withKey(b -> withKey(key -> withValue(value -> {
            final ImmutableMap<Integer, String> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final boolean contained = map.containsKey(key);
            final ImmutableMap<Integer, String> newMap = map.put(key, value);

            if (!contained) {
                final ImmutableHashMap.Builder<Integer, String> builder = new ImmutableHashMap.Builder<>();
                for (Map.Entry<Integer, String> entry : map.entries()) {
                    builder.put(entry.key(), entry.value());
                }
                assertEquals(builder.put(key, value).build(), newMap);
            }
            else {
                assertSame(map, map.put(key, valueFromKey(key)));

                final ImmutableSet<Integer> keySet = map.keySet();
                assertEquals(keySet, newMap.keySet());

                for (Integer k : keySet) {
                    if (equal(k, key)) {
                        assertEquals(value, newMap.get(k));
                    }
                    else {
                        assertEquals(map.get(k), newMap.get(k));
                    }
                }
            }
        }))));
    }

    @Test
    void testPutAllMustReturnAnImmutableHashMap() {
        final ImmutableSortedMap<Integer, String> map = newBuilder().build();
        final ImmutableSortedMap<Integer, String> result = map.putAll(map);
        assertSame(result, map);
    }

    private boolean hashCodeIsEven(String value) {
        return value == null || (value.hashCode() & 1) == 0;
    }

    @Override
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::hashCodeIsEven);
    }

    private static final class HashCodeKeyTransformableBuilder implements ImmutableTransformableBuilder<String> {
        private final ImmutableSortedMap.Builder<Integer, String> builder;

        HashCodeKeyTransformableBuilder(SortFunction<Integer> sortFunc) {
            builder = new ImmutableSortedMap.Builder<>(sortFunc);
        }

        @Override
        public HashCodeKeyTransformableBuilder add(String element) {
            builder.put(SortUtils.hashCode(element), element);
            return this;
        }

        @Override
        public ImmutableSortedMap<Integer, String> build() {
            return builder.build();
        }
    }
}

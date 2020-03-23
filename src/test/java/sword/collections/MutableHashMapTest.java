package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class MutableHashMapTest extends MapTest<Integer, String, MutableTransformableBuilder<String>> implements MutableTraversableTest<String, MutableTransformableBuilder<String>> {

    @Override
    MutableHashMap.Builder<Integer, String> newBuilder() {
        return new MutableHashMap.Builder<>();
    }

    @Override
    void withKey(Procedure<Integer> procedure) {
        withInt(procedure::apply);
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, MutableTransformableBuilder<String>>> procedure) {
        procedure.apply(HashCodeKeyTraversableBuilder::new);
    }

    @Override
    public void withValue(Procedure<String> procedure) {
        withString(procedure);
    }

    @Override
    void withSortFunc(Procedure<SortFunction<Integer>> procedure) {
        procedure.apply((a, b) -> a < b);
        procedure.apply((a, b) -> a > b);
    }

    private boolean hashCodeIsEven(String value) {
        return value == null || (value.hashCode() & 1) == 0;
    }

    @Override
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::hashCodeIsEven);
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
    void withMapBuilderSupplier(Procedure<MapBuilderSupplier<Integer, String, MapBuilder<Integer, String>>> procedure) {
        procedure.apply(MutableHashMap.Builder::new);
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
    String valueFromKey(Integer key) {
        return (key == null)? null : Integer.toString(key);
    }

    @Test
    void testToImmutableMethod() {
        withKey(a -> withKey(b -> {
            MutableMap<Integer, String> map1 = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            ImmutableMap<Integer, String> map2 = map1.toImmutable();

            final Iterator<Map.Entry<Integer, String>> it1 = map1.entries().iterator();
            final Iterator<Map.Entry<Integer, String>> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());

                final Map.Entry<Integer, String> entry1 = it1.next();
                final Map.Entry<Integer, String> entry2 = it2.next();

                assertEquals(entry1.key(), entry2.key());
                assertEquals(entry1.value(), entry2.value());
            }
            assertFalse(it2.hasNext());

            map1.remove(b);
            assertFalse(map1.containsKey(b));
            assertTrue(map2.containsKey(b));
        }));
    }

    @Test
    void testHashCode() {
        withInt(a -> withInt(b -> withInt(c -> {
            final Map<Integer, String> mutable = newBuilder()
                    .put(a, Integer.toString(b))
                    .put(b, Integer.toString(c))
                    .put(c, Integer.toString(a))
                    .build();
            final Map<Integer, String> immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable.hashCode(), immutable.hashCode());
        })));
    }

    @Test
    void testEquals() {
        withInt(a -> withInt(b -> withInt(c -> {
            final Map<Integer, String> mutable = newBuilder()
                    .put(a, Integer.toString(b))
                    .put(b, Integer.toString(c))
                    .put(c, Integer.toString(a))
                    .build();
            final Map<Integer, String> immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable, immutable);
            assertEquals(immutable, mutable);
        })));
    }

    private static final class HashCodeKeyTraversableBuilder implements MutableTransformableBuilder<String> {
        private final MutableHashMap<Integer, String> map = MutableHashMap.empty();

        @Override
        public HashCodeKeyTraversableBuilder add(String element) {
            map.put(SortUtils.hashCode(element), element);
            return this;
        }

        @Override
        public MutableHashMap<Integer, String> build() {
            return map;
        }
    }
}

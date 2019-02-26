package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class MutableHashMapTest extends MapTest<Integer, String> implements MutableTraversableTest<String> {

    @Override
    MutableHashMap.Builder<Integer, String> newBuilder() {
        return new MutableHashMap.Builder<>();
    }

    @Override
    void withKey(Procedure<Integer> procedure) {
        withInt(procedure::apply);
    }

    @Override
    public MutableTraversableBuilder<String> newTraversableBuilder() {
        return new HashCodeKeyTraversableBuilder();
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

    @Override
    void withMapBuilderSupplier(Procedure<MapBuilderSupplier<Integer, String, MapBuilder<Integer, String>>> procedure) {
        procedure.apply(MutableHashMap.Builder::new);
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
    public void testToImmutableMethod() {
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
    public void testHashCode() {
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
    public void testEquals() {
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

    private static final class HashCodeKeyTraversableBuilder implements MutableTraversableBuilder<String> {
        private final MutableHashMap<Integer, String> map = MutableHashMap.empty();

        @Override
        public HashCodeKeyTraversableBuilder add(String element) {
            map.put(SortUtils.hashCode(element), element);
            return this;
        }

        @Override
        public MutableTraversable<String> build() {
            return map;
        }
    }
}

package sword.collections;

import java.util.Iterator;

import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public class MutableHashMapTest extends MapTest<Integer, String> {

    @Override
    MutableHashMap.Builder<Integer, String> newBuilder() {
        return new MutableHashMap.Builder<>();
    }

    @Override
    void withKey(Procedure<Integer> procedure) {
        withInt(procedure::apply);
    }

    @Override
    void withValue(Procedure<String> procedure) {
        withString(procedure);
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

    public void testClearWhenEmpty() {
        final MutableMap<Integer, String> map = newBuilder().build();
        assertFalse(map.clear());
        assertTrue(map.isEmpty());
    }

    public void testClearForSingleItem() {
        withInt(value -> {
            final MutableMap<Integer, String> map = newBuilder()
                    .put(value, Integer.toString(value))
                    .build();
            assertTrue(map.clear());
            assertTrue(map.isEmpty());
        });
    }

    public void testClearForMultipleItems() {
        withInt(a -> withInt(b -> {
            final MutableMap<Integer, String> map = newBuilder()
                    .put(a, Integer.toString(a))
                    .put(b, Integer.toString(b))
                    .build();
            assertTrue(map.clear());
            assertTrue(map.isEmpty());
        }));
    }
}

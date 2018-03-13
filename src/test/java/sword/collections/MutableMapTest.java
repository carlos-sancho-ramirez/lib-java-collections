package sword.collections;

import java.util.Iterator;

import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public class MutableMapTest extends MapTest<Integer, String> {

    @Override
    MutableMap.Builder<Integer, String> newBuilder() {
        return new MutableMap.Builder<>();
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

            final Iterator<Map.Entry<Integer, String>> it1 = map1.iterator();
            final Iterator<Map.Entry<Integer, String>> it2 = map2.iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());

                final Map.Entry<Integer, String> entry1 = it1.next();
                final Map.Entry<Integer, String> entry2 = it2.next();

                assertEquals(entry1.getKey(), entry2.getKey());
                assertEquals(entry1.getValue(), entry2.getValue());
            }
            assertFalse(it2.hasNext());

            map1.remove(b);
            assertFalse(map1.containsKey(b));
            assertTrue(map2.containsKey(b));
        }));
    }
}

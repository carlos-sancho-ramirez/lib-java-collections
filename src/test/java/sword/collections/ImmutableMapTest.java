package sword.collections;

import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public class ImmutableMapTest extends MapTest<Integer, String> {

    @Override
    ImmutableMap.Builder<Integer, String> newBuilder() {
        return new ImmutableMap.Builder<>();
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
            final ImmutableMap<Integer, String> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            assertSame(map, map.toImmutable());
        }));
    }

    public void testPutMethod() {
        withKey(a -> withKey(b -> withKey(key -> withValue(value -> {
            final ImmutableMap<Integer, String> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final boolean contained = map.containsKey(key);
            final ImmutableMap<Integer, String> newMap = map.put(key, value);

            if (!contained) {
                final ImmutableMap.Builder<Integer, String> builder = new ImmutableMap.Builder<>();
                for (Map.Entry<Integer, String> entry : map) {
                    builder.put(entry.getKey(), entry.getValue());
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

    public void testMapValues() {
        withKey(ka -> withKey(kb -> {
            final String va = valueFromKey(ka);
            final String vb = valueFromKey(kb);
            final ImmutableMap<Integer, String> map = newBuilder()
                    .put(ka, va)
                    .put(kb, vb)
                    .build();

            final Function<String, String> mapFunc = str -> (str != null)? "_" + str : "_";
            final ImmutableMap<Integer, String> map2 = map.mapValues(mapFunc);

            final ImmutableSet<Integer> keySet = map.keySet();
            assertEquals(keySet, map2.keySet());

            for (Integer key : keySet) {
                assertEquals(mapFunc.apply(map.get(key)), map2.get(key));
            }
        }));
    }
}

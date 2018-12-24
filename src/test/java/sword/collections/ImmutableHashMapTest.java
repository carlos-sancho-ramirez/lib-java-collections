package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class ImmutableHashMapTest extends MapTest<Integer, String> {

    @Override
    ImmutableHashMap.Builder<Integer, String> newBuilder() {
        return new ImmutableHashMap.Builder<>();
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
            final ImmutableHashMap<Integer, String> map = newBuilder()
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

                final ImmutableHashSet<Integer> keySet = map.keySet();
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

    private boolean hashCodeIsEven(String value) {
        return value == null || (value.hashCode() & 1) == 0;
    }

    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::hashCodeIsEven);
    }

    public void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableMap<Integer, String> map = newBuilder().build();
            assertSame(map, map.filter(f));
        });
    }

    public void testFilterForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final String value = Integer.toString(key);
            final ImmutableMap<Integer, String> map = newBuilder().put(key, value).build();
            final ImmutableMap<Integer, String> filtered = map.filter(f);

            final ImmutableMap<Integer, String> expected = f.apply(value)? map : newBuilder().build();
            assertSame(expected, filtered);
        }));
    }

    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withInt(a -> withInt(b -> {
            final Integer keyA = a;
            final Integer keyB = b;
            final String valueA = Integer.toString(keyA);
            final String valueB = Integer.toString(keyB);
            final ImmutableMap<Integer, String> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableMap<Integer, String> filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertSame(map, filtered);
            }
            else if (aPassed) {
                Iterator<Map.Entry<Integer, String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final Map.Entry<Integer, String> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<Map.Entry<Integer, String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final Map.Entry<Integer, String> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertSame(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(newBuilder().build(), filtered);
            }
        })));
    }

    public void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableMap<Integer, String> map = newBuilder().build();
            assertSame(map, map.filterNot(f));
        });
    }

    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final String value = Integer.toString(key);
            final ImmutableMap<Integer, String> map = newBuilder().put(key, value).build();
            final ImmutableMap<Integer, String> filtered = map.filterNot(f);

            final ImmutableMap<Integer, String> expected = f.apply(value)? newBuilder().build() : map;
            assertSame(expected, filtered);
        }));
    }

    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withInt(a -> withInt(b -> {
            final Integer keyA = a;
            final Integer keyB = b;
            final String valueA = Integer.toString(keyA);
            final String valueB = Integer.toString(keyB);
            final ImmutableMap<Integer, String> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableMap<Integer, String> filtered = map.filterNot(f);

            final boolean aRemoved = f.apply(valueA);
            final boolean bRemoved = f.apply(valueB);

            if (aRemoved && bRemoved) {
                assertSame(newBuilder().build(), filtered);
            }
            else if (aRemoved) {
                Iterator<Map.Entry<Integer, String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final Map.Entry<Integer, String> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertSame(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<Map.Entry<Integer, String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final Map.Entry<Integer, String> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(map, filtered);
            }
        })));
    }

    public void testMapValuesForIntResult() {
        withKey(ka -> withKey(kb -> {
            final String va = valueFromKey(ka);
            final String vb = valueFromKey(kb);
            final ImmutableHashMap<Integer, String> map = newBuilder()
                    .put(ka, va)
                    .put(kb, vb)
                    .build();

            final IntResultFunction<String> mapFunc = str -> (str != null)? str.hashCode() : 0;
            final ImmutableIntValueMap<Integer> map2 = map.map(mapFunc);

            final ImmutableHashSet<Integer> keySet = map.keySet();
            assertEquals(keySet, map2.keySet());

            for (Integer key : keySet) {
                assertEquals(mapFunc.apply(map.get(key)), map2.get(key));
            }
        }));
    }

    public void testMapValues() {
        withKey(ka -> withKey(kb -> {
            final String va = valueFromKey(ka);
            final String vb = valueFromKey(kb);
            final ImmutableHashMap<Integer, String> map = newBuilder()
                    .put(ka, va)
                    .put(kb, vb)
                    .build();

            final Function<String, String> mapFunc = str -> (str != null)? "_" + str : "_";
            final ImmutableMap<Integer, String> map2 = map.map(mapFunc);

            final ImmutableHashSet<Integer> keySet = map.keySet();
            assertEquals(keySet, map2.keySet());

            for (Integer key : keySet) {
                assertEquals(mapFunc.apply(map.get(key)), map2.get(key));
            }
        }));
    }
}

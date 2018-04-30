package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class ImmutableIntKeyMapTest extends IntKeyMapTest<String> {

    @Override
    ImmutableIntKeyMap.Builder<String> newMapBuilder() {
        return new ImmutableIntKeyMap.Builder<>();
    }

    @Override
    void withValue(Procedure<String> procedure) {
        withString(procedure);
    }

    private boolean filterFunc(String value) {
        return value != null && !value.isEmpty();
    }

    @Override
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::filterFunc);
    }

    private String reduceFunc(String left, String right) {
        return String.valueOf(left) + '-' + String.valueOf(right);
    }

    @Override
    void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
        procedure.apply(this::reduceFunc);
    }

    private String prefixUnderscore(String value) {
        return "_" + value;
    }

    private String charCounter(String value) {
        final int length = (value != null)? value.length() : 0;
        return Integer.toString(length);
    }

    @Override
    void withMapFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(this::prefixUnderscore);
        procedure.apply(this::charCounter);
    }

    @Override
    String getTestValue() {
        return "value";
    }

    @Override
    String getTestValue2() {
        return "value2";
    }

    @Override
    String valueForKey(int key) {
        return Integer.toString(key);
    }

    @SuppressWarnings("unchecked")
    private static final ImmutableIntKeyMap<String>[] IMMUTABLE_INT_KEY_MAP_VALUES = new ImmutableIntKeyMap[] {
            null,
            new ImmutableIntKeyMap.Builder<String>().build(),
            new ImmutableIntKeyMap.Builder<String>().put(0, "").build(),
            new ImmutableIntKeyMap.Builder<String>().put(124, "big number").build(),
            new ImmutableIntKeyMap.Builder<String>().put(-3, "_3").build(),
            new ImmutableIntKeyMap.Builder<String>().put(0, null).put(12234, "large").build(),
            new ImmutableIntKeyMap.Builder<String>().put(-34, "x").put(2, "x").put(Integer.MAX_VALUE, "xy").build()
    };

    private void withImmutableIntKeyMap(Procedure<ImmutableIntKeyMap<String>> action) {
        final int length = IMMUTABLE_INT_KEY_MAP_VALUES.length;
        for (int i = 0; i < length; i++) {
            action.apply(IMMUTABLE_INT_KEY_MAP_VALUES[i]);
        }
    }

    private String mapValueFunction(String str) {
        return (str != null)? "_" + str : null;
    }

    private int mapValueIntResultFunction(String str) {
        return (str != null)? str.hashCode() : 0;
    }

    public void testMapValuesMethod() {
        withInt(a -> withInt(b -> {
            final ImmutableIntKeyMap<String> map = new ImmutableIntKeyMap.Builder<String>()
                    .put(a, Integer.toString(a))
                    .put(b, Integer.toString(b))
                    .build();

            final ImmutableIntKeyMap<String> map2 = map.mapValues(this::mapValueFunction);
            assertEquals(map.size(), map2.size());
            assertEquals(map.keySet(), map2.keySet());

            for (int key : map.keySet()) {
                assertEquals(mapValueFunction(map.get(key)), map2.get(key));
            }
        }));
    }

    public void testMapValuesForIntResultMethod() {
        withInt(a -> withInt(b -> {
            final ImmutableIntKeyMap<String> map = new ImmutableIntKeyMap.Builder<String>()
                    .put(a, Integer.toString(a))
                    .put(b, Integer.toString(b))
                    .build();

            final ImmutableIntPairMap map2 = map.mapValues(this::mapValueIntResultFunction);
            assertEquals(map.size(), map2.size());
            assertEquals(map.keySet(), map2.keySet());

            for (int key : map.keySet()) {
                assertEquals(mapValueIntResultFunction(map.get(key)), map2.get(key));
            }
        }));
    }

    public void testPutMethod() {
        withImmutableIntKeyMap(array -> withInt(key -> withString(value -> {
            if (array != null) {
                boolean contained = false;
                for (int i = 0; i < array.size(); i++) {
                    if (array.keyAt(i) == key) {
                        contained = true;
                        break;
                    }
                }

                final ImmutableIntKeyMap<String> newArray = array.put(key, value);

                if (!contained) {
                    final ImmutableIntKeyMap.Builder<String> builder = new ImmutableIntKeyMap.Builder<>();
                    for (IntKeyMap.Entry<String> entry : array.entries()) {
                        builder.put(entry.key(), entry.value());
                    }
                    assertEquals(builder.put(key, value).build(), newArray);
                }
            }
        })));
    }

    public void testReverseMethod() {
        withImmutableIntKeyMap(array -> {
            if (array != null) {
                // Check if the array is reversable, so no duplicated values should be found
                final int length = array.size();
                boolean duplicated = false;
                for (int i = 0; i < length - 1; i++) {
                    for (int j = i + 1; j < length; j++) {
                        if (equal(array.valueAt(i), array.valueAt(j))) {
                            duplicated = true;
                        }
                        break;
                    }

                    if (duplicated) {
                        break;
                    }
                }

                if (!duplicated) {
                    final ImmutableIntValueMap<String> reversed = array.reverse();
                    assertEquals(length, reversed.size());

                    for (int i = 0; i < length; i++) {
                        assertEquals(reversed.keyAt(i), array.get(reversed.valueAt(i)));
                    }
                }
            }
        });
    }

    public void testKeySetWhenEmpty() {
        final ImmutableIntKeyMap<String> empty = ImmutableIntKeyMap.empty();
        assertSame(ImmutableIntSetImpl.empty(), empty.keySet());
    }

    public void testToImmutableForEmpty() {
        final ImmutableIntKeyMap.Builder<String> builder = newMapBuilder();
        final ImmutableIntKeyMap<String> map = builder.build();
        assertSame(map, map.toImmutable());
    }

    public void testMutateForEmpty() {
        final ImmutableIntKeyMap.Builder<String> builder = newMapBuilder();
        final ImmutableIntKeyMap<String> map1 = builder.build();
        final MutableIntKeyMap<String> map2 = map1.mutate();

        assertTrue(map2.isEmpty());

        map2.put(1, "");
        assertEquals(null, map1.get(1, null));
    }

    public void testToImmutable() {
        withInt(a -> withInt(b -> {
            final ImmutableIntKeyMap.Builder<String> builder = newMapBuilder();
            final ImmutableIntKeyMap<String> map1 = builder.put(a, "").put(b, "").build();
            final ImmutableIntKeyMap<String> map2 = map1.toImmutable();
            assertSame(map1, map2);
        }));
    }

    public void testMutate() {
        withInt(a -> withInt(b -> {
            final ImmutableIntKeyMap.Builder<String> builder = newMapBuilder();
            final ImmutableIntKeyMap<String> map1 = builder.put(a, "").put(b, "").build();
            final MutableIntKeyMap<String> map2 = map1.mutate();

            final Iterator<IntKeyMap.Entry<String>> it1 = map1.entries().iterator();
            final Iterator<IntKeyMap.Entry<String>> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                final IntKeyMap.Entry<String> item1 = it1.next();
                final IntKeyMap.Entry<String> item2 = it2.next();
                assertEquals(item1.key(), item2.key());
                assertEquals(item1.value(), item2.value());
            }
            assertFalse(it2.hasNext());

            map2.remove(b);
            assertEquals("", map1.get(b, null));
            assertEquals(null, map2.get(b, null));
        }));
    }
}

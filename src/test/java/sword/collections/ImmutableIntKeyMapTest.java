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

    private boolean stringIsEmpty(String value) {
        return value != null && !value.isEmpty();
    }

    private boolean hashCodeIsEven(String value) {
        return value == null || (value.hashCode() & 1) == 0;
    }

    @Override
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::stringIsEmpty);
        procedure.apply(this::hashCodeIsEven);
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

    public void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableIntKeyMap<String> map = newMapBuilder().build();
            assertSame(map, map.filter(f));
        });
    }

    public void testFilterForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final String value = Integer.toString(key);
            final ImmutableIntKeyMap<String> map = newMapBuilder().put(key, value).build();
            final ImmutableIntKeyMap<String> filtered = map.filter(f);

            final ImmutableIntKeyMap<String> expected = f.apply(value)? map : newMapBuilder().build();
            assertSame(expected, filtered);
        }));
    }

    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withInt(keyA -> withInt(keyB -> {
            final String valueA = Integer.toString(keyA);
            final String valueB = Integer.toString(keyB);
            final ImmutableIntKeyMap<String> map = newMapBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableIntKeyMap<String> filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertSame(map, filtered);
            }
            else if (aPassed) {
                Iterator<IntKeyMap.Entry<String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntKeyMap.Entry<String> entry = iterator.next();
                assertEquals(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntKeyMap.Entry<String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntKeyMap.Entry<String> entry = iterator.next();
                assertEquals(keyB, entry.key());
                assertSame(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(newMapBuilder().build(), filtered);
            }
        })));
    }

    public void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableIntKeyMap<String> map = newMapBuilder().build();
            assertSame(map, map.filterNot(f));
        });
    }

    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final String value = Integer.toString(key);
            final ImmutableIntKeyMap<String> map = newMapBuilder().put(key, value).build();
            final ImmutableIntKeyMap<String> filtered = map.filterNot(f);

            if (!f.apply(value)) {
                assertSame(map, filtered);
            }
            else {
                assertSame(newMapBuilder().build(), filtered);
            }
        }));
    }

    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withInt(keyA -> withInt(keyB -> {
            final String valueA = Integer.toString(keyA);
            final String valueB = Integer.toString(keyB);
            final ImmutableIntKeyMap<String> map = newMapBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableIntKeyMap<String> filtered = map.filterNot(f);

            final boolean aRemoved = f.apply(valueA);
            final boolean bRemoved = f.apply(valueB);

            if (aRemoved && bRemoved) {
                assertSame(newMapBuilder().build(), filtered);
            }
            else if (aRemoved) {
                Iterator<IntKeyMap.Entry<String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntKeyMap.Entry<String> entry = iterator.next();
                assertEquals(keyB, entry.key());
                assertSame(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<IntKeyMap.Entry<String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntKeyMap.Entry<String> entry = iterator.next();
                assertEquals(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(map, filtered);
            }
        })));
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

            final ImmutableIntKeyMap<String> map2 = map.map(this::mapValueFunction);
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

            final ImmutableIntPairMap map2 = map.mapToInt(this::mapValueIntResultFunction);
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

    public void testInvertMethod() {
        withImmutableIntKeyMap(array -> {
            if (array != null) {
                // Check if the array is invertible, so no duplicated values should be found
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
                    final ImmutableIntValueMap<String> inverted = array.invert();
                    assertEquals(length, inverted.size());

                    for (int i = 0; i < length; i++) {
                        assertEquals(inverted.keyAt(i), array.get(inverted.valueAt(i)));
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

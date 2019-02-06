package sword.collections;

import java.util.Iterator;

import static sword.collections.TestUtils.withInt;

public final class ImmutableIntPairMapTest extends IntPairMapTest {

    @Override
    ImmutableIntPairMap.Builder newBuilder() {
        return new ImmutableIntPairMap.Builder();
    }

    @SuppressWarnings("unchecked")
    private static final ImmutableIntPairMap[] IMMUTABLE_INT_PAIR_MAP_VALUEs = new ImmutableIntPairMap[] {
            null,
            new ImmutableIntPairMap.Builder().build(),
            new ImmutableIntPairMap.Builder().put(0, 0).build(),
            new ImmutableIntPairMap.Builder().put(0, -4).build(),
            new ImmutableIntPairMap.Builder().put(0, 6).build(),
            new ImmutableIntPairMap.Builder().put(Integer.MIN_VALUE, 0).build(),
            new ImmutableIntPairMap.Builder().put(Integer.MAX_VALUE, 0).build(),
            new ImmutableIntPairMap.Builder().put(124, 12).build(),
            new ImmutableIntPairMap.Builder().put(127, -17).build(),
            new ImmutableIntPairMap.Builder().put(125, 0).build(),
            new ImmutableIntPairMap.Builder().put(-3, 13).build(),
            new ImmutableIntPairMap.Builder().put(-45, 0).build(),
            new ImmutableIntPairMap.Builder().put(-42, -1).build(),
            new ImmutableIntPairMap.Builder().put(0, -4).put(12234, 12345).build(),
            new ImmutableIntPairMap.Builder().put(0, 4).put(1, 4).build(), // Intentionally no reversable
            new ImmutableIntPairMap.Builder().put(-34, -33).put(2, 3).put(Integer.MAX_VALUE, Integer.MIN_VALUE).build()
    };

    private void withImmutableSparseIntArray(Procedure<ImmutableIntPairMap> procedure) {
        final int length = IMMUTABLE_INT_PAIR_MAP_VALUEs.length;
        for (int i = 0; i < length; i++) {
            procedure.apply(IMMUTABLE_INT_PAIR_MAP_VALUEs[i]);
        }
    }

    private boolean valueIsEven(int value) {
        return (value & 1) == 0;
    }

    void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::valueIsEven);
    }

    public void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableIntPairMap map = newBuilder().build();
            assertSame(map, map.filter(f));
        });
    }

    public void testFilterForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final int value = key * key;
            final ImmutableIntPairMap map = newBuilder().put(key, value).build();
            final ImmutableIntPairMap filtered = map.filter(f);

            if (f.apply(value)) {
                assertSame(map, filtered);
            }
            else {
                assertSame(newBuilder().build(), filtered);
            }
        }));
    }

    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withInt(keyA -> withInt(keyB -> {
            final int valueA = keyA * keyA;
            final int valueB = keyB * keyB;
            final ImmutableIntPairMap map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableIntPairMap filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertSame(map, filtered);
            }
            else if (aPassed) {
                Iterator<IntPairMap.Entry> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntPairMap.Entry entry = iterator.next();
                assertEquals(keyA, entry.key());
                assertEquals(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntPairMap.Entry> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntPairMap.Entry entry = iterator.next();
                assertEquals(keyB, entry.key());
                assertEquals(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(newBuilder().build(), filtered);
            }
        })));
    }

    public void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableIntPairMap map = newBuilder().build();
            assertSame(map, map.filterNot(f));
        });
    }

    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final int value = key * key;
            final ImmutableIntPairMap map = newBuilder().put(key, value).build();
            final ImmutableIntPairMap filtered = map.filterNot(f);

            if (!f.apply(value)) {
                assertSame(map, filtered);
            }
            else {
                assertSame(newBuilder().build(), filtered);
            }
        }));
    }

    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withInt(keyA -> withInt(keyB -> {
            final int valueA = keyA * keyA;
            final int valueB = keyB * keyB;
            final ImmutableIntPairMap map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableIntPairMap filtered = map.filterNot(f);

            final boolean aRemoved = f.apply(valueA);
            final boolean bRemoved = f.apply(valueB);

            if (aRemoved && bRemoved) {
                assertSame(newBuilder().build(), filtered);
            }
            else if (aRemoved) {
                Iterator<IntPairMap.Entry> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntPairMap.Entry entry = iterator.next();
                assertEquals(keyB, entry.key());
                assertEquals(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<IntPairMap.Entry> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntPairMap.Entry entry = iterator.next();
                assertEquals(keyA, entry.key());
                assertEquals(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(map, filtered);
            }
        })));
    }

    private String mapValueFunction(int value) {
        return Integer.toString(value);
    }

    private int mapValueIntResultFunction(int value) {
        return value + 1;
    }

    public void testMapValuesMethod() {
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap map = new ImmutableIntPairMap.Builder()
                    .put(a, a)
                    .put(b, b)
                    .build();

            final ImmutableIntKeyMap<String> map2 = map.map(this::mapValueFunction);
            assertEquals(map.size(), map2.size());
            assertEquals(map.keySet(), map2.keySet());

            for (int key : map.keySet()) {
                assertEquals(mapValueFunction(map.get(key)), map2.get(key));
            }
        }));
    }

    public void testMapToIntMethod() {
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap map = new ImmutableIntPairMap.Builder()
                    .put(a, a)
                    .put(b, b)
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
        withImmutableSparseIntArray(array -> withInt(key -> withInt(value -> {
            if (array != null) {
                boolean contained = false;
                for (int i = 0; i < array.size(); i++) {
                    if (array.keyAt(i) == key) {
                        contained = true;
                        break;
                    }
                }

                final ImmutableIntPairMap newArray = array.put(key, value);

                if (!contained) {
                    final ImmutableIntPairMap.Builder builder = new ImmutableIntPairMap.Builder();
                    for (IntPairMap.Entry entry : array.entries()) {
                        builder.put(entry.key(), entry.value());
                    }
                    assertEquals(builder.put(key, value).build(), newArray);
                }
            }
        })));
    }

    public void testReverseMethod() {
        withImmutableSparseIntArray(array -> {
            if (array != null) {
                // Check if the array is reversable, so no duplicated values should be found
                final int length = array.size();
                boolean duplicated = false;
                for (int i = 0; i < length - 1; i++) {
                    for (int j = i + 1; j < length; j++) {
                        if (array.valueAt(i) == array.valueAt(j)) {
                            duplicated = true;
                        }
                        break;
                    }

                    if (duplicated) {
                        break;
                    }
                }

                if (!duplicated) {
                    final ImmutableIntPairMap reversed = array.reverse();
                    assertEquals(length, reversed.size());

                    for (int i = 0; i < length; i++) {
                        assertEquals(reversed.keyAt(i), array.get(reversed.valueAt(i)));
                    }
                }
            }
        });
    }

    public void testKeySetWhenEmpty() {
        final ImmutableIntPairMap empty = ImmutableIntPairMap.empty();
        assertSame(ImmutableIntSetImpl.empty(), empty.keySet());
    }

    public void testToImmutableForEmpty() {
        final ImmutableIntPairMap map = newBuilder().build();
        assertSame(map, map.toImmutable());
    }

    public void testMutateForEmpty() {
        final ImmutableIntPairMap map1 = newBuilder().build();
        final MutableIntPairMap map2 = map1.mutate();

        assertTrue(map2.isEmpty());

        map2.put(1, 4);
        assertEquals(0, map1.get(1, 0));
    }

    public void testToImmutable() {
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap map1 = newBuilder().put(a, 1).put(b, 2).build();
            final ImmutableIntPairMap map2 = map1.toImmutable();
            assertSame(map1, map2);
        }));
    }

    public void testMutate() {
        final int defValue = -4;
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap map1 = newBuilder().put(a, 1).put(b, 2).build();
            final MutableIntPairMap map2 = map1.mutate();

            final Iterator<IntPairMap.Entry> it1 = map1.entries().iterator();
            final Iterator<IntPairMap.Entry> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                final IntPairMap.Entry item1 = it1.next();
                final IntPairMap.Entry item2 = it2.next();
                assertEquals(item1.key(), item2.key());
                assertEquals(item1.value(), item2.value());
            }
            assertFalse(it2.hasNext());

            map2.remove(b);
            assertEquals(2, map1.get(b, defValue));
            assertEquals(defValue, map2.get(b, defValue));
        }));
    }
}

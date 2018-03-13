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
                    for (IntPairMap.Entry entry : array) {
                        builder.put(entry.getKey(), entry.getValue());
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
        assertEquals(0, map1.get(1));
    }

    public void testToImmutable() {
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap map1 = newBuilder().put(a, 1).put(b, 2).build();
            final ImmutableIntPairMap map2 = map1.toImmutable();
            assertSame(map1, map2);
        }));
    }

    public void testMutate() {
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap map1 = newBuilder().put(a, 1).put(b, 2).build();
            final MutableIntPairMap map2 = map1.mutate();

            final Iterator<IntPairMap.Entry> it1 = map1.iterator();
            final Iterator<IntPairMap.Entry> it2 = map2.iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                final IntPairMap.Entry item1 = it1.next();
                final IntPairMap.Entry item2 = it2.next();
                assertEquals(item1.getKey(), item2.getKey());
                assertEquals(item1.getValue(), item2.getValue());
            }
            assertFalse(it2.hasNext());

            map2.remove(b);
            assertEquals(2, map1.get(b));
            assertEquals(0, map2.get(b));
        }));
    }
}

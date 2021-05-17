package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;

abstract class IntValueMapTest<K, B extends IntTransformableBuilder> extends IntTransformableTest<B> {

    abstract IntValueMap.Builder<K> newBuilder();

    abstract void withMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<K, IntValueMap.Builder<K>>> procedure);
    abstract void withKey(Procedure<K> procedure);
    abstract void withSortFunc(Procedure<SortFunction<K>> procedure);
    abstract K keyFromInt(int value);

    private void withArbitraryMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<K, IntValueMap.Builder<K>>> procedure) {
        procedure.apply(ImmutableIntValueHashMap.Builder::new);
        procedure.apply(MutableIntValueHashMap.Builder::new);
        withSortFunc(sortFunc -> {
            procedure.apply(() -> new ImmutableIntValueSortedMap.Builder<>(sortFunc));
            procedure.apply(() -> new MutableIntValueSortedMap.Builder<>(sortFunc));
        });
    }

    @Override
    public void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    @Override
    public void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v * v);
        procedure.apply(v -> v + 1);
    }

    @Test
    void testEmptyBuilderBuildsEmptyArray() {
        final IntValueMap<K> array = newBuilder().build();
        assertEquals(0, array.size());
        assertFalse(array.iterator().hasNext());
    }

    @Test
    void testBuilderWithSingleElementBuildsExpectedArray() {
        withKey(key -> withInt(value -> {
            final IntValueMap<K> array = newBuilder()
                    .put(key, value)
                    .build();

            assertEquals(1, array.size());
            assertSame(key, array.keyAt(0));
            assertEquals(value, array.valueAt(0));
        }));
    }

    @Test
    void testGet() {
        final int value = 45;
        final int defValue = 3;
        withKey(a -> withKey(b -> {
            IntValueMap<K> array = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .build();

            withKey(other -> {
                final int expectedValue = (equal(other, a) || equal(other, b))? value : defValue;
                assertEquals(expectedValue, array.get(other, defValue));
            });
        }));
    }

    @Test
    void testKeyAtMethod() {
        final int value = 6;
        withKey(a -> withKey(b -> withKey(c -> {
            final IntValueMap<K> array = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .build();

            final int size = array.size();
            boolean aFound = false;
            boolean bFound = false;
            boolean cFound = false;

            int index;
            for (index = 0; index < size && (!aFound || !bFound || !cFound); index++) {
                final K item = array.keyAt(index);
                if (item == a) {
                    aFound = true;
                }

                if (item == b) {
                    bFound = true;
                }

                if (item == c) {
                    cFound = true;
                }
            }

            assertTrue(aFound && bFound && cFound);
            assertEquals(size, index);
        })));
    }

    int valueFromKey(K str) {
        return (str != null)? str.hashCode() : 0;
    }

    @Test
    void testValueAtMethod() {
        withKey(a -> withKey(b -> withKey(c -> {
            IntValueMap<K> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int size = map.size();
            for (int i = 0; i < size; i++) {
                final K key = map.keyAt(i);
                assertEquals(valueFromKey(key), map.valueAt(i));
            }
        })));
    }

    @Test
    void testKeySetWhenEmpty() {
        assertTrue(newBuilder().build().isEmpty());
    }

    @Test
    void testKeySet() {
        final int value = 125;
        for (int amount = 0; amount < 3; amount++) {
            final IntValueMap.Builder<K> mapBuilder = newBuilder();
            final ImmutableHashSet.Builder<K> setBuilder = new ImmutableHashSet.Builder<>();
            for (int i = 0; i < amount; i++) {
                final K key = keyFromInt(i);
                setBuilder.add(key);
                mapBuilder.put(key, value);
            }

            final ImmutableHashSet<K> expectedKeys = setBuilder.build();
            final ImmutableSet<K> keySet = mapBuilder.build().keySet().toImmutable();
            assertEquals(expectedKeys, keySet);
        }
    }

    @Test
    void testIndexOfKey() {
        final int value = 37;
        withKey(a -> withKey(b -> withKey(c -> {
            final IntValueMap<K> map = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .build();

            assertEquals(a, map.keyAt(map.indexOfKey(a)));
            assertEquals(b, map.keyAt(map.indexOfKey(b)));
            assertEquals(c, map.keyAt(map.indexOfKey(c)));
        })));
    }

    @Test
    void testContainsKeyWhenEmpty() {
        final IntValueMap<K> map = newBuilder().build();
        withKey(key -> assertFalse(map.containsKey(key)));
    }

    @Test
    void testContainsKey() {
        withKey(a -> withKey(b -> withKey(c -> {
            final IntValueMap<K> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final boolean expectedResult = equal(c, a) || equal(c, b);
            assertEquals(expectedResult, map.containsKey(c));
        })));
    }

    @Test
    void testEntryIterator() {
        withKey(a -> withKey(b -> withKey(c -> {
            IntValueMap<K> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int size = map.size();
            final Iterator<IntValueMap.Entry<K>> iterator = map.entries().iterator();
            for (int i = 0; i < size; i++) {
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<K> entry = iterator.next();
                assertEquals(i, entry.index());
                assertEquals(map.keyAt(i), entry.key());
                assertEquals(map.valueAt(i), entry.value());
            }

            assertFalse(iterator.hasNext());
        })));
    }

    @Test
    void testEqualMapReturnsFalseWhenAPairIsMissing() {
        withKey(a -> withKey(b -> withKey(c -> withMapBuilderSupplier(supplier -> {
            final IntValueMap<K> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int mapSize = map.size();
            final IntValueMap.Builder<K> mapBuilder = supplier.newBuilder();
            for (int i = 1; i < mapSize; i++) {
                mapBuilder.put(map.keyAt(i), map.valueAt(i));
            }
            final IntValueMap<K> reducedMap = mapBuilder.build();

            assertFalse(map.equalMap(reducedMap));
            assertFalse(reducedMap.equalMap(map));
        }))));
    }

    @Test
    void testEqualMapReturnsFalseWhenKeyMatchesButNotValues() {
        withKey(a -> withKey(b -> withKey(c -> withMapBuilderSupplier(supplier -> {
            final IntValueMap<K> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int mapSize = map.size();
            for (int j = 0; j < mapSize; j++) {
                final IntValueMap.Builder<K> mapBuilder = supplier.newBuilder();
                for (int i = 0; i < mapSize; i++) {
                    final int mapValue = map.valueAt(i);
                    final int value = (i == j)? ~mapValue : mapValue;
                    mapBuilder.put(map.keyAt(i), value);
                }
                final IntValueMap<K> modifiedMap = mapBuilder.build();

                assertFalse(map.equalMap(modifiedMap));
                assertFalse(modifiedMap.equalMap(map));
            }
        }))));
    }

    @Test
    void testEqualMapReturnsTrueForOtherSortingsAndMutabilities() {
        withKey(a -> withKey(b -> withKey(c -> withMapBuilderSupplier(supplier -> {
            final IntValueMap<K> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            withArbitraryMapBuilderSupplier(mapSupplier -> {
                final IntValueMap<K> arbitraryMap = mapSupplier.newBuilder()
                        .put(a, valueFromKey(a))
                        .put(b, valueFromKey(b))
                        .put(c, valueFromKey(c))
                        .build();

                assertTrue(map.equalMap(arbitraryMap));
            });
        }))));
    }

    @Test
    void testMutateMethod() {
        withKey(a -> withKey(b -> {
            IntValueMap<K> map1 = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            MutableIntValueMap<K> map2 = map1.mutate();

            final Iterator<IntValueMap.Entry<K>> it1 = map1.entries().iterator();
            final Iterator<IntValueMap.Entry<K>> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());

                final IntValueMap.Entry<K> entry1 = it1.next();
                final IntValueMap.Entry<K> entry2 = it2.next();

                assertEquals(entry1.key(), entry2.key());
                assertEquals(entry1.value(), entry2.value());
            }
            assertFalse(it2.hasNext());

            final ImmutableIntValueMap<K> immutableMap1 = map1.toImmutable();
            assertEquals(immutableMap1, map2.toImmutable());
            map2.removeAt(0);
            assertNotEquals(immutableMap1, map2.toImmutable());
        }));
    }

    @Test
    void testSortWhenEmpty() {
        final SortFunction<K> func = (a, b) -> {
            throw new AssertionError("Should not be called");
        };
        assertTrue(newBuilder().build().sort(func).isEmpty());
    }

    @Test
    void testSortForSingleElement() {
        final SortFunction<K> func = (a, b) -> {
            throw new AssertionError("Should not be called");
        };
        withKey(key -> {
            final int value = valueFromKey(key);
            final IntValueMap<K> map = newBuilder().put(key, value).build().sort(func);
            assertEquals(1, map.size());
            assertSame(key, map.keyAt(0));
            assertEquals(value, map.valueAt(0));
        });
    }

    @Test
    void testSort() {
        withKey(a -> withKey(b -> withKey(c -> {
            final IntValueMap<K> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();
            final int mapLength = map.size();
            withSortFunc(f -> {
                final IntValueMap<K> sortedMap = map.sort(f);
                assertEquals(mapLength, sortedMap.size(), map.toString());
                for (int i = 1; i < mapLength; i++) {
                    assertFalse(f.lessThan(sortedMap.keyAt(i), sortedMap.keyAt(i - 1)));
                }
            });
        })));
    }

    @Test
    void testMapResultingKeysForMultipleElements() {
        withMapFunc(f -> withKey(keyA -> withKey(keyB -> withMapBuilderSupplier(supplier -> {
            final IntValueMap<K> map = supplier.newBuilder()
                    .put(keyA, valueFromKey(keyA))
                    .put(keyB, valueFromKey(keyB))
                    .build();
            final Map<K, String> mapped = map.map(f);

            final int size = map.size();
            assertEquals(size, mapped.size());

            for (int i = 0; i < size; i++) {
                assertSame(map.keyAt(i), mapped.keyAt(i));
            }
        }))));
    }

    @Test
    void testMapToIntForMultipleElements() {
        withMapToIntFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final IntValueMap<K> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final IntValueMap<K> mapped = map.mapToInt(f);

            final int size = map.size();
            assertEquals(size, mapped.size());

            for (int i = 0; i < size; i++) {
                assertSame(map.keyAt(i), mapped.keyAt(i));
            }
        }))));
    }
}

package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;

abstract class MapTest<K, V, B extends TransformableBuilder<V>> extends TransformableTest<V, B> {

    abstract MapBuilder<K, V> newBuilder();
    abstract void withKey(Procedure<K> procedure);
    abstract void withFilterFunc(Procedure<Predicate<V>> procedure);
    abstract void withSortFunc(Procedure<SortFunction<K>> procedure);
    abstract V getTestValue();
    abstract K keyFromInt(int value);
    abstract V valueFromKey(K key);
    abstract void withMapBuilderSupplier(Procedure<MapBuilderSupplier<K, V, MapBuilder<K, V>>> procedure);
    abstract void withMapFunc(Procedure<Function<V, String>> procedure);
    abstract void withMapToIntFunc(Procedure<IntResultFunction<V>> procedure);

    private void withArbitraryMapBuilderSupplier(Procedure<MapBuilderSupplier<K, V, MapBuilder<K, V>>> procedure) {
        procedure.apply(ImmutableHashMap.Builder::new);
        procedure.apply(MutableHashMap.Builder::new);
        withSortFunc(sortFunc -> {
            procedure.apply(() -> new ImmutableSortedMap.Builder<>(sortFunc));
            procedure.apply(() -> new MutableSortedMap.Builder<>(sortFunc));
        });
    }

    @Test
    void testEmptyBuilderBuildsEmptyArray() {
        Map<K, V> array = newBuilder().build();
        assertEquals(0, array.size());
    }

    @Test
    void testGet() {
        final V value = getTestValue();
        withKey(a -> withKey(b -> {
            final Map<K, V> map = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .build();

            withKey(other -> {
                final V expectedValue = (equal(other, a) || equal(other, b))? value : null;
                assertEquals(expectedValue, map.get(other, null));
            });
        }));
    }

    @Test
    void testKeyAtMethod() {
        withValue(value -> withKey(a -> withKey(b -> withKey(c -> {
            final Map<K, V> map = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .build();

            final MutableHashSet<K> keySet = new MutableHashSet.Builder<K>().add(a).add(b).add(c).build();

            final int size = map.size();
            for (int i = 0; i < size; i++) {
                assertTrue(keySet.remove(map.keyAt(i)));
            }

            assertTrue(keySet.isEmpty());
        }))));
    }

    @Test
    void testValueAtMethod() {
        withKey(a -> withKey(b -> withKey(c -> {
            Map<K, V> map = newBuilder()
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
    void testKeySet() {
        final V value = getTestValue();
        for (int amount = 0; amount < 3; amount++) {
            final MapBuilder<K, V> mapBuilder = newBuilder();
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
        final V value = getTestValue();
        withKey(a -> withKey(b -> withKey(c -> {
            final Map<K, V> map = newBuilder()
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
        final Map<K, V> map = newBuilder().build();
        withKey(key -> assertFalse(map.containsKey(key)));
    }

    @Test
    void testContainsKey() {
        withKey(a -> withKey(b -> withKey(c -> {
            final V value = getTestValue();
            final Map<K, V> map = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .build();

            final boolean expectedResult = equal(c, a) || equal(c, b);
            assertEquals(expectedResult, map.containsKey(c));
        })));
    }

    @Test
    void testEntryIterator() {
        withKey(a -> withKey(b -> withKey(c -> {
            Map<K, V> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int size = map.size();
            final Iterator<Map.Entry<K, V>> iterator = map.entries().iterator();
            for (int i = 0; i < size; i++) {
                assertTrue(iterator.hasNext());

                final Map.Entry<K, V> entry = iterator.next();
                assertEquals(i, entry.index());
                assertEquals(map.keyAt(i), entry.key());
                assertEquals(map.valueAt(i), entry.value());
            }

            assertFalse(iterator.hasNext());
        })));
    }

    @Test
    void testMutateMethod() {
        withKey(a -> withKey(b -> {
            Map<K, V> map1 = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            MutableMap<K, V> map2 = map1.mutate();

            final Iterator<Map.Entry<K, V>> it1 = map1.entries().iterator();
            final Iterator<Map.Entry<K, V>> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());

                final Map.Entry<K, V> entry1 = it1.next();
                final Map.Entry<K, V> entry2 = it2.next();

                assertEquals(entry1.key(), entry2.key());
                assertEquals(entry1.value(), entry2.value());
            }
            assertFalse(it2.hasNext());

            map2.remove(b);
            assertTrue(map1.containsKey(b));
            assertFalse(map2.containsKey(b));
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
            final V value = valueFromKey(key);
            final Map<K, V> map = newBuilder().put(key, value).build().sort(func);
            assertEquals(1, map.size());
            assertSame(key, map.keyAt(0));
            assertSame(value, map.valueAt(0));
        });
    }

    @Test
    void testSort() {
        withKey(a -> withKey(b -> withKey(c -> {
            final Map<K, V> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();
            final int mapLength = map.size();
            withSortFunc(f -> {
                final Map<K, V> sortedMap = map.sort(f);
                assertEquals(mapLength, sortedMap.size());
                for (int i = 1; i < mapLength; i++) {
                    assertFalse(f.lessThan(sortedMap.keyAt(i), sortedMap.keyAt(i - 1)));
                }
            });
        })));
    }

    @Test
    void testFilterResultingKeysForMultipleElements() {
        withFilterFunc(f -> withKey(keyA -> withKey(keyB -> {
            final V valueA = valueFromKey(keyA);
            final V valueB = valueFromKey(keyB);
            final Map<K, V> map = newBuilder()
                    .put(keyA, valueA)
                    .put(keyB, valueB)
                    .build();
            final Map<K, V> filtered = map.filter(f);

            final int size = map.size();
            int index = 0;
            for (int i = 0; i < size; i++) {
                if (f.apply(map.valueAt(i))) {
                    assertSame(map.keyAt(i), filtered.keyAt(index++));
                }
            }

            assertEquals(filtered.size(), index);
        })));
    }

    @Test
    void testFilterNotResultingKeysForMultipleElements() {
        withFilterFunc(f -> withKey(keyA -> withKey(keyB -> {
            final V valueA = valueFromKey(keyA);
            final V valueB = valueFromKey(keyB);
            final Map<K, V> map = newBuilder()
                    .put(keyA, valueA)
                    .put(keyB, valueB)
                    .build();
            final Map<K, V> filtered = map.filterNot(f);

            final int size = map.size();
            int index = 0;
            for (int i = 0; i < size; i++) {
                if (!f.apply(map.valueAt(i))) {
                    assertSame(map.keyAt(i), filtered.keyAt(index++));
                }
            }

            assertEquals(filtered.size(), index);
        })));
    }

    @Test
    void testEqualMapReturnsFalseWhenAPairIsMissing() {
        withKey(a -> withKey(b -> withKey(c -> withMapBuilderSupplier(supplier -> {
            final Map<K, V> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int mapSize = map.size();
            final MapBuilder<K, V> mapBuilder = supplier.newBuilder();
            for (int i = 1; i < mapSize; i++) {
                mapBuilder.put(map.keyAt(i), map.valueAt(i));
            }
            final Map<K, V> reducedMap = mapBuilder.build();

            assertFalse(map.equalMap(reducedMap));
            assertFalse(reducedMap.equalMap(map));
        }))));
    }

    @Test
    void testEqualMapReturnsFalseWhenKeyMatchesButNotValues() {
        withKey(a -> withKey(b -> withKey(c -> withMapBuilderSupplier(supplier -> {
            final Map<K, V> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int mapSize = map.size();
            for (int j = 0; j < mapSize; j++) {
                final MapBuilder<K, V> mapBuilder = supplier.newBuilder();
                for (int i = 0; i < mapSize; i++) {
                    V value = (i == j) ? null : map.valueAt(i);
                    mapBuilder.put(map.keyAt(i), value);
                }
                final Map<K, V> modifiedMap = mapBuilder.build();

                assertFalse(map.equalMap(modifiedMap));
                assertFalse(modifiedMap.equalMap(map));
            }
        }))));
    }

    @Test
    void testEqualMapReturnsTrueForOtherSortingsAndMutabilities() {
        withKey(a -> withKey(b -> withKey(c -> withMapBuilderSupplier(supplier -> {
            final Map<K, V> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            withArbitraryMapBuilderSupplier(mapSupplier -> {
                final Map<K, V> arbitraryMap = mapSupplier.newBuilder()
                        .put(a, valueFromKey(a))
                        .put(b, valueFromKey(b))
                        .put(c, valueFromKey(c))
                        .build();

                assertTrue(map.equalMap(arbitraryMap));
            });
        }))));
    }

    @Test
    void testMapResultingKeysForMultipleElements() {
        withMapFunc(f -> withKey(keyA -> withKey(keyB -> withMapBuilderSupplier(supplier -> {
            final Map<K, V> map = supplier.newBuilder()
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
            final Map<K, V> map = supplier.newBuilder()
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

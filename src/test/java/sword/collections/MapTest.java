package sword.collections;

import junit.framework.TestCase;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class MapTest<K, V> extends TestCase {

    abstract MapBuilder<K, V> newBuilder();
    abstract void withKey(Procedure<K> procedure);
    abstract void withValue(Procedure<V> procedure);
    abstract void withFilterFunc(Procedure<Predicate<V>> procedure);
    abstract void withSortFunc(Procedure<SortFunction<K>> procedure);
    abstract V getTestValue();
    abstract K keyFromInt(int value);
    abstract V valueFromKey(K key);
    abstract void withMapBuilderSupplier(Procedure<MapBuilderSupplier<K, V, MapBuilder<K, V>>> procedure);

    private void withArbitraryMapBuilderSupplier(Procedure<MapBuilderSupplier<K, V, MapBuilder<K, V>>> procedure) {
        procedure.apply(ImmutableHashMap.Builder::new);
        procedure.apply(MutableHashMap.Builder::new);
        withSortFunc(sortFunc -> {
            procedure.apply(() -> new ImmutableSortedMap.Builder<>(sortFunc));
            procedure.apply(() -> new MutableSortedMap.Builder<>(sortFunc));
        });
    }

    public void testEmptyBuilderBuildsEmptyArray() {
        Map<K, V> array = newBuilder().build();
        assertEquals(0, array.size());
    }

    public void testSize() {
        final V value = getTestValue();
        withKey(a -> withKey(b -> withKey(c -> withKey(d -> {
            Map<K, V> map = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .put(d, value)
                    .build();

            int expectedSize = 1;
            if (!equal(a, b)) {
                expectedSize++;
            }

            if (!equal(b, c) && !equal(a, c)) {
                expectedSize++;
            }

            if (!equal(c, d) && !equal(b, d) && !equal(a, d)) {
                expectedSize++;
            }

            assertEquals(expectedSize, map.size());
        }))));
    }

    public void testGet() {
        final V value = getTestValue();
        withKey(a -> withKey(b -> {
            Map<K, V> array = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .build();

            withKey(other -> {
                final V expectedValue = (equal(other, a) || equal(other, b))? value : null;
                assertEquals(expectedValue, array.get(other, null));
            });
        }));
    }

    public void testKeyAtMethod() {
        withValue(value -> withKey(a -> withKey(b -> withKey(c -> {
            Map<K, V> array = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .build();

            MutableHashSet<K> keySet = new MutableHashSet.Builder<K>().add(a).add(b).add(c).build();

            final int size = array.size();
            for (int i = 0; i < size; i++) {
                assertTrue(keySet.remove(array.keyAt(i)));
            }

            assertTrue(keySet.isEmpty());
        }))));
    }

    public void testValueAtMethod() {
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

    public void testKeySet() {
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

    public void testValueListWhenEmpty() {
        assertTrue(newBuilder().build().valueList().isEmpty());
    }

    public void testValueList() {
        withKey(a -> withKey(b -> withKey(c -> {
            final Map<K, V> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final ImmutableList.Builder<V> listBuilder = new ImmutableList.Builder<>();
            for (V value : map) {
                listBuilder.add(value);
            }

            assertEquals(listBuilder.build(), map.valueList().toImmutable());
        })));
    }

    public void testIndexOfKey() {
        final V value = getTestValue();
        withKey(a -> withKey(b -> withKey(c -> {
            final Map map = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .build();

            assertEquals(a, map.keyAt(map.indexOfKey(a)));
            assertEquals(b, map.keyAt(map.indexOfKey(b)));
            assertEquals(c, map.keyAt(map.indexOfKey(c)));
        })));
    }

    public void testEntryIterator() {
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

    public void testMutateMethod() {
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

    public void testSortWhenEmpty() {
        final SortFunction<K> func = (a, b) -> {
            throw new AssertionError("Should not be called");
        };
        assertTrue(newBuilder().build().sort(func).isEmpty());
    }

    public void testSortForSingleElement() {
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

    public void testSort() {
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

    public void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final Map<K, V> map = newBuilder().build();
            assertTrue(map.filter(f).isEmpty());
        });
    }

    public void testFilterForSingleElement() {
        withFilterFunc(f -> withKey(key -> {
            final V value = valueFromKey(key);
            final Map<K, V> map = newBuilder().put(key, value).build();
            final Map<K, V> filtered = map.filter(f);

            if (f.apply(value)) {
                assertEquals(map, filtered);
            }
            else {
                assertTrue(filtered.isEmpty());
            }
        }));
    }

    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withKey(keyA -> withKey(keyB -> {
            final V valueA = valueFromKey(keyA);
            final V valueB = valueFromKey(keyB);
            final Map<K, V> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final Map<K, V> filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertEquals(map, filtered);
            }
            else if (aPassed) {
                Iterator<Map.Entry<K, V>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final Map.Entry<K, V> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<Map.Entry<K, V>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final Map.Entry<K, V> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertSame(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertTrue(filtered.isEmpty());
            }
        })));
    }

    public void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final Map<K, V> map = newBuilder().build();
            assertTrue(map.filterNot(f).isEmpty());
        });
    }

    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withKey(key -> {
            final V value = valueFromKey(key);
            final Map<K, V> map = newBuilder().put(key, value).build();
            final Map<K, V> filtered = map.filterNot(f);

            if (f.apply(value)) {
                assertTrue(filtered.isEmpty());
            }
            else {
                assertEquals(map, filtered);
            }
        }));
    }

    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withKey(keyA -> withKey(keyB -> {
            final V valueA = valueFromKey(keyA);
            final V valueB = valueFromKey(keyB);
            final Map<K, V> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final Map<K, V> filtered = map.filterNot(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertTrue(filtered.isEmpty());
            }
            else if (aPassed) {
                Iterator<Map.Entry<K, V>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final Map.Entry<K, V> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertSame(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<Map.Entry<K, V>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final Map.Entry<K, V> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertEquals(map, filtered);
            }
        })));
    }

    public void testEqualMapReturnsFalseWhenAPairIsMissing() {
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

    public void testEqualMapReturnsFalseWhenKeyMatchesButNotValues() {
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

    public void testEqualMapReturnsTrueForOtherSortingsAndMutabilities() {
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
}

package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;

interface MapTest<K, V, B extends TransformableBuilder<V>, MB extends MapBuilder<K, V>> extends TransformableTest<V, B> {

    MB newBuilder();
    void withKey(Procedure<K> procedure);
    void withFilterByKeyFunc(Procedure<Predicate<K>> procedure);
    void withSortFunc(Procedure<SortFunction<K>> procedure);
    V getTestValue();
    K keyFromInt(int value);
    V valueFromKey(K key);
    void withMapBuilderSupplier(Procedure<MapBuilderSupplier<K, V, MB>> procedure);

    default void withFilterByEntryFunc(Procedure<Predicate<MapEntry<K, V>>> procedure) {
        withFilterByKeyFunc(f -> procedure.apply(entry -> f.apply(entry.key())));
    }

    @Override
    void withMapFunc(Procedure<Function<V, String>> procedure);

    @Override
    void withMapToIntFunc(Procedure<IntResultFunction<V>> procedure);

    default void withArbitraryMapBuilderSupplier(Procedure<MapBuilderSupplier<K, V, MapBuilder<K, V>>> procedure) {
        procedure.apply(ImmutableHashMap.Builder::new);
        procedure.apply(MutableHashMap.Builder::new);
        withSortFunc(sortFunc -> {
            procedure.apply(() -> new ImmutableSortedMap.Builder<>(sortFunc));
            procedure.apply(() -> new MutableSortedMap.Builder<>(sortFunc));
        });
    }

    @Test
    default void testEmptyBuilderBuildsEmptyArray() {
        Map<K, V> array = newBuilder().build();
        assertEquals(0, array.size());
    }

    @Test
    default void testGet() {
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
    default void testKeyAtMethod() {
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
    default void testValueAtMethod() {
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
    default void testKeySet() {
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
    default void testIndexOfKey() {
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
    default void testContainsKeyWhenEmpty() {
        final Map<K, V> map = newBuilder().build();
        withKey(key -> assertFalse(map.containsKey(key)));
    }

    @Test
    default void testContainsKey() {
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
    default void testEntryIterator() {
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
    default void testMutateMethod() {
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
    default void testSortWhenEmpty() {
        final SortFunction<K> func = (a, b) -> {
            throw new AssertionError("Should not be called");
        };
        assertTrue(newBuilder().build().sort(func).isEmpty());
    }

    @Test
    default void testSortForSingleElement() {
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
    default void testSort() {
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
    default void testFilterResultingKeysForMultipleElements() {
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
    default void testFilterNotResultingKeysForMultipleElements() {
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
    default void testEqualMapReturnsFalseWhenAPairIsMissing() {
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
    default void testEqualMapReturnsFalseWhenKeyMatchesButNotValues() {
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
    default void testEqualMapReturnsTrueForOtherSortingsAndMutabilities() {
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
    default void testMapResultingKeysForMultipleElements() {
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
    default void testMapToIntForMultipleElements() {
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

    @Test
    default void testFilterByKeyWhenEmpty() {
        final Predicate<K> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().filterByKey(f).iterator().hasNext());
        });
    }

    @Test
    default void testFilterByKeyForSingleElement() {
        withFilterByKeyFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final Map<K, V> map = supplier.newBuilder().put(key, valueFromKey(key)).build();
            final Map<K, V> filtered = map.filterByKey(f);

            if (f.apply(key)) {
                assertTrue(map.equalMap(filtered));
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        })));
    }

    @Test
    default void testFilterByKeyForMultipleElements() {
        withFilterByKeyFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final Map<K, V> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final Map<K, V> filtered = map.filterByKey(f);

            final TransformerWithKey<K, V> tr = filtered.iterator();
            for (K key : map.keySet()) {
                if (f.apply(key)) {
                    assertTrue(tr.hasNext());
                    assertSame(map.get(key), tr.next());
                    assertSame(key, tr.key());
                }
            }
            assertFalse(tr.hasNext());
        }))));
    }

    @Test
    default void testFilterByKeyNotWhenEmpty() {
        final Predicate<K> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().filterByKeyNot(f).iterator().hasNext());
        });
    }

    @Test
    default void testFilterByKeyNotForSingleElement() {
        withFilterByKeyFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final Map<K, V> map = supplier.newBuilder().put(key, valueFromKey(key)).build();
            final Map<K, V> filtered = map.filterByKeyNot(f);

            if (!f.apply(key)) {
                assertTrue(map.equalMap(filtered));
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        })));
    }

    @Test
    default void testFilterByKeyNotForMultipleElements() {
        withFilterByKeyFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final Map<K, V> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final Map<K, V> filtered = map.filterByKeyNot(f);

            final TransformerWithKey<K, V> tr = filtered.iterator();
            for (K key : map.keySet()) {
                if (!f.apply(key)) {
                    assertTrue(tr.hasNext());
                    assertSame(map.get(key), tr.next());
                    assertSame(key, tr.key());
                }
            }
            assertFalse(tr.hasNext());
        }))));
    }

    @Test
    default void testFilterByEntryWhenEmpty() {
        final Predicate<MapEntry<K, V>> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().filterByEntry(f).iterator().hasNext());
        });
    }

    @Test
    default void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final Map.Entry<K, V> entry = new Map.Entry<>(0, key, valueFromKey(key));
            final Map<K, V> map = supplier.newBuilder().put(key, entry.value()).build();
            final Map<K, V> filtered = map.filterByEntry(f);

            if (f.apply(entry)) {
                assertTrue(map.equalMap(filtered));
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        })));
    }

    @Test
    default void testFilterByEntryForMultipleElements() {
        withFilterByEntryFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final Map<K, V> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final Map<K, V> filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            int counter = 0;
            for (Map.Entry<K, V> entry : map.entries()) {
                if (f.apply(entry)) {
                    assertSame(entry.value(), filtered.get(entry.key()));
                    counter++;
                }
            }
            assertEquals(filteredSize, counter);
        }))));
    }

    @Test
    @Override
    default void testSlice() {
        withKey(a -> withKey(b -> withKey(c -> {
            final V aValue = valueFromKey(a);
            final V bValue = valueFromKey(b);
            final V cValue = valueFromKey(c);
            final Map<K, V> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final K firstKey = map.keyAt(0);
            final K secondKey = (size >= 2)? map.keyAt(1) : null;
            final K thirdKey = (size >= 3)? map.keyAt(2) : null;
            final V firstValue = map.valueAt(0);
            final V secondValue = (size >= 2)? map.valueAt(1) : null;
            final V thirdValue = (size >= 3)? map.valueAt(2) : null;

            final Map<K, V> sliceA = map.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(firstKey, sliceA.keyAt(0));
            assertSame(firstValue, sliceA.valueAt(0));

            final Map<K, V> sliceB = map.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(secondKey, sliceB.keyAt(0));
                assertSame(secondValue, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final Map<K, V> sliceC = map.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(thirdKey, sliceC.keyAt(0));
                assertSame(thirdValue, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final Map<K, V> sliceAB = map.slice(new ImmutableIntRange(0, 1));
            if (size >= 2) {
                assertEquals(2, sliceAB.size());
                assertSame(secondKey, sliceAB.keyAt(1));
                assertSame(secondValue, sliceAB.valueAt(1));
            }
            else {
                assertEquals(1, sliceAB.size());
            }
            assertSame(firstKey, sliceAB.keyAt(0));
            assertSame(firstValue, sliceAB.valueAt(0));

            final Map<K, V> sliceBC = map.slice(new ImmutableIntRange(1, 2));
            if (size == 1) {
                assertEquals(0, sliceBC.size());
            }
            else if (size == 2) {
                assertEquals(1, sliceBC.size());
                assertSame(secondKey, sliceBC.keyAt(0));
                assertSame(secondValue, sliceBC.valueAt(0));
            }
            else {
                assertEquals(2, sliceBC.size());
                assertSame(secondKey, sliceBC.keyAt(0));
                assertSame(secondValue, sliceBC.valueAt(0));
                assertSame(thirdKey, sliceBC.keyAt(1));
                assertSame(thirdValue, sliceBC.valueAt(1));
            }

            final Map<K, V> sliceABC = map.slice(new ImmutableIntRange(0, 2));
            assertEquals(size, sliceABC.size());
            assertSame(firstKey, sliceABC.keyAt(0));
            assertSame(firstValue, sliceABC.valueAt(0));
            if (size >= 2) {
                assertSame(secondKey, sliceABC.keyAt(1));
                assertSame(secondValue, sliceABC.valueAt(1));
                if (size >= 3) {
                    assertSame(thirdKey, sliceABC.keyAt(2));
                    assertSame(thirdValue, sliceABC.valueAt(2));
                }
            }

            final Map<K, V> sliceABCD = map.slice(new ImmutableIntRange(0, 3));
            assertEquals(size, sliceABCD.size());
            assertSame(firstKey, sliceABCD.keyAt(0));
            assertSame(firstValue, sliceABCD.valueAt(0));
            if (size >= 2) {
                assertSame(secondKey, sliceABCD.keyAt(1));
                assertSame(secondValue, sliceABCD.valueAt(1));
                if (size >= 3) {
                    assertSame(thirdKey, sliceABCD.keyAt(2));
                    assertSame(thirdValue, sliceABCD.valueAt(2));
                }
            }
        })));
    }

    @Test
    @Override
    default void testSkipWhenEmpty() {
        final Map<K, V> map = newBuilder().build();
        assertSame(map, map.skip(0));
        assertTrue(map.skip(1).isEmpty());
        assertTrue(map.skip(20).isEmpty());
    }

    @Test
    @Override
    default void testSkip() {
        withKey(a -> withKey(b -> withKey(c -> {
            final V aValue = valueFromKey(a);
            final V bValue = valueFromKey(b);
            final V cValue = valueFromKey(c);

            final Map<K, V> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            final int size = map.size();
            final K secondKey = (size >= 2)? map.keyAt(1) : null;
            final V secondValue = (size >= 2)? map.valueAt(1) : null;
            final K thirdKey = (size == 3)? map.keyAt(2) : null;
            final V thirdValue = (size == 3)? map.valueAt(2) : null;

            assertSame(map, map.skip(0));

            final Map<K, V> skip1 = map.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertSame(secondKey, skip1.keyAt(0));
                assertSame(secondValue, skip1.valueAt(0));
                if (size == 3) {
                    assertSame(thirdKey, skip1.keyAt(1));
                    assertSame(thirdValue, skip1.valueAt(1));
                }
            }

            final Map<K, V> skip2 = map.skip(2);
            if (size == 3) {
                assertSame(thirdKey, skip2.keyAt(0));
                assertSame(thirdValue, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertTrue(skip2.isEmpty());
            }

            assertTrue(map.skip(3).isEmpty());
            assertTrue(map.skip(4).isEmpty());
            assertTrue(map.skip(24).isEmpty());
        })));
    }

    @Test
    @Override
    default void testTakeWhenEmpty() {
        final Map<K, V> map = newBuilder().build();
        assertTrue(map.take(0).isEmpty());
        assertTrue(map.take(1).isEmpty());
        assertTrue(map.take(2).isEmpty());
        assertTrue(map.take(24).isEmpty());
    }

    @Test
    @Override
    default void testTake() {
        withKey(a -> withKey(b -> withKey(c -> {
            final V aValue = valueFromKey(a);
            final V bValue = valueFromKey(b);
            final V cValue = valueFromKey(c);
            final Map<K, V> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final K firstKey = map.keyAt(0);
            final V firstValue = map.valueAt(0);

            assertTrue(map.take(0).isEmpty());

            final Map<K, V> take1 = map.take(1);
            assertEquals(1, take1.size());
            assertSame(firstKey, take1.keyAt(0));
            assertSame(firstValue, take1.valueAt(0));

            final Map<K, V> take2 = map.take(2);
            assertSame(firstKey, take2.keyAt(0));
            assertSame(firstValue, take2.valueAt(0));
            if (size == 1) {
                assertEquals(1, take2.size());
            }
            else {
                assertEquals(2, take2.size());
                assertSame(map.keyAt(1), take2.keyAt(1));
                assertSame(map.valueAt(1), take2.valueAt(1));
            }

            final Map<K, V> take3 = map.take(3);
            assertEquals(size, take3.size());
            assertSame(firstKey, take3.keyAt(0));
            assertSame(firstValue, take3.valueAt(0));
            if (size > 1) {
                assertSame(map.keyAt(1), take3.keyAt(1));
                assertSame(map.valueAt(1), take3.valueAt(1));
                if (size == 3) {
                    assertSame(map.keyAt(2), take3.keyAt(2));
                    assertSame(map.valueAt(2), take3.valueAt(2));
                }
            }

            final Map<K, V> take4 = map.take(4);
            assertEquals(size, take4.size());
            assertSame(firstKey, take4.keyAt(0));
            assertSame(firstValue, take4.valueAt(0));
            if (size > 1) {
                assertSame(map.keyAt(1), take4.keyAt(1));
                assertSame(map.valueAt(1), take4.valueAt(1));
                if (size == 3) {
                    assertSame(map.keyAt(2), take4.keyAt(2));
                    assertSame(map.valueAt(2), take4.valueAt(2));
                }
            }
        })));
    }

    @Test
    @Override
    default void testSkipLastWhenEmpty() {
        final Map<K, V> map = newBuilder().build();
        assertSame(map, map.skipLast(0));
        assertTrue(map.skipLast(1).isEmpty());
        assertTrue(map.skipLast(2).isEmpty());
        assertTrue(map.skipLast(24).isEmpty());
    }

    @Test
    @Override
    default void testSkipLast() {
        withKey(a -> withKey(b -> withKey(c -> {
            final V aValue = valueFromKey(a);
            final V bValue = valueFromKey(b);
            final V cValue = valueFromKey(c);
            final Map<K, V> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            assertSame(map, map.skipLast(0));

            final int size = map.size();
            final K firstKey = map.keyAt(0);
            final V firstValue = map.valueAt(0);
            final K secondKey = (size >= 2)? map.keyAt(1) : null;
            final V secondValue = (size >= 2)? map.valueAt(1) : null;

            final Map<K, V> map1 = map.skipLast(1);
            assertEquals(size - 1, map1.size());
            if (size >= 2) {
                assertSame(firstKey, map1.keyAt(0));
                assertSame(firstValue, map1.valueAt(0));
                if (size == 3) {
                    assertSame(secondKey, map1.keyAt(1));
                    assertSame(secondValue, map1.valueAt(1));
                }
            }

            final Map<K, V> map2 = map.skipLast(2);
            if (size < 3) {
                assertTrue(map2.isEmpty());
            }
            else {
                assertEquals(1, map2.size());
                assertSame(firstKey, map2.keyAt(0));
                assertSame(firstValue, map2.valueAt(0));
            }

            assertTrue(map.skipLast(3).isEmpty());
            assertTrue(map.skipLast(4).isEmpty());
            assertTrue(map.skipLast(24).isEmpty());
        })));
    }

    @Test
    default void testTakeLastWhenEmpty() {
        final Map<K, V> map = newBuilder().build();
        assertSame(map, map.takeLast(0));
        assertTrue(map.takeLast(1).isEmpty());
        assertTrue(map.takeLast(2).isEmpty());
        assertTrue(map.takeLast(24).isEmpty());
    }

    @Test
    default void testTakeLast() {
        withKey(a -> withKey(b -> withKey(c -> {
            final V aValue = valueFromKey(a);
            final V bValue = valueFromKey(b);
            final V cValue = valueFromKey(c);
            final Map<K, V> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            assertTrue(map.takeLast(0).isEmpty());

            final int size = map.size();
            final K firstKey = map.keyAt(0);
            final V firstValue = map.valueAt(0);
            final K secondKey = (size >= 2)? map.keyAt(1) : null;
            final V secondValue = (size >= 2)? map.valueAt(1) : null;
            final K thirdKey = (size >= 3)? map.keyAt(2) : null;
            final V thirdValue = (size >= 3)? map.valueAt(2) : null;

            final Map<K, V> take1 = map.takeLast(1);
            assertEquals(1, take1.size());
            assertSame((size == 1)? firstKey : (size == 2)? secondKey : thirdKey, take1.keyAt(0));
            assertSame((size == 1)? firstValue : (size == 2)? secondValue : thirdValue, take1.valueAt(0));

            final Map<K, V> take2 = map.takeLast(2);
            assertEquals(Math.min(size, 2), take2.size());
            if (size <= 2) {
                assertSame(firstKey, take2.keyAt(0));
                assertSame(firstValue, take2.valueAt(0));
                if (size == 2) {
                    assertSame(secondKey, take2.keyAt(1));
                    assertSame(secondValue, take2.valueAt(1));
                }
            }
            else {
                assertSame(secondKey, take2.keyAt(0));
                assertSame(secondValue, take2.valueAt(0));
                assertSame(thirdKey, take2.keyAt(1));
                assertSame(thirdValue, take2.valueAt(1));
            }

            final Map<K, V> take3 = map.takeLast(3);
            assertEquals(size, take3.size());
            assertSame(firstKey, take3.keyAt(0));
            assertSame(firstValue, take3.valueAt(0));
            if (size >= 2) {
                assertSame(secondKey, take3.keyAt(1));
                assertSame(secondValue, take3.valueAt(1));
                if (size == 3) {
                    assertSame(thirdKey, take3.keyAt(2));
                    assertSame(thirdValue, take3.valueAt(2));
                }
            }

            final Map<K, V> take4 = map.takeLast(3);
            assertEquals(size, take4.size());
            assertSame(firstKey, take4.keyAt(0));
            assertSame(firstValue, take4.valueAt(0));
            if (size >= 2) {
                assertSame(secondKey, take4.keyAt(1));
                assertSame(secondValue, take4.valueAt(1));
                if (size == 3) {
                    assertSame(thirdKey, take4.keyAt(2));
                    assertSame(thirdValue, take4.valueAt(2));
                }
            }
        })));
    }
}

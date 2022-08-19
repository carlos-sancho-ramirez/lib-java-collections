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

interface IntValueMapTest<K, B extends IntTransformableBuilder, MB extends IntValueMap.Builder<K>> extends IntTransformableTest<B> {

    MB newBuilder();

    void withMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<K, MB>> procedure);
    void withKey(Procedure<K> procedure);
    void withSortFunc(Procedure<SortFunction<K>> procedure);
    void withFilterByKeyFunc(Procedure<Predicate<K>> procedure);
    K keyFromInt(int value);

    default void withFilterByEntryFunc(Procedure<Predicate<IntValueMapEntry<K>>> procedure) {
        withFilterByKeyFunc(f -> procedure.apply(entry -> f.apply(entry.key())));
    }

    default void withArbitraryMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<K, IntValueMap.Builder<K>>> procedure) {
        procedure.apply(ImmutableIntValueHashMap.Builder::new);
        procedure.apply(MutableIntValueHashMap.Builder::new);
        withSortFunc(sortFunc -> {
            procedure.apply(() -> new ImmutableIntValueSortedMap.Builder<>(sortFunc));
            procedure.apply(() -> new MutableIntValueSortedMap.Builder<>(sortFunc));
        });
    }

    @Test
    default void testEmptyBuilderBuildsEmptyArray() {
        final IntValueMap<K> array = newBuilder().build();
        assertEquals(0, array.size());
        assertFalse(array.iterator().hasNext());
    }

    @Test
    default void testBuilderWithSingleElementBuildsExpectedArray() {
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
    default void testGet() {
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
    default void testKeyAtMethod() {
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

    default int valueFromKey(K str) {
        return (str != null)? str.hashCode() : 0;
    }

    @Test
    default void testValueAtMethod() {
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
    default void testKeySetWhenEmpty() {
        assertTrue(newBuilder().build().isEmpty());
    }

    @Test
    default void testKeySet() {
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
    default void testIndexOfKey() {
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
    default void testContainsKeyWhenEmpty() {
        final IntValueMap<K> map = newBuilder().build();
        withKey(key -> assertFalse(map.containsKey(key)));
    }

    @Test
    default void testContainsKey() {
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
    default void testEntryIterator() {
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
    default void testEqualMapReturnsFalseWhenAPairIsMissing() {
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
    default void testEqualMapReturnsFalseWhenKeyMatchesButNotValues() {
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
    default void testEqualMapReturnsTrueForOtherSortingsAndMutabilities() {
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
    default void testMutateMethod() {
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
            final int value = valueFromKey(key);
            final IntValueMap<K> map = newBuilder().put(key, value).build().sort(func);
            assertEquals(1, map.size());
            assertSame(key, map.keyAt(0));
            assertEquals(value, map.valueAt(0));
        });
    }

    @Test
    default void testSort() {
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
    @Override
    default void testFilterNotForSingleElement() {
        withFilterFunc(f -> withKey(key -> {
            final int value = valueFromKey(key);
            final IntValueMap<K> map = newBuilder().put(key, value).build();
            final IntValueMap<K> filtered = map.filterNot(f);

            if (f.apply(value)) {
                assertTrue(filtered.isEmpty());
            }
            else {
                assertTrue(map.equalMap(filtered));
            }
        }));
    }

    @Test
    default void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withKey(keyA -> withKey(keyB -> {
            final int valueA = valueFromKey(keyA);
            final int valueB = valueFromKey(keyB);
            final IntValueMap<K> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final IntValueMap<K> filtered = map.filterNot(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertTrue(filtered.isEmpty());
            }
            else if (aPassed) {
                Iterator<IntValueMap.Entry<K>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<K> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertEquals(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntValueMap.Entry<K>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<K> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertEquals(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertTrue(map.equalMap(filtered));
            }
        })));
    }

    @Test
    default void testFilterByEntryWhenEmpty() {
        final Predicate<IntValueMapEntry<K>> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().filterByEntry(f).iterator().hasNext());
        });
    }

    @Test
    default void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final IntValueMap.Entry<K> entry = new IntValueMap.Entry<>(0, key, valueFromKey(key));
            final IntValueMap<K> map = supplier.newBuilder().put(key, entry.value()).build();
            final IntValueMap<K> filtered = map.filterByEntry(f);

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
            final IntValueMap<K> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final IntValueMap<K> filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            int counter = 0;
            for (IntValueMap.Entry<K> entry : map.entries()) {
                if (f.apply(entry)) {
                    assertEquals(entry.value(), filtered.get(entry.key()));
                    counter++;
                }
            }
            assertEquals(filteredSize, counter);
        }))));
    }

    @Test
    default void testMapResultingKeysForMultipleElements() {
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
    @Override
    default void testMapToIntForMultipleElements() {
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

    @Test
    @Override
    default void testFilterForSingleElement() {
        withFilterFunc(f -> withInt(value -> {
            final K key = keyFromInt(value);
            final IntValueMap<K> map = newBuilder().put(key, value).build();
            final IntValueMap<K> filtered = map.filter(f);

            if (f.apply(value)) {
                assertTrue(map.equalMap(filtered));
            }
            else {
                assertTrue(filtered.isEmpty());
            }
        }));
    }

    @Test
    @Override
    default void testFilterForMultipleElements() {
        withFilterFunc(f -> withKey(keyA -> withKey(keyB -> {
            final int valueA = valueFromKey(keyA);
            final int valueB = valueFromKey(keyB);
            final IntValueMap<K> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final IntValueMap<K> filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertTrue(map.equalMap(filtered));
            }
            else if (aPassed) {
                Iterator<IntValueMap.Entry<K>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<K> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertEquals(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntValueMap.Entry<K>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<K> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertEquals(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertTrue(filtered.isEmpty());
            }
        })));
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
            final IntValueMap<K> map = supplier.newBuilder().put(key, valueFromKey(key)).build();
            final IntValueMap<K> filtered = map.filterByKey(f);

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
            final IntValueMap<K> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final IntValueMap<K> filtered = map.filterByKey(f);

            final Transformer<IntValueMap.Entry<K>> tr = filtered.entries().iterator();
            for (K key : map.keySet()) {
                if (f.apply(key)) {
                    assertTrue(tr.hasNext());
                    final IntValueMap.Entry<K> entry = tr.next();
                    assertEquals(map.get(key), entry.value());
                    assertSame(key, entry.key());
                }
            }
            assertFalse(tr.hasNext());
        }))));
    }

    @Test
    default void testSliceWhenEmpty() {
        final IntValueMap<K> map = newBuilder().build();
        assertTrue(map.slice(new ImmutableIntRange(0, 0)).isEmpty());
        assertTrue(map.slice(new ImmutableIntRange(1, 1)).isEmpty());
        assertTrue(map.slice(new ImmutableIntRange(2, 2)).isEmpty());
        assertTrue(map.slice(new ImmutableIntRange(0, 1)).isEmpty());
        assertTrue(map.slice(new ImmutableIntRange(1, 2)).isEmpty());
        assertTrue(map.slice(new ImmutableIntRange(0, 2)).isEmpty());
    }

    @Test
    default void testSlice() {
        withKey(a -> withKey(b -> withKey(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);
            final IntValueMap<K> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final K firstKey = map.keyAt(0);
            final K secondKey = (size >= 2)? map.keyAt(1) : null;
            final K thirdKey = (size >= 3)? map.keyAt(2) : null;
            final int firstValue = map.valueAt(0);
            final int secondValue = (size >= 2)? map.valueAt(1) : 0;
            final int thirdValue = (size >= 3)? map.valueAt(2) : 0;

            final IntValueMap<K> sliceA = map.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(firstKey, sliceA.keyAt(0));
            assertEquals(firstValue, sliceA.valueAt(0));

            final IntValueMap<K> sliceB = map.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(secondKey, sliceB.keyAt(0));
                assertEquals(secondValue, sliceB.valueAt(0));
            }
            else {
                assertTrue(sliceB.isEmpty());
            }

            final IntValueMap<K> sliceC = map.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(thirdKey, sliceC.keyAt(0));
                assertEquals(thirdValue, sliceC.valueAt(0));
            }
            else {
                assertTrue(sliceC.isEmpty());
            }

            final IntValueMap<K> sliceAB = map.slice(new ImmutableIntRange(0, 1));
            if (size >= 2) {
                assertEquals(2, sliceAB.size());
                assertSame(secondKey, sliceAB.keyAt(1));
                assertEquals(secondValue, sliceAB.valueAt(1));
            }
            else {
                assertEquals(1, sliceAB.size());
            }
            assertSame(firstKey, sliceAB.keyAt(0));
            assertEquals(firstValue, sliceAB.valueAt(0));

            final IntValueMap<K> sliceBC = map.slice(new ImmutableIntRange(1, 2));
            if (size == 1) {
                assertTrue(sliceBC.isEmpty());
            }
            else if (size == 2) {
                assertEquals(1, sliceBC.size());
                assertSame(secondKey, sliceBC.keyAt(0));
                assertEquals(secondValue, sliceBC.valueAt(0));
            }
            else {
                assertEquals(2, sliceBC.size());
                assertSame(secondKey, sliceBC.keyAt(0));
                assertEquals(secondValue, sliceBC.valueAt(0));
                assertSame(thirdKey, sliceBC.keyAt(1));
                assertEquals(thirdValue, sliceBC.valueAt(1));
            }

            final IntValueMap<K> sliceABC = map.slice(new ImmutableIntRange(0, 2));
            assertEquals(size, sliceABC.size());
            assertSame(firstKey, sliceABC.keyAt(0));
            assertEquals(firstValue, sliceABC.valueAt(0));
            if (size >= 2) {
                assertSame(secondKey, sliceABC.keyAt(1));
                assertEquals(secondValue, sliceABC.valueAt(1));
                if (size == 3) {
                    assertSame(thirdKey, sliceABC.keyAt(2));
                    assertEquals(thirdValue, sliceABC.valueAt(2));
                }
            }

            final IntValueMap<K> sliceABCD = map.slice(new ImmutableIntRange(0, 3));
            assertEquals(size, sliceABCD.size());
            assertSame(firstKey, sliceABCD.keyAt(0));
            assertEquals(firstValue, sliceABCD.valueAt(0));
            if (size >= 2) {
                assertSame(secondKey, sliceABCD.keyAt(1));
                assertEquals(secondValue, sliceABCD.valueAt(1));
                if (size == 3) {
                    assertSame(thirdKey, sliceABCD.keyAt(2));
                    assertEquals(thirdValue, sliceABCD.valueAt(2));
                }
            }
        })));
    }

    @Test
    @Override
    default void testSkipWhenEmpty() {
        final IntValueMap<K> map = newBuilder().build();
        assertSame(map, map.skip(0));
        assertTrue(map.skip(1).isEmpty());
        assertTrue(map.skip(20).isEmpty());
    }

    @Test
    @Override
    default void testSkip() {
        withKey(a -> withKey(b -> withKey(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);

            final IntValueMap<K> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            final int size = map.size();
            final K secondKey = (size >= 2)? map.keyAt(1) : null;
            final int secondValue = (size >= 2)? map.valueAt(1) : 0;
            final K thirdKey = (size == 3)? map.keyAt(2) : null;
            final int thirdValue = (size == 3)? map.valueAt(2) : 0;

            assertSame(map, map.skip(0));

            final IntValueMap<K> skip1 = map.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertSame(secondKey, skip1.keyAt(0));
                assertEquals(secondValue, skip1.valueAt(0));
                if (size == 3) {
                    assertSame(thirdKey, skip1.keyAt(1));
                    assertEquals(thirdValue, skip1.valueAt(1));
                }
            }

            final IntValueMap<K> skip2 = map.skip(2);
            if (size == 3) {
                assertSame(thirdKey, skip2.keyAt(0));
                assertEquals(thirdValue, skip2.valueAt(0));
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
    default void testTakeWhenEmpty() {
        final IntValueMap<K> map = newBuilder().build();
        assertTrue(map.take(0).isEmpty());
        assertTrue(map.take(1).isEmpty());
        assertTrue(map.take(2).isEmpty());
        assertTrue(map.take(24).isEmpty());
    }

    @Test
    default void testTake() {
        withKey(a -> withKey(b -> withKey(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);
            final IntValueMap<K> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final K firstKey = map.keyAt(0);
            final int firstValue = map.valueAt(0);

            assertTrue(map.take(0).isEmpty());

            final IntValueMap<K> take1 = map.take(1);
            assertEquals(1, take1.size());
            assertSame(firstKey, take1.keyAt(0));
            assertEquals(firstValue, take1.valueAt(0));

            final IntValueMap<K> take2 = map.take(2);
            assertSame(firstKey, take2.keyAt(0));
            assertEquals(firstValue, take2.valueAt(0));
            if (size == 1) {
                assertEquals(1, take2.size());
            }
            else {
                assertEquals(2, take2.size());
                assertSame(map.keyAt(1), take2.keyAt(1));
                assertEquals(map.valueAt(1), take2.valueAt(1));
            }

            final IntValueMap<K> take3 = map.take(3);
            assertEquals(size, take3.size());
            assertSame(firstKey, take3.keyAt(0));
            assertEquals(firstValue, take3.valueAt(0));
            if (size > 1) {
                assertSame(map.keyAt(1), take3.keyAt(1));
                assertEquals(map.valueAt(1), take3.valueAt(1));
                if (size == 3) {
                    assertSame(map.keyAt(2), take3.keyAt(2));
                    assertEquals(map.valueAt(2), take3.valueAt(2));
                }
            }

            final IntValueMap<K> take4 = map.take(4);
            assertEquals(size, take4.size());
            assertSame(firstKey, take4.keyAt(0));
            assertEquals(firstValue, take4.valueAt(0));
            if (size > 1) {
                assertSame(map.keyAt(1), take4.keyAt(1));
                assertEquals(map.valueAt(1), take4.valueAt(1));
                if (size == 3) {
                    assertSame(map.keyAt(2), take4.keyAt(2));
                    assertEquals(map.valueAt(2), take4.valueAt(2));
                }
            }
        })));
    }
}

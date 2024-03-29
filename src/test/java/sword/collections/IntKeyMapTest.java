package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withInt;

interface IntKeyMapTest<T, B extends TransformableBuilder<T>, MB extends IntKeyMapBuilder<T>> extends TransformableTest<T, B> {

    MB newMapBuilder();
    T getTestValue();
    T getTestValue2();
    T valueFromKey(int key);
    void withMapBuilderSupplier(Procedure<IntKeyMapBuilderSupplier<T, MB>> procedure);

    default void withFilterByKeyFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(a -> (a & 1) == 0);
        procedure.apply(a -> a < 0);
    }

    default void withFilterByEntryFunc(Procedure<Predicate<IntKeyMapEntry<T>>> procedure) {
        withFilterByKeyFunc(f -> procedure.apply(entry -> f.apply(entry.key())));
    }

    default void withArbitraryMapBuilderSupplier(Procedure<IntKeyMapBuilderSupplier<T, IntKeyMapBuilder<T>>> procedure) {
        procedure.apply(ImmutableIntKeyMap.Builder::new);
        procedure.apply(MutableIntKeyMap.Builder::new);
    }

    @Test
    default void testEmptyBuilderBuildsEmptyArray() {
        IntKeyMapBuilder<T> builder = newMapBuilder();
        IntKeyMap<T> array = builder.build();
        assertEquals(0, array.size());
    }

    @Test
    default void testSize() {
        final T value = getTestValue();
        withInt(a -> withInt(b -> withInt(c -> withInt(d -> {
            IntKeyMapBuilder<T> builder = newMapBuilder();
            IntKeyMap<T> array = builder
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .put(d, value)
                    .build();

            int expectedSize = 1;
            if (b != a) {
                expectedSize++;
            }

            if (c != b && c != a) {
                expectedSize++;
            }

            if (d != c && d != b && d != a) {
                expectedSize++;
            }

            assertEquals(expectedSize, array.size());
        }))));
    }

    @Test
    default void testGet() {
        final T value = getTestValue();
        final T defValue = getTestValue2();
        withInt(a -> withInt(b -> {
            IntKeyMapBuilder<T> builder = newMapBuilder();
            IntKeyMap<T> array = builder
                    .put(a, value)
                    .put(b, value)
                    .build();

            withInt(other -> {
                final T expectedValue = (other == a || other == b)? value : defValue;
                assertEquals(expectedValue, array.get(other, defValue));
            });
        }));
    }

    @Test
    default void testKeyAtMethod() {
        final T value = getTestValue();
        withInt(a -> withInt(b -> withInt(c -> {
            IntKeyMapBuilder<T> builder = newMapBuilder();
            IntKeyMap<T> array = builder
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .build();

            int lastKey = array.keyAt(0);
            assertTrue(lastKey == a || lastKey == b || lastKey == c);

            final int size = array.size();
            for (int i = 1; i < size; i++) {
                int newKey = array.keyAt(i);
                assertTrue(newKey > lastKey);

                lastKey = newKey;
                assertTrue(lastKey == a || lastKey == b || lastKey == c);
            }
        })));
    }

    @Test
    default void testValueAtMethod() {
        withInt(a -> withInt(b -> withInt(c -> {
            IntKeyMapBuilder<T> builder = newMapBuilder();
            IntKeyMap<T> array = builder
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int size = array.size();
            for (int i = 0; i < size; i++) {
                final int key = array.keyAt(i);
                assertEquals(valueFromKey(key), array.valueAt(i));
            }
        })));
    }

    @Test
    default void testKeySetWhenEmpty() {
        final IntKeyMapBuilder<T> builder = newMapBuilder();
        final IntKeyMap<T> map = builder.build();
        assertTrue(map.keySet().isEmpty());
    }

    @Test
    default void testKeySet() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntKeyMapBuilder<T> builder = newMapBuilder();
            final IntKeyMap<T> map = builder
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final ImmutableIntSet set = new ImmutableIntSetCreator()
                    .add(a).add(b).add(c).build();
            assertEquals(set, map.keySet().toImmutable());
        })));
    }

    @Test
    default void testIndexOfKey() {
        withInt(a -> withInt(b -> withInt(c -> {
            final T value = getTestValue();
            final IntKeyMapBuilder<T> builder = newMapBuilder();
            final IntKeyMap<T> map = builder
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
        final IntKeyMap<T> map = newMapBuilder().build();
        withInt(key -> assertFalse(map.containsKey(key)));
    }

    @Test
    default void testContainsKey() {
        withInt(a -> withInt(b -> withInt(c -> {
            final T value = getTestValue();
            final IntKeyMap<T> map = newMapBuilder()
                    .put(a, value)
                    .put(b, value)
                    .build();

            final boolean expectedResult = c == a || c == b;
            assertEquals(expectedResult, map.containsKey(c));
        })));
    }

    @Test
    default void testEntryIterator() {
        withInt(a -> withInt(b -> withInt(c -> {
            IntKeyMapBuilder<T> builder = newMapBuilder();
            IntKeyMap<T> array = builder
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int size = array.size();
            final Iterator<IntKeyMap.Entry<T>> iterator = array.entries().iterator();
            for (int i = 0; i < size; i++) {
                assertTrue(iterator.hasNext());

                final IntKeyMap.Entry<T> entry = iterator.next();
                assertEquals(i, entry.index());
                assertEquals(array.keyAt(i), entry.key());
                assertEquals(array.valueAt(i), entry.value());
            }

            assertFalse(iterator.hasNext());
        })));
    }

    @Test
    default void testEqualMapReturnsFalseWhenAPairIsMissing() {
        withInt(a -> withInt(b -> withInt(c -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int mapSize = map.size();
            final IntKeyMapBuilder<T> mapBuilder = supplier.newBuilder();
            for (int i = 1; i < mapSize; i++) {
                mapBuilder.put(map.keyAt(i), map.valueAt(i));
            }
            final IntKeyMap<T> reducedMap = mapBuilder.build();

            assertFalse(map.equalMap(reducedMap));
            assertFalse(reducedMap.equalMap(map));
        }))));
    }

    @Test
    default void testEqualMapReturnsFalseWhenKeyMatchesButNotValues() {
        withInt(a -> withInt(b -> withInt(c -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int mapSize = map.size();
            for (int j = 0; j < mapSize; j++) {
                final IntKeyMapBuilder<T> mapBuilder = supplier.newBuilder();
                for (int i = 0; i < mapSize; i++) {
                    T value = (i == j) ? null : map.valueAt(i);
                    mapBuilder.put(map.keyAt(i), value);
                }
                final IntKeyMap<T> modifiedMap = mapBuilder.build();

                assertFalse(map.equalMap(modifiedMap));
                assertFalse(modifiedMap.equalMap(map));
            }
        }))));
    }

    @Test
    default void testEqualMapReturnsTrueForOtherSortingsAndMutabilities() {
        withInt(a -> withInt(b -> withInt(c -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            withArbitraryMapBuilderSupplier(mapSupplier -> {
                final IntKeyMap<T> arbitraryMap = mapSupplier.newBuilder()
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
        withMapFunc(f -> withInt(keyA -> withInt(keyB -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder()
                    .put(keyA, valueFromKey(keyA))
                    .put(keyB, valueFromKey(keyB))
                    .build();
            final IntKeyMap<String> mapped = map.map(f);

            final int size = map.size();
            assertEquals(size, mapped.size());

            for (int i = 0; i < size; i++) {
                assertEquals(map.keyAt(i), mapped.keyAt(i));
            }
        }))));
    }

    @Test
    default void testMapToIntForMultipleElements() {
        withMapToIntFunc(f -> withInt(a -> withInt(b -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final IntPairMap mapped = map.mapToInt(f);

            final int size = map.size();
            assertEquals(size, mapped.size());

            for (int i = 0; i < size; i++) {
                assertEquals(map.keyAt(i), mapped.keyAt(i));
            }
        }))));
    }

    @Test
    default void testFilterByKeyWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().filterByKey(f).iterator().hasNext());
        });
    }

    @Test
    default void testFilterByKeyForSingleElement() {
        withFilterByKeyFunc(f -> withInt(key -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder().put(key, valueFromKey(key)).build();
            final IntKeyMap<T> filtered = map.filterByKey(f);

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
        withFilterByKeyFunc(f -> withInt(a -> withInt(b -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final IntKeyMap<T> filtered = map.filterByKey(f);

            final Transformer<IntKeyMap.Entry<T>> tr = filtered.entries().iterator();
            for (int key : map.keySet()) {
                if (f.apply(key)) {
                    assertTrue(tr.hasNext());
                    final IntKeyMap.Entry<T> entry = tr.next();
                    assertSame(map.get(key), entry.value());
                    assertEquals(key, entry.key());
                }
            }
            assertFalse(tr.hasNext());
        }))));
    }

    @Test
    default void testFilterByEntryWhenEmpty() {
        final Predicate<IntKeyMapEntry<T>> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().filterByEntry(f).iterator().hasNext());
        });
    }

    @Test
    default void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withInt(key -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap.Entry<T> entry = new IntKeyMap.Entry<>(0, key, valueFromKey(key));
            final IntKeyMap<T> map = supplier.newBuilder().put(key, entry.value()).build();
            final IntKeyMap<T> filtered = map.filterByEntry(f);

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
        withFilterByEntryFunc(f -> withInt(a -> withInt(b -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final IntKeyMap<T> filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            int counter = 0;
            for (IntKeyMap.Entry<T> entry : map.entries()) {
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
        withInt(a -> withInt(b -> withInt(c -> {
            final T aValue = valueFromKey(a);
            final T bValue = valueFromKey(b);
            final T cValue = valueFromKey(c);
            final IntKeyMap<T> map = newMapBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final int firstKey = map.keyAt(0);
            final int secondKey = (size >= 2)? map.keyAt(1) : 0;
            final int thirdKey = (size >= 3)? map.keyAt(2) : 0;
            final T firstValue = map.valueAt(0);
            final T secondValue = (size >= 2)? map.valueAt(1) : null;
            final T thirdValue = (size >= 3)? map.valueAt(2) : null;

            final IntKeyMap<T> sliceA = map.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertEquals(firstKey, sliceA.keyAt(0));
            assertSame(firstValue, sliceA.valueAt(0));

            final IntKeyMap<T> sliceB = map.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertEquals(secondKey, sliceB.keyAt(0));
                assertSame(secondValue, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final IntKeyMap<T> sliceC = map.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertEquals(thirdKey, sliceC.keyAt(0));
                assertSame(thirdValue, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final IntKeyMap<T> sliceAB = map.slice(new ImmutableIntRange(0, 1));
            if (size >= 2) {
                assertEquals(2, sliceAB.size());
                assertEquals(secondKey, sliceAB.keyAt(1));
                assertSame(secondValue, sliceAB.valueAt(1));
            }
            else {
                assertEquals(1, sliceAB.size());
            }
            assertEquals(firstKey, sliceAB.keyAt(0));
            assertSame(firstValue, sliceAB.valueAt(0));

            final IntKeyMap<T> sliceBC = map.slice(new ImmutableIntRange(1, 2));
            if (size == 1) {
                assertEquals(0, sliceBC.size());
            }
            else if (size == 2) {
                assertEquals(1, sliceBC.size());
                assertEquals(secondKey, sliceBC.keyAt(0));
                assertSame(secondValue, sliceBC.valueAt(0));
            }
            else {
                assertEquals(2, sliceBC.size());
                assertEquals(secondKey, sliceBC.keyAt(0));
                assertSame(secondValue, sliceBC.valueAt(0));
                assertEquals(thirdKey, sliceBC.keyAt(1));
                assertSame(thirdValue, sliceBC.valueAt(1));
            }

            assertSame(map, map.slice(new ImmutableIntRange(0, 2)));
            assertSame(map, map.slice(new ImmutableIntRange(0, 3)));
        })));
    }

    @Test
    @Override
    default void testSkipWhenEmpty() {
        final IntKeyMap<T> map = newMapBuilder().build();
        assertSame(map, map.skip(0));
        assertTrue(map.skip(1).isEmpty());
        assertTrue(map.skip(20).isEmpty());
    }

    @Test
    @Override
    default void testSkip() {
        withInt(a -> withInt(b -> withInt(c -> {
            final T aValue = valueFromKey(a);
            final T bValue = valueFromKey(b);
            final T cValue = valueFromKey(c);

            final IntKeyMap<T> map = newMapBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            final int size = map.size();
            final int secondKey = (size >= 2)? map.keyAt(1) : 0;
            final T secondValue = (size >= 2)? map.valueAt(1) : null;
            final int thirdKey = (size == 3)? map.keyAt(2) : 0;
            final T thirdValue = (size == 3)? map.valueAt(2) : null;

            assertSame(map, map.skip(0));

            final IntKeyMap<T> skip1 = map.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertEquals(secondKey, skip1.keyAt(0));
                assertSame(secondValue, skip1.valueAt(0));
                if (size == 3) {
                    assertEquals(thirdKey, skip1.keyAt(1));
                    assertSame(thirdValue, skip1.valueAt(1));
                }
            }

            final IntKeyMap<T> skip2 = map.skip(2);
            if (size == 3) {
                assertEquals(thirdKey, skip2.keyAt(0));
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
        final IntKeyMap<T> map = newMapBuilder().build();
        assertTrue(map.take(0).isEmpty());
        assertTrue(map.take(1).isEmpty());
        assertTrue(map.take(2).isEmpty());
        assertTrue(map.take(24).isEmpty());
    }

    @Test
    @Override
    default void testTake() {
        withInt(a -> withInt(b -> withInt(c -> {
            final T aValue = valueFromKey(a);
            final T bValue = valueFromKey(b);
            final T cValue = valueFromKey(c);
            final IntKeyMap<T> map = newMapBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final int firstKey = map.keyAt(0);
            final T firstValue = map.valueAt(0);

            assertTrue(map.take(0).isEmpty());

            final IntKeyMap<T> take1 = map.take(1);
            assertEquals(1, take1.size());
            assertEquals(firstKey, take1.keyAt(0));
            assertSame(firstValue, take1.valueAt(0));

            final IntKeyMap<T> take2 = map.take(2);
            assertEquals(firstKey, take2.keyAt(0));
            assertSame(firstValue, take2.valueAt(0));
            if (size == 1) {
                assertEquals(1, take2.size());
            }
            else {
                assertEquals(2, take2.size());
                assertEquals(map.keyAt(1), take2.keyAt(1));
                assertSame(map.valueAt(1), take2.valueAt(1));
            }

            final IntKeyMap<T> take3 = map.take(3);
            assertEquals(size, take3.size());
            assertEquals(firstKey, take3.keyAt(0));
            assertSame(firstValue, take3.valueAt(0));
            if (size > 1) {
                assertEquals(map.keyAt(1), take3.keyAt(1));
                assertSame(map.valueAt(1), take3.valueAt(1));
                if (size == 3) {
                    assertEquals(map.keyAt(2), take3.keyAt(2));
                    assertSame(map.valueAt(2), take3.valueAt(2));
                }
            }

            final IntKeyMap<T> take4 = map.take(4);
            assertEquals(size, take4.size());
            assertEquals(firstKey, take4.keyAt(0));
            assertSame(firstValue, take4.valueAt(0));
            if (size > 1) {
                assertEquals(map.keyAt(1), take4.keyAt(1));
                assertSame(map.valueAt(1), take4.valueAt(1));
                if (size == 3) {
                    assertEquals(map.keyAt(2), take4.keyAt(2));
                    assertSame(map.valueAt(2), take4.valueAt(2));
                }
            }
        })));
    }

    @Test
    @Override
    default void testSkipLastWhenEmpty() {
        final IntKeyMap<T> map = newMapBuilder().build();
        assertSame(map, map.skipLast(0));
        assertTrue(map.skipLast(1).isEmpty());
        assertTrue(map.skipLast(2).isEmpty());
        assertTrue(map.skipLast(24).isEmpty());
    }

    @Test
    @Override
    default void testSkipLast() {
        withInt(a -> withInt(b -> withInt(c -> {
            final T aValue = valueFromKey(a);
            final T bValue = valueFromKey(b);
            final T cValue = valueFromKey(c);
            final IntKeyMap<T> map = newMapBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            assertSame(map, map.skipLast(0));

            final int size = map.size();
            final int firstKey = map.keyAt(0);
            final T firstValue = map.valueAt(0);
            final int secondKey = (size >= 2)? map.keyAt(1) : 0;
            final T secondValue = (size >= 2)? map.valueAt(1) : null;

            final IntKeyMap<T> map1 = map.skipLast(1);
            assertEquals(size - 1, map1.size());
            if (size >= 2) {
                assertEquals(firstKey, map1.keyAt(0));
                assertSame(firstValue, map1.valueAt(0));
                if (size == 3) {
                    assertEquals(secondKey, map1.keyAt(1));
                    assertSame(secondValue, map1.valueAt(1));
                }
            }

            final IntKeyMap<T> map2 = map.skipLast(2);
            if (size < 3) {
                assertTrue(map2.isEmpty());
            }
            else {
                assertEquals(1, map2.size());
                assertEquals(firstKey, map2.keyAt(0));
                assertSame(firstValue, map2.valueAt(0));
            }

            assertTrue(map.skipLast(3).isEmpty());
            assertTrue(map.skipLast(4).isEmpty());
            assertTrue(map.skipLast(24).isEmpty());
        })));
    }

    @Test
    @Override
    default void testTakeLastWhenEmpty() {
        final IntKeyMap<T> map = newMapBuilder().build();
        assertSame(map, map.takeLast(0));
        assertTrue(map.takeLast(1).isEmpty());
        assertTrue(map.takeLast(2).isEmpty());
        assertTrue(map.takeLast(24).isEmpty());
    }

    @Test
    @Override
    default void testTakeLast() {
        withInt(a -> withInt(b -> withInt(c -> {
            final T aValue = valueFromKey(a);
            final T bValue = valueFromKey(b);
            final T cValue = valueFromKey(c);
            final IntKeyMap<T> map = newMapBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            assertTrue(map.takeLast(0).isEmpty());

            final int size = map.size();
            final int firstKey = map.keyAt(0);
            final T firstValue = map.valueAt(0);
            final int secondKey = (size >= 2)? map.keyAt(1) : 0;
            final T secondValue = (size >= 2)? map.valueAt(1) : null;
            final int thirdKey = (size >= 3)? map.keyAt(2) : 0;
            final T thirdValue = (size >= 3)? map.valueAt(2) : null;

            final IntKeyMap<T> take1 = map.takeLast(1);
            assertEquals(1, take1.size());
            assertEquals((size == 1)? firstKey : (size == 2)? secondKey : thirdKey, take1.keyAt(0));
            assertSame((size == 1)? firstValue : (size == 2)? secondValue : thirdValue, take1.valueAt(0));

            final IntKeyMap<T> take2 = map.takeLast(2);
            assertEquals(Math.min(size, 2), take2.size());
            if (size <= 2) {
                assertEquals(firstKey, take2.keyAt(0));
                assertSame(firstValue, take2.valueAt(0));
                if (size == 2) {
                    assertEquals(secondKey, take2.keyAt(1));
                    assertSame(secondValue, take2.valueAt(1));
                }
            }
            else {
                assertEquals(secondKey, take2.keyAt(0));
                assertSame(secondValue, take2.valueAt(0));
                assertEquals(thirdKey, take2.keyAt(1));
                assertSame(thirdValue, take2.valueAt(1));
            }

            final IntKeyMap<T> take3 = map.takeLast(3);
            assertEquals(size, take3.size());
            assertEquals(firstKey, take3.keyAt(0));
            assertSame(firstValue, take3.valueAt(0));
            if (size >= 2) {
                assertEquals(secondKey, take3.keyAt(1));
                assertSame(secondValue, take3.valueAt(1));
                if (size == 3) {
                    assertEquals(thirdKey, take3.keyAt(2));
                    assertSame(thirdValue, take3.valueAt(2));
                }
            }

            final IntKeyMap<T> take4 = map.takeLast(3);
            assertEquals(size, take4.size());
            assertEquals(firstKey, take4.keyAt(0));
            assertSame(firstValue, take4.valueAt(0));
            if (size >= 2) {
                assertEquals(secondKey, take4.keyAt(1));
                assertSame(secondValue, take4.valueAt(1));
                if (size == 3) {
                    assertEquals(thirdKey, take4.keyAt(2));
                    assertSame(thirdValue, take4.valueAt(2));
                }
            }
        })));
    }
}

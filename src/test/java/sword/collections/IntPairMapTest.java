package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withInt;

interface IntPairMapTest<B extends IntTransformableBuilder, MB extends IntPairMapBuilder> extends IntTransformableTest<B> {

    MB newBuilder();
    void withMapBuilderSupplier(Procedure<IntPairMapBuilderSupplier<MB>> procedure);

    default int valueFromKey(int key) {
        return key + 7;
    }

    default void withFilterByEntryFunc(Procedure<Predicate<IntPairMapEntry>> procedure) {
        withFilterByKeyFunc(f -> procedure.apply(entry -> f.apply(entry.key())));
    }

    default void withFilterByKeyFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(a -> (a & 1) == 0);
        procedure.apply(a -> a < 0);
    }

    default void withArbitraryMapBuilderSupplier(Procedure<IntPairMapBuilderSupplier<IntPairMapBuilder>> procedure) {
        procedure.apply(ImmutableIntPairMap.Builder::new);
        procedure.apply(MutableIntPairMap.Builder::new);
    }

    @Test
    default void testEmptyBuilderBuildsEmptyArray() {
        IntPairMap array = newBuilder().build();
        assertEquals(0, array.size());
    }

    @Test
    default void testGet() {
        final int defValue = -3;
        final int value = 21;
        withInt(a -> withInt(b -> {
            IntPairMap array = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .build();

            withInt(other -> {
                final int expectedValue = (other == a || other == b)? value : defValue;
                assertEquals(expectedValue, array.get(other, defValue));
            });
        }));
    }

    @Test
    default void testKeyAtMethod() {
        withInt(value -> withInt(a -> withInt(b -> withInt(c -> {
            IntPairMap array = newBuilder()
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
        }))));
    }

    @Test
    default void testValueAtMethod() {
        withInt(a -> withInt(b -> withInt(c -> {
            IntPairMap array = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .put(c, c)
                    .build();

            final int size = array.size();
            for (int i = 1; i < size; i++) {
                final int key = array.keyAt(i);
                assertEquals(key, array.valueAt(i));
            }
        })));
    }

    @Test
    default void testKeySet() {
        for (int amount = 0; amount < 3; amount++) {
            final IntPairMapBuilder mapBuilder = newBuilder();
            final ImmutableIntSetCreator setBuilder = new ImmutableIntSetCreator();
            for (int i = 2; i < amount + 2; i++) {
                setBuilder.add(i);
                mapBuilder.put(i, 5);
            }

            final ImmutableIntSet expectedKeys = setBuilder.build();
            final ImmutableIntSet keySet = mapBuilder.build().keySet().toImmutable();
            assertEquals(expectedKeys, keySet);
        }
    }

    @Test
    default void testIndexOfKey() {
        withInt(a -> withInt(b -> withInt(c -> {
            final int value = 34;
            final IntPairMap map = newBuilder()
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
        final IntPairMap map = newBuilder().build();
        withInt(key -> assertFalse(map.containsKey(key)));
    }

    @Test
    default void testContainsKey() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntPairMap map = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .build();

            final boolean expectedResult = c == a || c == b;
            assertEquals(expectedResult, map.containsKey(c));
        })));
    }

    @Test
    default void testEntryIterator() {
        withInt(a -> withInt(b -> withInt(c -> {
            IntPairMap array = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .put(c, c)
                    .build();

            final int size = array.size();
            final Iterator<IntPairMap.Entry> iterator = array.entries().iterator();
            for (int i = 0; i < size; i++) {
                assertTrue(iterator.hasNext());

                final IntPairMap.Entry entry = iterator.next();
                assertEquals(i, entry.index());
                assertEquals(array.keyAt(i), entry.key());
                assertEquals(array.valueAt(i), entry.value());
            }

            assertFalse(iterator.hasNext());
        })));
    }

    @Test
    default void testEqualMapReturnsFalseWhenAPairIsMissing() {
        withInt(a -> withInt(b -> withInt(c -> withMapToIntFunc(mapFunc -> withMapBuilderSupplier(supplier -> {
            final IntPairMap map = supplier.newBuilder()
                    .put(a, mapFunc.apply(a))
                    .put(b, mapFunc.apply(b))
                    .put(c, mapFunc.apply(c))
                    .build();

            final int mapSize = map.size();
            final IntPairMapBuilder mapBuilder = supplier.newBuilder();
            for (int i = 1; i < mapSize; i++) {
                mapBuilder.put(map.keyAt(i), map.valueAt(i));
            }
            final IntPairMap reducedMap = mapBuilder.build();

            assertFalse(map.equalMap(reducedMap));
            assertFalse(reducedMap.equalMap(map));
        })))));
    }

    @Test
    default void testEqualMapReturnsFalseWhenKeyMatchesButNotValues() {
        withInt(a -> withInt(b -> withInt(c -> withMapToIntFunc(mapFunc -> withMapBuilderSupplier(supplier -> {
            final IntPairMap map = supplier.newBuilder()
                    .put(a, mapFunc.apply(a))
                    .put(b, mapFunc.apply(b))
                    .put(c, mapFunc.apply(c))
                    .build();

            final int mapSize = map.size();
            for (int j = 0; j < mapSize; j++) {
                final IntPairMapBuilder mapBuilder = supplier.newBuilder();
                for (int i = 0; i < mapSize; i++) {
                    final int mapValue = map.valueAt(i);
                    final int value = (i == j)? ~mapValue : mapValue;
                    mapBuilder.put(map.keyAt(i), value);
                }
                final IntPairMap modifiedMap = mapBuilder.build();

                assertFalse(map.equalMap(modifiedMap));
                assertFalse(modifiedMap.equalMap(map));
            }
        })))));
    }

    @Test
    default void testEqualMapReturnsTrueForOtherSortingsAndMutabilities() {
        withInt(a -> withInt(b -> withInt(c -> withMapToIntFunc(mapFunc -> withMapBuilderSupplier(supplier -> {
            final IntPairMap map = supplier.newBuilder()
                    .put(a, mapFunc.apply(a))
                    .put(b, mapFunc.apply(b))
                    .put(c, mapFunc.apply(c))
                    .build();

            withArbitraryMapBuilderSupplier(mapSupplier -> {
                final IntPairMap arbitraryMap = mapSupplier.newBuilder()
                        .put(a, mapFunc.apply(a))
                        .put(b, mapFunc.apply(b))
                        .put(c, mapFunc.apply(c))
                        .build();

                assertTrue(map.equalMap(arbitraryMap));
            });
        })))));
    }

    @Test
    @Override
    default void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final IntPairMap map = newBuilder().build();
            assertTrue(map.filter(f).isEmpty());
        });
    }

    @Test
    @Override
    default void testFilterForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final int value = key * key;
            final IntPairMap map = newBuilder().put(key, value).build();
            final IntPairMap filtered = map.filter(f);

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
        withFilterFunc(f -> withInt(keyA -> withInt(keyB -> {
            final int valueA = keyA * keyA;
            final int valueB = keyB * keyB;
            final IntPairMap map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final IntPairMap filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertTrue(map.equalMap(filtered));
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
                assertTrue(filtered.isEmpty());
            }
        })));
    }

    @Test
    @Override
    default void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final IntPairMap map = newBuilder().build();
            assertTrue(map.filterNot(f).isEmpty());
        });
    }

    @Test
    @Override
    default void testFilterNotForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final int value = key * key;
            final IntPairMap map = newBuilder().put(key, value).build();
            final IntPairMap filtered = map.filterNot(f);

            if (f.apply(value)) {
                assertTrue(filtered.isEmpty());
            }
            else {
                assertTrue(map.equalMap(filtered));
            }
        }));
    }

    @Test
    @Override
    default void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withInt(keyA -> withInt(keyB -> {
            final int valueA = keyA * keyA;
            final int valueB = keyB * keyB;
            final IntPairMap map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final IntPairMap filtered = map.filterNot(f);

            final boolean aRemoved = f.apply(valueA);
            final boolean bRemoved = f.apply(valueB);

            if (aRemoved && bRemoved) {
                assertTrue(filtered.isEmpty());
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
                assertTrue(map.equalMap(filtered));
            }
        })));
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
            final IntPairMap map = supplier.newBuilder().put(key, valueFromKey(key)).build();
            final IntPairMap filtered = map.filterByKey(f);

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
            final IntPairMap map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final IntPairMap filtered = map.filterByKey(f);

            final Transformer<IntPairMap.Entry> tr = filtered.entries().iterator();
            for (int key : map.keySet()) {
                if (f.apply(key)) {
                    assertTrue(tr.hasNext());
                    final IntPairMap.Entry entry = tr.next();
                    assertEquals(map.get(key), entry.value());
                    assertEquals(key, entry.key());
                }
            }
            assertFalse(tr.hasNext());
        }))));
    }

    @Test
    default void testFilterByEntryWhenEmpty() {
        final Predicate<IntPairMapEntry> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().filterByEntry(f).iterator().hasNext());
        });
    }

    @Test
    default void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withInt(key -> withMapBuilderSupplier(supplier -> {
            final IntPairMap.Entry entry = new IntPairMap.Entry(0, key, valueFromKey(key));
            final IntPairMap map = supplier.newBuilder().put(key, entry.value()).build();
            final IntPairMap filtered = map.filterByEntry(f);

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
            final IntPairMap map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final IntPairMap filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            int counter = 0;
            for (IntPairMap.Entry entry : map.entries()) {
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
        withMapFunc(f -> withInt(a -> withInt(b -> {
            final IntPairMap map = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .build();
            final IntKeyMap<String> mapped = map.map(f);

            final int size = map.size();
            assertEquals(size, mapped.size());

            for (int i = 0; i < size; i++) {
                assertEquals(map.keyAt(i), mapped.keyAt(i));
            }
        })));
    }

    @Test
    @Override
    default void testMapToIntForMultipleElements() {
        withMapToIntFunc(f -> withInt(a -> withInt(b -> {
            final IntPairMap map = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .build();
            final IntPairMap mapped = map.mapToInt(f);

            final int size = map.size();
            assertEquals(size, mapped.size());

            for (int i = 0; i < size; i++) {
                assertEquals(map.keyAt(i), mapped.keyAt(i));
            }
        })));
    }

    @Test
    default void testSliceWhenEmpty() {
        final IntPairMap map = newBuilder().build();
        assertTrue(map.slice(new ImmutableIntRange(0, 0)).isEmpty());
        assertTrue(map.slice(new ImmutableIntRange(1, 1)).isEmpty());
        assertTrue(map.slice(new ImmutableIntRange(2, 2)).isEmpty());
        assertTrue(map.slice(new ImmutableIntRange(0, 1)).isEmpty());
        assertTrue(map.slice(new ImmutableIntRange(1, 2)).isEmpty());
        assertTrue(map.slice(new ImmutableIntRange(0, 2)).isEmpty());
    }

    @Test
    default void testSlice() {
        withInt(a -> withInt(b -> withInt(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);
            final IntPairMap map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final int firstKey = map.keyAt(0);
            final int secondKey = (size >= 2)? map.keyAt(1) : 0;
            final int thirdKey = (size >= 3)? map.keyAt(2) : 0;
            final int firstValue = map.valueAt(0);
            final int secondValue = (size >= 2)? map.valueAt(1) : 0;
            final int thirdValue = (size >= 3)? map.valueAt(2) : 0;

            final IntPairMap sliceA = map.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertEquals(firstKey, sliceA.keyAt(0));
            assertEquals(firstValue, sliceA.valueAt(0));

            final IntPairMap sliceB = map.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertEquals(secondKey, sliceB.keyAt(0));
                assertEquals(secondValue, sliceB.valueAt(0));
            }
            else {
                assertTrue(sliceB.isEmpty());
            }

            final IntPairMap sliceC = map.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertEquals(thirdKey, sliceC.keyAt(0));
                assertEquals(thirdValue, sliceC.valueAt(0));
            }
            else {
                assertTrue(sliceC.isEmpty());
            }

            final IntPairMap sliceAB = map.slice(new ImmutableIntRange(0, 1));
            if (size >= 2) {
                assertEquals(2, sliceAB.size());
                assertEquals(secondKey, sliceAB.keyAt(1));
                assertEquals(secondValue, sliceAB.valueAt(1));
            }
            else {
                assertEquals(1, sliceAB.size());
            }
            assertEquals(firstKey, sliceAB.keyAt(0));
            assertEquals(firstValue, sliceAB.valueAt(0));

            final IntPairMap sliceBC = map.slice(new ImmutableIntRange(1, 2));
            if (size == 1) {
                assertTrue(sliceBC.isEmpty());
            }
            else if (size == 2) {
                assertEquals(1, sliceBC.size());
                assertEquals(secondKey, sliceBC.keyAt(0));
                assertEquals(secondValue, sliceBC.valueAt(0));
            }
            else {
                assertEquals(2, sliceBC.size());
                assertEquals(secondKey, sliceBC.keyAt(0));
                assertEquals(secondValue, sliceBC.valueAt(0));
                assertEquals(thirdKey, sliceBC.keyAt(1));
                assertEquals(thirdValue, sliceBC.valueAt(1));
            }

            final IntPairMap sliceABC = map.slice(new ImmutableIntRange(0, 2));
            assertEquals(size, sliceABC.size());
            assertEquals(firstKey, sliceABC.keyAt(0));
            assertEquals(firstValue, sliceABC.valueAt(0));
            if (size >= 2) {
                assertEquals(secondKey, sliceABC.keyAt(1));
                assertEquals(secondValue, sliceABC.valueAt(1));
                if (size == 3) {
                    assertEquals(thirdKey, sliceABC.keyAt(2));
                    assertEquals(thirdValue, sliceABC.valueAt(2));
                }
            }

            final IntPairMap sliceABCD = map.slice(new ImmutableIntRange(0, 3));
            assertEquals(size, sliceABCD.size());
            assertEquals(firstKey, sliceABCD.keyAt(0));
            assertEquals(firstValue, sliceABCD.valueAt(0));
            if (size >= 2) {
                assertEquals(secondKey, sliceABCD.keyAt(1));
                assertEquals(secondValue, sliceABCD.valueAt(1));
                if (size == 3) {
                    assertEquals(thirdKey, sliceABCD.keyAt(2));
                    assertEquals(thirdValue, sliceABCD.valueAt(2));
                }
            }
        })));
    }

    @Test
    @Override
    default void testSkipWhenEmpty() {
        final IntPairMap map = newBuilder().build();
        assertSame(map, map.skip(0));
        assertTrue(map.skip(1).isEmpty());
        assertTrue(map.skip(20).isEmpty());
    }

    @Test
    @Override
    default void testSkip() {
        withInt(a -> withInt(b -> withInt(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);

            final IntPairMap map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            final int size = map.size();
            final int secondKey = (size >= 2)? map.keyAt(1) : 0;
            final int secondValue = (size >= 2)? map.valueAt(1) : 0;
            final int thirdKey = (size == 3)? map.keyAt(2) : 0;
            final int thirdValue = (size == 3)? map.valueAt(2) : 0;

            assertSame(map, map.skip(0));

            final IntPairMap skip1 = map.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertEquals(secondKey, skip1.keyAt(0));
                assertEquals(secondValue, skip1.valueAt(0));
                if (size == 3) {
                    assertEquals(thirdKey, skip1.keyAt(1));
                    assertEquals(thirdValue, skip1.valueAt(1));
                }
            }

            final IntPairMap skip2 = map.skip(2);
            if (size == 3) {
                assertEquals(thirdKey, skip2.keyAt(0));
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
        final IntPairMap map = newBuilder().build();
        assertTrue(map.take(0).isEmpty());
        assertTrue(map.take(1).isEmpty());
        assertTrue(map.take(2).isEmpty());
        assertTrue(map.take(24).isEmpty());
    }

    @Test
    default void testTake() {
        withInt(a -> withInt(b -> withInt(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);
            final IntPairMap map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final int firstKey = map.keyAt(0);
            final int firstValue = map.valueAt(0);

            assertTrue(map.take(0).isEmpty());

            final IntPairMap take1 = map.take(1);
            assertEquals(1, take1.size());
            assertEquals(firstKey, take1.keyAt(0));
            assertEquals(firstValue, take1.valueAt(0));

            final IntPairMap take2 = map.take(2);
            assertEquals(firstKey, take2.keyAt(0));
            assertEquals(firstValue, take2.valueAt(0));
            if (size == 1) {
                assertEquals(1, take2.size());
            }
            else {
                assertEquals(2, take2.size());
                assertEquals(map.keyAt(1), take2.keyAt(1));
                assertEquals(map.valueAt(1), take2.valueAt(1));
            }

            final IntPairMap take3 = map.take(3);
            assertEquals(size, take3.size());
            assertEquals(firstKey, take3.keyAt(0));
            assertEquals(firstValue, take3.valueAt(0));
            if (size > 1) {
                assertEquals(map.keyAt(1), take3.keyAt(1));
                assertEquals(map.valueAt(1), take3.valueAt(1));
                if (size == 3) {
                    assertEquals(map.keyAt(2), take3.keyAt(2));
                    assertEquals(map.valueAt(2), take3.valueAt(2));
                }
            }

            final IntPairMap take4 = map.take(4);
            assertEquals(size, take4.size());
            assertEquals(firstKey, take4.keyAt(0));
            assertEquals(firstValue, take4.valueAt(0));
            if (size > 1) {
                assertEquals(map.keyAt(1), take4.keyAt(1));
                assertEquals(map.valueAt(1), take4.valueAt(1));
                if (size == 3) {
                    assertEquals(map.keyAt(2), take4.keyAt(2));
                    assertEquals(map.valueAt(2), take4.valueAt(2));
                }
            }
        })));
    }
}

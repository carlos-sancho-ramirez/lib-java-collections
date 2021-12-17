package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withInt;

interface IntPairMapTest<B extends IntTransformableBuilder> extends IntTransformableTest<B> {

    IntPairMapBuilder newBuilder();
    void withMapBuilderSupplier(Procedure<IntPairMapBuilderSupplier<IntPairMapBuilder>> procedure);

    default void withArbitraryMapBuilderSupplier(Procedure<IntPairMapBuilderSupplier<IntPairMapBuilder>> procedure) {
        procedure.apply(ImmutableIntPairMap.Builder::new);
        procedure.apply(MutableIntPairMap.Builder::new);
    }

    @Override
    default void withValue(IntProcedure procedure) {
        withInt(procedure);
    }

    @Override
    default void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    @Override
    default void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v * v);
        procedure.apply(v -> v + 1);
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
                assertEquals(map, filtered);
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
                assertEquals(map, filtered);
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
                assertEquals(map, filtered);
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
                assertEquals(map, filtered);
            }
        })));
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
}

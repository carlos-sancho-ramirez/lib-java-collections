package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface ImmutableMapTest<K, V, B extends ImmutableTransformableBuilder<V>, MB extends ImmutableMap.Builder<K, V>> extends MapTest<K, V, B, MB>, ImmutableTransformableTest<V, B> {

    MB newBuilder();
    void withKey(Procedure<K> procedure);
    V valueFromKey(K key);

    @Test
    @Override
    default void testFilterByKeyWhenEmpty() {
        final Predicate<K> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            final ImmutableMap<K, V> empty = supplier.newBuilder().build();
            final ImmutableMap<K, V> filtered = empty.filterByKey(f);
            assertTrue(filtered.isEmpty());
        });
    }

    @Test
    @Override
    default void testFilterByKeyForSingleElement() {
        withFilterByKeyFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final ImmutableMap<K, V> map = supplier.newBuilder().put(key, valueFromKey(key)).build();
            final ImmutableMap<K, V> filtered = map.filterByKey(f);

            if (f.apply(key)) {
                assertTrue(map.equalMap(filtered));
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        })));
    }

    @Test
    @Override
    default void testFilterByKeyForMultipleElements() {
        withFilterByKeyFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableMap<K, V> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableMap<K, V> filtered = map.filterByKey(f);

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
    @Override
    default void testFilterByKeyNotWhenEmpty() {
        final Predicate<K> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            final ImmutableMap<K, V> empty = supplier.newBuilder().build();
            final ImmutableMap<K, V> filtered = empty.filterByKeyNot(f);
            assertTrue(filtered.isEmpty());
        });
    }

    @Test
    @Override
    default void testFilterByKeyNotForSingleElement() {
        withFilterByKeyFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final ImmutableMap<K, V> map = supplier.newBuilder().put(key, valueFromKey(key)).build();
            final ImmutableMap<K, V> filtered = map.filterByKeyNot(f);

            if (!f.apply(key)) {
                assertTrue(map.equalMap(filtered));
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        })));
    }

    @Test
    @Override
    default void testFilterByKeyNotForMultipleElements() {
        withFilterByKeyFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableMap<K, V> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableMap<K, V> filtered = map.filterByKeyNot(f);

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
    default void testPutAllMethodForMultipleElementsInThisMap() {
        withKey(a -> withKey(b -> {
            final ImmutableMap<K, V> thisMap = newBuilder().build();
            final ImmutableMap<K, V> thatMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            assertEquals(thatMap, thisMap.putAll(thatMap));
        }));
    }

    @Test
    default void testPutAllMethodForEmptyGivenMap() {
        withKey(a -> withKey(b -> {
            final ImmutableMap<K, V> thisMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            assertSame(thisMap, thisMap.putAll(newBuilder().build()));
        }));
    }

    @Test
    default void testPutAllMethodForMultipleElementsInTheGivenMap() {
        withKey(a -> withKey(b -> withKey(c -> withKey(d -> {
            final ImmutableMap<K, V> thisMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final ImmutableMap<K, V> thatMap = newBuilder()
                    .put(c, valueFromKey(c))
                    .put(d, valueFromKey(d))
                    .build();

            final ImmutableMap.Builder<K, V> builder = newBuilder();
            for (Map.Entry<K, V> entry : thisMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            for (Map.Entry<K, V> entry : thatMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            assertEquals(builder.build(), thisMap.putAll(thatMap));
        }))));
    }

    @Test
    @Override
    default void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final Map.Entry<K, V> entry = new Map.Entry<>(0, key, valueFromKey(key));
            final ImmutableMap<K, V> map = supplier.newBuilder().put(key, entry.value()).build();
            final ImmutableMap<K, V> filtered = map.filterByEntry(f);

            if (f.apply(entry)) {
                assertTrue(map.equalMap(filtered));
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        })));
    }

    @Test
    @Override
    default void testFilterByEntryForMultipleElements() {
        withFilterByEntryFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableMap<K, V> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableMap<K, V> filtered = map.filterByEntry(f);
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
            final ImmutableMap<K, V> map = newBuilder()
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

            final ImmutableMap<K, V> sliceA = map.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(firstKey, sliceA.keyAt(0));
            assertSame(firstValue, sliceA.valueAt(0));

            final ImmutableMap<K, V> sliceB = map.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(secondKey, sliceB.keyAt(0));
                assertSame(secondValue, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final ImmutableMap<K, V> sliceC = map.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(thirdKey, sliceC.keyAt(0));
                assertSame(thirdValue, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final ImmutableMap<K, V> sliceAB = map.slice(new ImmutableIntRange(0, 1));
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

            final ImmutableMap<K, V> sliceBC = map.slice(new ImmutableIntRange(1, 2));
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

            final ImmutableMap<K, V> sliceABC = map.slice(new ImmutableIntRange(0, 2));
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

            final ImmutableMap<K, V> sliceABCD = map.slice(new ImmutableIntRange(0, 3));
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
    default void testSkipWhenEmpty() {
        final ImmutableMap<K, V> map = newBuilder().build();
        assertSame(map, map.skip(0));
        assertSame(map, map.skip(1));
        assertSame(map, map.skip(20));
    }

    @Test
    default void testSkip() {
        withKey(a -> withKey(b -> withKey(c -> {
            final V aValue = valueFromKey(a);
            final V bValue = valueFromKey(b);
            final V cValue = valueFromKey(c);

            final ImmutableMap<K, V> map = newBuilder()
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

            final ImmutableMap<K, V> skip1 = map.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertSame(secondKey, skip1.keyAt(0));
                assertSame(secondValue, skip1.valueAt(0));
                if (size == 3) {
                    assertSame(thirdKey, skip1.keyAt(1));
                    assertSame(thirdValue, skip1.valueAt(1));
                }
            }

            final ImmutableMap<K, V> skip2 = map.skip(2);
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
    default void testTakeWhenEmpty() {
        final ImmutableMap<K, V> map = newBuilder().build();
        assertSame(map, map.take(0));
        assertSame(map, map.take(1));
        assertSame(map, map.take(2));
        assertSame(map, map.take(24));
    }

    @Test
    default void testTake() {
        withKey(a -> withKey(b -> withKey(c -> {
            final V aValue = valueFromKey(a);
            final V bValue = valueFromKey(b);
            final V cValue = valueFromKey(c);
            final ImmutableMap<K, V> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final K firstKey = map.keyAt(0);
            final V firstValue = map.valueAt(0);

            assertTrue(map.take(0).isEmpty());

            final ImmutableMap<K, V> take1 = map.take(1);
            if (size > 1) {
                assertEquals(1, take1.size());
                assertSame(firstKey, take1.keyAt(0));
                assertSame(firstValue, take1.valueAt(0));
            }
            else {
                assertSame(map, take1);
            }

            final ImmutableMap<K, V> take2 = map.take(2);
            if (size > 2) {
                assertEquals(2, take2.size());
                assertSame(firstKey, take2.keyAt(0));
                assertSame(firstValue, take2.valueAt(0));
                assertSame(map.keyAt(1), take2.keyAt(1));
                assertSame(map.valueAt(1), take2.valueAt(1));
            }
            else {
                assertSame(map, take2);
            }

            assertSame(map, map.take(3));
            assertSame(map, map.take(4));
            assertSame(map, map.take(24));
        })));
    }

    @Test
    default void testSkipLastWhenEmpty() {
        final ImmutableMap<K, V> map = newBuilder().build();
        assertSame(map, map.skipLast(0));
        assertSame(map, map.skipLast(1));
        assertSame(map, map.skipLast(2));
        assertSame(map, map.skipLast(24));
    }

    @Test
    default void testSkipLast() {
        withKey(a -> withKey(b -> withKey(c -> {
            final V aValue = valueFromKey(a);
            final V bValue = valueFromKey(b);
            final V cValue = valueFromKey(c);
            final ImmutableMap<K, V> map = newBuilder()
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

            final ImmutableMap<K, V> map1 = map.skipLast(1);
            assertEquals(size - 1, map1.size());
            if (size >= 2) {
                assertSame(firstKey, map1.keyAt(0));
                assertSame(firstValue, map1.valueAt(0));
                if (size == 3) {
                    assertSame(secondKey, map1.keyAt(1));
                    assertSame(secondValue, map1.valueAt(1));
                }
            }

            final ImmutableMap<K, V> map2 = map.skipLast(2);
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
}

package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;

interface ImmutableIntValueMapTest<T, B extends ImmutableIntTransformableBuilder, MB extends ImmutableIntValueMap.Builder<T>> extends IntValueMapTest<T, B, MB>, ImmutableIntTransformableTest<B> {

    default void withInvertibleArray(Procedure<ImmutableIntValueMap<T>> action) {
        withKey(key1 -> withKey(key2 -> withKey(key3 -> {
            if (!equal(key1, key2) && !equal(key1, key3) && !equal(key2, key3)) {
                withInt(value1 -> withInt(value2 -> withInt(value3 -> {
                    if (value1 != value2 && value1 != value3 && value2 != value3) {
                        final ImmutableIntValueMap<T> invertibleArray = new ImmutableIntValueHashMap.Builder<T>()
                                .put(key1, value1)
                                .put(key2, value2)
                                .put(key3, value3)
                                .build();

                        action.apply(invertibleArray);
                    }
                })));
            }
        })));
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
    default void testInverted() {
        withInvertibleArray(invertedArray -> {
            final ImmutableIntKeyMap<T> array = invertedArray.invert();
            assertEquals(invertedArray.size(), array.size());

            for (ImmutableIntValueMap.Entry<T> entry : invertedArray.entries()) {
                assertEquals(entry.key(), array.get(entry.value()));
            }
        });
    }

    @Test
    default void testKeySet() {
        withInvertibleArray(array -> {
            final ImmutableSet<T> result = array.keySet();

            final ImmutableHashSet.Builder<T> builder = new ImmutableHashSet.Builder<>();
            for (ImmutableIntValueMap.Entry<T> entry : array.entries()) {
                builder.add(entry.key());
            }

            assertEquals(builder.build(), result);
        });
    }

    default boolean valueIsEven(int value) {
        return (value & 1) == 0;
    }

    default void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::valueIsEven);
    }

    @Override
    default void withFilterByKeyFunc(Procedure<Predicate<T>> procedure) {
        procedure.apply(v -> v == null || (v.hashCode() & 1) == 0);
    }

    @Test
    @Override
    default void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableIntValueMap<T> map = newBuilder().build();
            assertSame(map, map.filter(f));
        });
    }

    default void assertEmpty(ImmutableIntValueMap<T> map) {
        assertTrue(map.isEmpty());
    }

    @Test
    @Override
    default void testFilterForSingleElement() {
        withFilterFunc(f -> withInt(value -> {
            final T key = keyFromInt(value);
            final ImmutableIntValueMap<T> map = newBuilder().put(key, value).build();
            final ImmutableIntValueMap<T> filtered = map.filter(f);

            if (f.apply(value)) {
                assertSame(map, filtered);
            }
            else {
                assertTrue(filtered.isEmpty());
            }
        }));
    }

    @Test
    @Override
    default void testFilterForMultipleElements() {
        withFilterFunc(f -> withInt(valueA -> withInt(valueB -> {
            final T keyA = keyFromInt(valueA);
            final T keyB = keyFromInt(valueB);
            final ImmutableIntValueMap<T> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableIntValueMap<T> filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertSame(map, filtered);
            }
            else if (aPassed) {
                Iterator<IntValueMap.Entry<T>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntValueMap.Entry<T> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertEquals(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntValueMap.Entry<T>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntValueMap.Entry<T> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertEquals(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertEmpty(filtered);
            }
        })));
    }

    @Test
    @Override
    default void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableIntValueMap<T> map = newBuilder().build();
            assertSame(map, map.filterNot(f));
        });
    }

    @Test
    @Override
    default void testFilterNotForSingleElement() {
        withFilterFunc(f -> withInt(value -> {
            final T key = keyFromInt(value);
            final ImmutableIntValueMap<T> map = newBuilder().put(key, value).build();
            final ImmutableIntValueMap<T> filtered = map.filterNot(f);

            if (f.apply(value)) {
                assertEmpty(filtered);
            }
            else {
                assertSame(map, filtered);
            }
        }));
    }

    @Test
    @Override
    default void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withInt(valueA -> withInt(valueB -> {
            final T keyA = keyFromInt(valueA);
            final T keyB = keyFromInt(valueB);
            final ImmutableIntValueMap<T> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableIntValueMap<T> filtered = map.filterNot(f);

            final boolean aRemoved = f.apply(valueA);
            final boolean bRemoved = f.apply(valueB);

            if (aRemoved && bRemoved) {
                assertEmpty(filtered);
            }
            else if (aRemoved) {
                Iterator<IntValueMap.Entry<T>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntValueMap.Entry<T> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertEquals(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<IntValueMap.Entry<T>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntValueMap.Entry<T> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertEquals(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(map, filtered);
            }
        })));
    }

    @Test
    @Override
    default void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final IntValueMap.Entry<T> entry = new IntValueMap.Entry<>(0, key, valueFromKey(key));
            final ImmutableIntValueMap<T> map = supplier.newBuilder().put(key, entry.value()).build();
            final ImmutableIntValueMap<T> filtered = map.filterByEntry(f);

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
            final ImmutableIntValueMap<T> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableIntValueMap<T> filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            int counter = 0;
            for (IntValueMap.Entry<T> entry : map.entries()) {
                if (f.apply(entry)) {
                    assertEquals(entry.value(), filtered.get(entry.key()));
                    counter++;
                }
            }
            assertEquals(filteredSize, counter);
        }))));
    }

    @Test
    default void testMapToIntMethod() {
        withInt(a -> withInt(b -> {
            final ImmutableIntValueMap<T> map = newBuilder()
                    .put(keyFromInt(a), a)
                    .put(keyFromInt(b), b)
                    .build();

            final IntToIntFunction mapFunc = value -> value + 3;
            final ImmutableIntValueMap<T> map2 = map.mapToInt(mapFunc);

            final ImmutableSet<T> keySet = map.keySet();
            assertEquals(keySet, map2.keySet());

            for (T key : keySet) {
                assertEquals(mapFunc.apply(map.get(key)), map2.get(key));
            }
        }));
    }

    @Test
    default void testMapValues() {
        withInt(a -> withInt(b -> {
            final ImmutableIntValueMap<T> map = newBuilder()
                    .put(keyFromInt(a), a)
                    .put(keyFromInt(b), b)
                    .build();

            final IntFunction<T> mapFunc = this::keyFromInt;
            final ImmutableMap<T, T> map2 = map.map(mapFunc);

            final ImmutableSet<T> keySet = map.keySet();
            assertEquals(keySet, map2.keySet());

            for (T key : keySet) {
                assertEquals(mapFunc.apply(map.get(key)), map2.get(key));
            }
        }));
    }

    @Test
    default void testPutMethod() {
        withKey(a -> withKey(b -> withKey(key -> withValue(value -> {
            final ImmutableIntValueMap<T> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final boolean contained = map.containsKey(key);
            final ImmutableIntValueMap<T> newMap = map.put(key, value);

            if (!contained) {
                final ImmutableIntValueMap.Builder<T> builder = newBuilder();
                for (IntValueMap.Entry<T> entry : map.entries()) {
                    builder.put(entry.key(), entry.value());
                }
                assertTrue(builder.put(key, value).build().equalMap(newMap));
            }
            else {
                assertSame(map, map.put(key, valueFromKey(key)));

                final ImmutableSet<T> keySet = map.keySet();
                assertEquals(keySet, newMap.keySet());

                for (T k : keySet) {
                    if (equal(k, key)) {
                        assertEquals(value, newMap.get(k));
                    }
                    else {
                        assertEquals(map.get(k), newMap.get(k));
                    }
                }
            }
        }))));
    }

    @Test
    default void testPutAllMethodForMultipleElementsInThisMap() {
        withKey(a -> withKey(b -> {
            final ImmutableIntValueMap<T> thisMap = newBuilder().build();
            final ImmutableIntValueMap<T> thatMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            assertTrue(thatMap.equalMap(thisMap.putAll(thatMap)));
        }));
    }

    @Test
    default void testPutAllMethodForEmptyGivenMap() {
        withKey(a -> withKey(b -> {
            final ImmutableIntValueMap<T> thisMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            assertSame(thisMap, thisMap.putAll(newBuilder().build()));
        }));
    }

    @Test
    default void testPutAllMethodForMultipleElementsInTheGivenMap() {
        withKey(a -> withKey(b -> withKey(c -> withKey(d -> {
            final ImmutableIntValueMap<T> thisMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final ImmutableIntValueMap<T> thatMap = newBuilder()
                    .put(c, valueFromKey(c))
                    .put(d, valueFromKey(d))
                    .build();

            final ImmutableIntValueMap.Builder<T> builder = newBuilder();
            for (IntValueMap.Entry<T> entry : thisMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            for (IntValueMap.Entry<T> entry : thatMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            assertTrue(builder.build().equalMap(thisMap.putAll(thatMap)));
        }))));
    }

    @Test
    default void testSliceWhenEmpty() {
        final ImmutableIntValueMap<T> map = newBuilder().build();
        assertSame(map, map.slice(new ImmutableIntRange(0, 0)));
        assertSame(map, map.slice(new ImmutableIntRange(1, 1)));
        assertSame(map, map.slice(new ImmutableIntRange(2, 2)));
        assertSame(map, map.slice(new ImmutableIntRange(0, 1)));
        assertSame(map, map.slice(new ImmutableIntRange(1, 2)));
        assertSame(map, map.slice(new ImmutableIntRange(0, 2)));
    }

    @Test
    default void testSlice() {
        withKey(a -> withKey(b -> withKey(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);
            final ImmutableIntValueMap<T> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final T firstKey = map.keyAt(0);
            final T secondKey = (size >= 2)? map.keyAt(1) : null;
            final T thirdKey = (size >= 3)? map.keyAt(2) : null;
            final int firstValue = map.valueAt(0);
            final int secondValue = (size >= 2)? map.valueAt(1) : 0;
            final int thirdValue = (size >= 3)? map.valueAt(2) : 0;

            final ImmutableIntValueMap<T> sliceA = map.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(firstKey, sliceA.keyAt(0));
            assertEquals(firstValue, sliceA.valueAt(0));

            final ImmutableIntValueMap<T> sliceB = map.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(secondKey, sliceB.keyAt(0));
                assertEquals(secondValue, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final ImmutableIntValueMap<T> sliceC = map.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(thirdKey, sliceC.keyAt(0));
                assertEquals(thirdValue, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final ImmutableIntValueMap<T> sliceAB = map.slice(new ImmutableIntRange(0, 1));
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

            final ImmutableIntValueMap<T> sliceBC = map.slice(new ImmutableIntRange(1, 2));
            if (size == 1) {
                assertEquals(0, sliceBC.size());
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

            assertSame(map, map.slice(new ImmutableIntRange(0, 2)));
            assertSame(map, map.slice(new ImmutableIntRange(0, 3)));
        })));
    }
}

package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class ImmutableIntKeyMapTest implements IntKeyMapTest<String, ImmutableTransformableBuilder<String>, ImmutableIntKeyMap.Builder<String>>, ImmutableTransformableTest<String, ImmutableTransformableBuilder<String>> {

    @Override
    public ImmutableIntKeyMap.Builder<String> newMapBuilder() {
        return new ImmutableIntKeyMap.Builder<>();
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableTransformableBuilder<String>>> procedure) {
        procedure.apply(HashCodeKeyTraversableBuilder::new);
    }

    @Override
    public void withValue(Procedure<String> procedure) {
        withString(procedure);
    }

    private boolean stringIsEmpty(String value) {
        return value != null && !value.isEmpty();
    }

    private boolean hashCodeIsEven(String value) {
        return value == null || (value.hashCode() & 1) == 0;
    }

    @Override
    public void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::stringIsEmpty);
        procedure.apply(this::hashCodeIsEven);
    }

    private String reduceFunc(String left, String right) {
        return String.valueOf(left) + '-' + String.valueOf(right);
    }

    @Override
    public void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
        procedure.apply(this::reduceFunc);
    }

    private String prefixUnderscore(String value) {
        return "_" + value;
    }

    private String charCounter(String value) {
        final int length = (value != null)? value.length() : 0;
        return Integer.toString(length);
    }

    @Override
    public void withMapFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(this::prefixUnderscore);
        procedure.apply(this::charCounter);
    }

    @Override
    public void withMapToIntFunc(Procedure<IntResultFunction<String>> procedure) {
        procedure.apply(SortUtils::hashCode);
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntKeyMapBuilderSupplier<String, ImmutableIntKeyMap.Builder<String>>> procedure) {
        procedure.apply(ImmutableIntKeyMap.Builder::new);
    }

    @Override
    public String getTestValue() {
        return "value";
    }

    @Override
    public String getTestValue2() {
        return "value2";
    }

    @Override
    public String valueFromKey(int key) {
        return Integer.toString(key);
    }

    @SuppressWarnings("unchecked")
    private static final ImmutableIntKeyMap<String>[] IMMUTABLE_INT_KEY_MAP_VALUES = new ImmutableIntKeyMap[] {
            null,
            new ImmutableIntKeyMap.Builder<String>().build(),
            new ImmutableIntKeyMap.Builder<String>().put(0, "").build(),
            new ImmutableIntKeyMap.Builder<String>().put(124, "big number").build(),
            new ImmutableIntKeyMap.Builder<String>().put(-3, "_3").build(),
            new ImmutableIntKeyMap.Builder<String>().put(0, null).put(12234, "large").build(),
            new ImmutableIntKeyMap.Builder<String>().put(-34, "x").put(2, "x").put(Integer.MAX_VALUE, "xy").build()
    };

    private void withImmutableIntKeyMap(Procedure<ImmutableIntKeyMap<String>> action) {
        final int length = IMMUTABLE_INT_KEY_MAP_VALUES.length;
        for (int i = 0; i < length; i++) {
            action.apply(IMMUTABLE_INT_KEY_MAP_VALUES[i]);
        }
    }

    @Test
    public void testFilterForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final String value = Integer.toString(key);
            final ImmutableIntKeyMap<String> map = newMapBuilder().put(key, value).build();
            final ImmutableIntKeyMap<String> filtered = map.filter(f);

            final ImmutableIntKeyMap<String> expected = f.apply(value)? map : newMapBuilder().build();
            assertSame(expected, filtered);
        }));
    }

    @Test
    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withInt(keyA -> withInt(keyB -> {
            final String valueA = Integer.toString(keyA);
            final String valueB = Integer.toString(keyB);
            final ImmutableIntKeyMap<String> map = newMapBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableIntKeyMap<String> filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertSame(map, filtered);
            }
            else if (aPassed) {
                Iterator<IntKeyMap.Entry<String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntKeyMap.Entry<String> entry = iterator.next();
                assertEquals(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntKeyMap.Entry<String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntKeyMap.Entry<String> entry = iterator.next();
                assertEquals(keyB, entry.key());
                assertSame(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(newMapBuilder().build(), filtered);
            }
        })));
    }

    @Test
    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final String value = Integer.toString(key);
            final ImmutableIntKeyMap<String> map = newMapBuilder().put(key, value).build();
            final ImmutableIntKeyMap<String> filtered = map.filterNot(f);

            if (!f.apply(value)) {
                assertSame(map, filtered);
            }
            else {
                assertSame(newMapBuilder().build(), filtered);
            }
        }));
    }

    @Test
    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withInt(keyA -> withInt(keyB -> {
            final String valueA = Integer.toString(keyA);
            final String valueB = Integer.toString(keyB);
            final ImmutableIntKeyMap<String> map = newMapBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableIntKeyMap<String> filtered = map.filterNot(f);

            final boolean aRemoved = f.apply(valueA);
            final boolean bRemoved = f.apply(valueB);

            if (aRemoved && bRemoved) {
                assertSame(newMapBuilder().build(), filtered);
            }
            else if (aRemoved) {
                Iterator<IntKeyMap.Entry<String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntKeyMap.Entry<String> entry = iterator.next();
                assertEquals(keyB, entry.key());
                assertSame(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<IntKeyMap.Entry<String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntKeyMap.Entry<String> entry = iterator.next();
                assertEquals(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(map, filtered);
            }
        })));
    }

    @Test
    void testFilterByKeyReturnTheSameInstanceAndTypeWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        final ImmutableIntKeyMap<String> map = newMapBuilder().build();
        final ImmutableIntKeyMap<String> filtered = map.filterByKey(f);
        assertSame(map, filtered);
    }

    @Test
    void testFilterByKeyReturnTheSameInstanceAndType() {
        final IntPredicate f = unused -> true;
        withInt(a -> withInt(b -> {
            final ImmutableIntKeyMap<String> map = newMapBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableIntKeyMap<String> filtered = map.filterByKey(f);
            assertSame(map, filtered);
        }));
    }

    @Test
    @Override
    public void testFilterByEntryWhenEmpty() {
        final Predicate<IntKeyMapEntry<String>> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            final ImmutableIntKeyMap<String> empty = supplier.newBuilder().build();
            final ImmutableIntKeyMap<String> filtered = empty.filterByEntry(f);
            assertSame(empty, filtered);
            assertTrue(filtered.isEmpty());
        });
    }

    @Test
    @Override
    public void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withInt(key -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap.Entry<String> entry = new IntKeyMap.Entry<>(0, key, valueFromKey(key));
            final ImmutableIntKeyMap<String> map = supplier.newBuilder().put(key, entry.value()).build();
            final ImmutableIntKeyMap<String> expected = f.apply(entry)? map : supplier.newBuilder().build();
            assertSame(expected, map.filterByEntry(f));
        })));
    }

    @Test
    @Override
    public void testFilterByEntryForMultipleElements() {
        withFilterByEntryFunc(f -> withInt(a -> withInt(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableIntKeyMap<String> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableIntKeyMap<String> filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            if (filteredSize == 0) {
                assertSame(supplier.newBuilder().build(), filtered);
            }
            else if (filteredSize == map.size()) {
                assertSame(map, filtered);
            }
            else {
                int counter = 0;
                for (IntKeyMap.Entry<String> entry : map.entries()) {
                    if (f.apply(entry)) {
                        assertSame(entry.value(), filtered.get(entry.key()));
                        counter++;
                    }
                }
                assertEquals(filteredSize, counter);
            }
        }))));
    }

    @Test
    void testPutMethod() {
        withImmutableIntKeyMap(array -> withInt(key -> withString(value -> {
            if (array != null) {
                boolean contained = false;
                for (int i = 0; i < array.size(); i++) {
                    if (array.keyAt(i) == key) {
                        contained = true;
                        break;
                    }
                }

                final ImmutableIntKeyMap<String> newArray = array.put(key, value);

                if (!contained) {
                    final ImmutableIntKeyMap.Builder<String> builder = new ImmutableIntKeyMap.Builder<>();
                    for (IntKeyMap.Entry<String> entry : array.entries()) {
                        builder.put(entry.key(), entry.value());
                    }
                    assertEquals(builder.put(key, value).build(), newArray);
                }
            }
        })));
    }

    @Test
    void testPutAllMethodForMultipleElementsInThisMap() {
        withInt(a -> withInt(b -> {
            final ImmutableIntKeyMap<String> thisMap = newMapBuilder().build();
            final ImmutableIntKeyMap<String> thatMap = newMapBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            assertEquals(thatMap, thisMap.putAll(thatMap));
        }));
    }

    @Test
    void testPutAllMethodForEmptyGivenMap() {
        withInt(a -> withInt(b -> {
            final ImmutableIntKeyMap<String> thisMap = newMapBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            assertSame(thisMap, thisMap.putAll(newMapBuilder().build()));
        }));
    }

    @Test
    void testPutAllMethodForMultipleElementsInTheGivenMap() {
        withInt(a -> withInt(b -> withInt(c -> withInt(d -> {
            final ImmutableIntKeyMap<String> thisMap = newMapBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final ImmutableIntKeyMap<String> thatMap = newMapBuilder()
                    .put(c, valueFromKey(c))
                    .put(d, valueFromKey(d))
                    .build();

            final ImmutableIntKeyMap.Builder<String> builder = newMapBuilder();
            for (IntKeyMap.Entry<String> entry : thisMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            for (IntKeyMap.Entry<String> entry : thatMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            assertEquals(builder.build(), thisMap.putAll(thatMap));
        }))));
    }

    @Test
    void testInvertMethod() {
        withImmutableIntKeyMap(array -> {
            if (array != null) {
                // Check if the array is invertible, so no duplicated values should be found
                final int length = array.size();
                boolean duplicated = false;
                for (int i = 0; i < length - 1; i++) {
                    for (int j = i + 1; j < length; j++) {
                        if (equal(array.valueAt(i), array.valueAt(j))) {
                            duplicated = true;
                        }
                        break;
                    }

                    if (duplicated) {
                        break;
                    }
                }

                if (!duplicated) {
                    final ImmutableIntValueMap<String> inverted = array.invert();
                    assertEquals(length, inverted.size());

                    for (int i = 0; i < length; i++) {
                        assertEquals(inverted.keyAt(i), array.get(inverted.valueAt(i)));
                    }
                }
            }
        });
    }

    @Test
    public void testKeySetWhenEmpty() {
        final ImmutableIntKeyMap<String> empty = ImmutableIntKeyMap.empty();
        assertSame(ImmutableIntArraySet.empty(), empty.keySet());
    }

    @Test
    void testToImmutableForEmpty() {
        final ImmutableIntKeyMap.Builder<String> builder = newMapBuilder();
        final ImmutableIntKeyMap<String> map = builder.build();
        assertSame(map, map.toImmutable());
    }

    @Test
    void testMutateForEmpty() {
        final ImmutableIntKeyMap.Builder<String> builder = newMapBuilder();
        final ImmutableIntKeyMap<String> map1 = builder.build();
        final MutableIntKeyMap<String> map2 = map1.mutate();

        assertTrue(map2.isEmpty());

        map2.put(1, "");
        assertNull(map1.get(1, null));
    }

    @Test
    void testToImmutable() {
        withInt(a -> withInt(b -> {
            final ImmutableIntKeyMap.Builder<String> builder = newMapBuilder();
            final ImmutableIntKeyMap<String> map1 = builder.put(a, "").put(b, "").build();
            final ImmutableIntKeyMap<String> map2 = map1.toImmutable();
            assertSame(map1, map2);
        }));
    }

    @Test
    void testMutate() {
        withInt(a -> withInt(b -> {
            final ImmutableIntKeyMap.Builder<String> builder = newMapBuilder();
            final ImmutableIntKeyMap<String> map1 = builder.put(a, "").put(b, "").build();
            final MutableIntKeyMap<String> map2 = map1.mutate();

            final Iterator<IntKeyMap.Entry<String>> it1 = map1.entries().iterator();
            final Iterator<IntKeyMap.Entry<String>> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                final IntKeyMap.Entry<String> item1 = it1.next();
                final IntKeyMap.Entry<String> item2 = it2.next();
                assertEquals(item1.key(), item2.key());
                assertEquals(item1.value(), item2.value());
            }
            assertFalse(it2.hasNext());

            map2.remove(b);
            assertEquals("", map1.get(b, null));
            assertNull(map2.get(b, null));
        }));
    }

    @Test
    @Override
    public void testSlice() {
        withInt(a -> withInt(b -> withInt(c -> {
            final String aValue = valueFromKey(a);
            final String bValue = valueFromKey(b);
            final String cValue = valueFromKey(c);
            final ImmutableIntKeyMap<String> map = newMapBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final int firstKey = map.keyAt(0);
            final int secondKey = (size >= 2)? map.keyAt(1) : 0;
            final int thirdKey = (size >= 3)? map.keyAt(2) : 0;
            final String firstValue = map.valueAt(0);
            final String secondValue = (size >= 2)? map.valueAt(1) : null;
            final String thirdValue = (size >= 3)? map.valueAt(2) : null;

            final ImmutableIntKeyMap<String> sliceA = map.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertEquals(firstKey, sliceA.keyAt(0));
            assertSame(firstValue, sliceA.valueAt(0));

            final ImmutableIntKeyMap<String> sliceB = map.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertEquals(secondKey, sliceB.keyAt(0));
                assertSame(secondValue, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final ImmutableIntKeyMap<String> sliceC = map.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertEquals(thirdKey, sliceC.keyAt(0));
                assertSame(thirdValue, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final ImmutableIntKeyMap<String> sliceAB = map.slice(new ImmutableIntRange(0, 1));
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

            final ImmutableIntKeyMap<String> sliceBC = map.slice(new ImmutableIntRange(1, 2));
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
    public void testSkipWhenEmpty() {
        final ImmutableIntKeyMap<String> set = newMapBuilder().build();
        assertSame(set, set.skip(0));
        assertSame(set, set.skip(1));
        assertSame(set, set.skip(20));
    }

    @Test
    @Override
    public void testSkip() {
        withInt(a -> withInt(b -> withInt(c -> {
            final String aValue = valueFromKey(a);
            final String bValue = valueFromKey(b);
            final String cValue = valueFromKey(c);

            final ImmutableIntKeyMap<String> set = newMapBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            final int size = set.size();
            final int secondKey = (size >= 2)? set.keyAt(1) : 0;
            final String secondValue = (size >= 2)? set.valueAt(1) : null;
            final int thirdKey = (size == 3)? set.keyAt(2) : 0;
            final String thirdValue = (size == 3)? set.valueAt(2) : null;

            assertSame(set, set.skip(0));

            final ImmutableIntKeyMap<String> skip1 = set.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertEquals(secondKey, skip1.keyAt(0));
                assertSame(secondValue, skip1.valueAt(0));
                if (size == 3) {
                    assertEquals(thirdKey, skip1.keyAt(1));
                    assertSame(thirdValue, skip1.valueAt(1));
                }
            }

            final ImmutableIntKeyMap<String> empty = ImmutableIntKeyMap.empty();
            final ImmutableIntKeyMap<String> skip2 = set.skip(2);
            if (size == 3) {
                assertEquals(thirdKey, skip2.keyAt(0));
                assertSame(thirdValue, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertSame(empty, skip2);
            }

            assertSame(empty, set.skip(3));
            assertSame(empty, set.skip(4));
            assertSame(empty, set.skip(24));
        })));
    }

    @Test
    public void testTakeWhenEmpty() {
        final ImmutableIntKeyMap<String> map = newMapBuilder().build();
        assertSame(map, map.take(0));
        assertSame(map, map.take(1));
        assertSame(map, map.take(2));
        assertSame(map, map.take(24));
    }

    @Test
    public void testTake() {
        withInt(a -> withInt(b -> withInt(c -> {
            final String aValue = valueFromKey(a);
            final String bValue = valueFromKey(b);
            final String cValue = valueFromKey(c);
            final ImmutableIntKeyMap<String> map = newMapBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final int firstKey = map.keyAt(0);
            final String firstValue = map.valueAt(0);

            assertSame(ImmutableIntKeyMap.empty(), map.take(0));

            final ImmutableIntKeyMap<String> take1 = map.take(1);
            if (size > 1) {
                assertEquals(1, take1.size());
                assertEquals(firstKey, take1.keyAt(0));
                assertSame(firstValue, take1.valueAt(0));
            }
            else {
                assertSame(map, take1);
            }

            final ImmutableIntKeyMap<String> take2 = map.take(2);
            if (size > 2) {
                assertEquals(2, take2.size());
                assertEquals(firstKey, take2.keyAt(0));
                assertSame(firstValue, take2.valueAt(0));
                assertEquals(map.keyAt(1), take2.keyAt(1));
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
    @Override
    public void testSkipLastWhenEmpty() {
        final ImmutableIntKeyMap<String> map = newMapBuilder().build();
        assertSame(map, map.skipLast(0));
        assertSame(map, map.skipLast(1));
        assertSame(map, map.skipLast(2));
        assertSame(map, map.skipLast(24));
    }

    @Test
    @Override
    public void testSkipLast() {
        withInt(a -> withInt(b -> withInt(c -> {
            final String aValue = valueFromKey(a);
            final String bValue = valueFromKey(b);
            final String cValue = valueFromKey(c);
            final ImmutableIntKeyMap<String> map = newMapBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            assertSame(map, map.skipLast(0));

            final int size = map.size();
            final int firstKey = map.keyAt(0);
            final String firstValue = map.valueAt(0);
            final int secondKey = (size >= 2)? map.keyAt(1) : 0;
            final String secondValue = (size >= 2)? map.valueAt(1) : null;
            final ImmutableIntKeyMap<String> empty = ImmutableIntKeyMap.empty();

            final ImmutableIntKeyMap<String> map1 = map.skipLast(1);
            if (size == 1) {
                assertSame(empty, map1);
            }
            else {
                assertEquals(size - 1, map1.size());
                assertEquals(firstKey, map1.keyAt(0));
                assertSame(firstValue, map1.valueAt(0));
                if (size == 3) {
                    assertEquals(secondKey, map1.keyAt(1));
                    assertSame(secondValue, map1.valueAt(1));
                }
            }

            final ImmutableIntKeyMap<String> map2 = map.skipLast(2);
            if (size < 3) {
                assertSame(empty, map2);
            }
            else {
                assertEquals(1, map2.size());
                assertEquals(firstKey, map2.keyAt(0));
                assertSame(firstValue, map2.valueAt(0));
            }

            assertSame(empty, map.skipLast(3));
            assertSame(empty, map.skipLast(4));
            assertSame(empty, map.skipLast(24));
        })));
    }

    @Test
    @Override
    public void testTakeLastWhenEmpty() {
        final ImmutableIntKeyMap<String> map = newMapBuilder().build();
        assertSame(map, map.takeLast(0));
        assertSame(map, map.takeLast(1));
        assertSame(map, map.takeLast(2));
        assertSame(map, map.takeLast(24));
    }

    @Test
    @Override
    public void testTakeLast() {
        withInt(a -> withInt(b -> withInt(c -> {
            final String aValue = valueFromKey(a);
            final String bValue = valueFromKey(b);
            final String cValue = valueFromKey(c);
            final ImmutableIntKeyMap<String> map = newMapBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            assertSame(ImmutableIntKeyMap.empty(), map.takeLast(0));

            final int size = map.size();
            final int secondKey = (size >= 2)? map.keyAt(1) : 0;
            final String secondValue = (size >= 2)? map.valueAt(1) : null;
            final int thirdKey = (size >= 3)? map.keyAt(2) : 0;
            final String thirdValue = (size >= 3)? map.valueAt(2) : null;

            final ImmutableIntKeyMap<String> take1 = map.takeLast(1);
            if (size == 1) {
                assertSame(map, take1);
            }
            else {
                assertEquals(1, take1.size());
                assertEquals((size == 2)? secondKey : thirdKey, take1.keyAt(0));
                assertSame((size == 2)? secondValue : thirdValue, take1.valueAt(0));
            }

            final ImmutableIntKeyMap<String> take2 = map.takeLast(2);
            if (size <= 2) {
                assertSame(map, take2);
            }
            else {
                assertEquals(2, take2.size());
                assertEquals(secondKey, take2.keyAt(0));
                assertSame(secondValue, take2.valueAt(0));
                assertEquals(thirdKey, take2.keyAt(1));
                assertSame(thirdValue, take2.valueAt(1));
            }

            assertSame(map, map.takeLast(3));
            assertSame(map, map.takeLast(4));
            assertSame(map, map.takeLast(24));
        })));
    }

    static final class HashCodeKeyTraversableBuilder<E> implements ImmutableTransformableBuilder<E> {
        private final ImmutableIntKeyMap.Builder<E> builder = new ImmutableIntKeyMap.Builder<>();

        @Override
        public HashCodeKeyTraversableBuilder<E> add(E element) {
            builder.put(SortUtils.hashCode(element), element);
            return this;
        }

        @Override
        public ImmutableTransformable<E> build() {
            return builder.build();
        }
    }
}

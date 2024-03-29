package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withInt;

public final class ImmutableIntValueSortedMapTest implements ImmutableIntValueMapTest<String, ImmutableIntTransformableBuilder, ImmutableIntValueSortedMap.Builder<String>> {

    @Override
    public ImmutableIntValueSortedMap.Builder<String> newBuilder() {
        return new ImmutableIntValueSortedMap.Builder<>(SortUtils::compareCharSequenceByUnicode);
    }

    @Override
    public void withKey(Procedure<String> procedure) {
        final String[] values = {null, "", " ", "abcd", "0"};
        for (String value : values) {
            procedure.apply(value);
        }
    }

    @Override
    public void withSortFunc(Procedure<SortFunction<String>> procedure) {
        procedure.apply(SortUtils::compareCharSequenceByUnicode);
        procedure.apply(SortUtils::compareByHashCode);
    }

    @Override
    public String keyFromInt(int value) {
        return Integer.toString(value);
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntTransformableBuilder>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new SameKeyAndValueTraversableBuilder(sortFunc)));
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<String, ImmutableIntValueSortedMap.Builder<String>>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new ImmutableIntValueSortedMap.Builder<>(sortFunc)));
    }

    @Override
    public IntTransformableBuilder newIntBuilder() {
        return new SameKeyAndValueTraversableBuilder(SortUtils::compareByHashCode);
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withInt(procedure);
    }

    @Test
    @Override
    public void testFilterByEntryWhenEmpty() {
        final Predicate<IntValueMapEntry<String>> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            final ImmutableIntValueSortedMap<String> empty = supplier.newBuilder().build();
            final ImmutableIntValueSortedMap<String> filtered = empty.filterByEntry(f);
            assertSame(empty, filtered);
            assertTrue(filtered.isEmpty());
        });
    }

    @Test
    @Override
    public void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final IntValueMap.Entry<String> entry = new IntValueMap.Entry<>(0, key, valueFromKey(key));
            final ImmutableIntValueSortedMap<String> map = supplier.newBuilder().put(key, entry.value()).build();
            final ImmutableIntValueSortedMap<String> filtered = map.filterByEntry(f);

            if (f.apply(entry)) {
                assertSame(map, filtered);
            }
            else {
                assertTrue(filtered.isEmpty());
            }
        })));
    }

    @Test
    @Override
    public void testFilterByEntryForMultipleElements() {
        withFilterByEntryFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableIntValueSortedMap<String> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableIntValueSortedMap<String> filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            if (filteredSize == map.size()) {
                assertSame(map, filtered);
            }
            else {
                int counter = 0;
                for (IntValueMap.Entry<String> entry : map.entries()) {
                    if (f.apply(entry)) {
                        assertEquals(entry.value(), filtered.get(entry.key()));
                        counter++;
                    }
                }
                assertEquals(filteredSize, counter);
            }
        }))));
    }

    @Test
    void testPutAllMustReturnAnImmutableHashMap() {
        final ImmutableIntValueSortedMap<String> map = newBuilder().build();
        final ImmutableIntValueSortedMap<String> result = map.putAll(map);
        assertSame(result, map);
    }

    @Test
    @Override
    public void testSliceWhenEmpty() {
        final ImmutableIntValueSortedMap<String> map = newBuilder().build();
        assertSame(map, map.slice(new ImmutableIntRange(0, 0)));
        assertSame(map, map.slice(new ImmutableIntRange(1, 1)));
        assertSame(map, map.slice(new ImmutableIntRange(2, 2)));
        assertSame(map, map.slice(new ImmutableIntRange(0, 1)));
        assertSame(map, map.slice(new ImmutableIntRange(1, 2)));
        assertSame(map, map.slice(new ImmutableIntRange(0, 2)));
    }

    @Test
    @Override
    public void testSlice() {
        withKey(a -> withKey(b -> withKey(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);
            final ImmutableIntValueSortedMap<String> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final String firstKey = map.keyAt(0);
            final String secondKey = (size >= 2)? map.keyAt(1) : null;
            final String thirdKey = (size >= 3)? map.keyAt(2) : null;
            final int firstValue = map.valueAt(0);
            final int secondValue = (size >= 2)? map.valueAt(1) : 0;
            final int thirdValue = (size >= 3)? map.valueAt(2) : 0;

            final ImmutableIntValueSortedMap<String> sliceA = map.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(firstKey, sliceA.keyAt(0));
            assertEquals(firstValue, sliceA.valueAt(0));

            final ImmutableIntValueSortedMap<String> sliceB = map.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(secondKey, sliceB.keyAt(0));
                assertEquals(secondValue, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final ImmutableIntValueSortedMap<String> sliceC = map.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(thirdKey, sliceC.keyAt(0));
                assertEquals(thirdValue, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final ImmutableIntValueSortedMap<String> sliceAB = map.slice(new ImmutableIntRange(0, 1));
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

            final ImmutableIntValueSortedMap<String> sliceBC = map.slice(new ImmutableIntRange(1, 2));
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

    @Test
    public void testSkipWhenEmpty() {
        final ImmutableIntValueSortedMap<String> set = newBuilder().build();
        assertSame(set, set.skip(0));
        assertSame(set, set.skip(1));
        assertSame(set, set.skip(20));
    }

    @Test
    public void testSkip() {
        withKey(a -> withKey(b -> withKey(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);

            final ImmutableIntValueSortedMap<String> set = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            final int size = set.size();
            final String secondKey = (size >= 2)? set.keyAt(1) : null;
            final int secondValue = (size >= 2)? set.valueAt(1) : 0;
            final String thirdKey = (size == 3)? set.keyAt(2) : null;
            final int thirdValue = (size == 3)? set.valueAt(2) : 0;

            assertSame(set, set.skip(0));

            final ImmutableIntValueSortedMap<String> skip1 = set.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertSame(secondKey, skip1.keyAt(0));
                assertEquals(secondValue, skip1.valueAt(0));
                if (size == 3) {
                    assertSame(thirdKey, skip1.keyAt(1));
                    assertEquals(thirdValue, skip1.valueAt(1));
                }
            }

            final ImmutableIntValueSortedMap<String> skip2 = set.skip(2);
            if (size == 3) {
                assertSame(thirdKey, skip2.keyAt(0));
                assertEquals(thirdValue, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertTrue(skip2.isEmpty());
            }

            assertTrue(set.skip(3).isEmpty());
            assertTrue(set.skip(4).isEmpty());
            assertTrue(set.skip(24).isEmpty());
        })));
    }

    @Test
    public void testTakeWhenEmpty() {
        final ImmutableIntValueSortedMap<String> map = newBuilder().build();
        assertSame(map, map.take(0));
        assertSame(map, map.take(1));
        assertSame(map, map.take(2));
        assertSame(map, map.take(24));
    }

    @Test
    public void testTake() {
        withKey(a -> withKey(b -> withKey(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);
            final ImmutableIntValueSortedMap<String> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final String firstKey = map.keyAt(0);
            final int firstValue = map.valueAt(0);

            assertTrue(map.take(0).isEmpty());

            final ImmutableIntValueSortedMap<String> take1 = map.take(1);
            if (size > 1) {
                assertEquals(1, take1.size());
                assertSame(firstKey, take1.keyAt(0));
                assertEquals(firstValue, take1.valueAt(0));
            }
            else {
                assertSame(map, take1);
            }

            final ImmutableIntValueSortedMap<String> take2 = map.take(2);
            if (size > 2) {
                assertEquals(2, take2.size());
                assertSame(firstKey, take2.keyAt(0));
                assertEquals(firstValue, take2.valueAt(0));
                assertSame(map.keyAt(1), take2.keyAt(1));
                assertEquals(map.valueAt(1), take2.valueAt(1));
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
    public void testSkipLastWhenEmpty() {
        final ImmutableIntValueSortedMap<String> map = newBuilder().build();
        assertSame(map, map.skipLast(0));
        assertSame(map, map.skipLast(1));
        assertSame(map, map.skipLast(2));
        assertSame(map, map.skipLast(24));
    }

    @Test
    public void testSkipLast() {
        withKey(a -> withKey(b -> withKey(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);
            final ImmutableIntValueSortedMap<String> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            assertSame(map, map.skipLast(0));

            final int size = map.size();
            final String firstKey = map.keyAt(0);
            final int firstValue = map.valueAt(0);
            final String secondKey = (size >= 2)? map.keyAt(1) : null;
            final int secondValue = (size >= 2)? map.valueAt(1) : 0;

            final ImmutableIntValueSortedMap<String> map1 = map.skipLast(1);
            assertEquals(size - 1, map1.size());
            if (size >= 2) {
                assertSame(firstKey, map1.keyAt(0));
                assertEquals(firstValue, map1.valueAt(0));
                if (size == 3) {
                    assertSame(secondKey, map1.keyAt(1));
                    assertEquals(secondValue, map1.valueAt(1));
                }
            }

            final ImmutableIntValueSortedMap<String> map2 = map.skipLast(2);
            if (size < 3) {
                assertTrue(map2.isEmpty());
            }
            else {
                assertEquals(1, map2.size());
                assertSame(firstKey, map2.keyAt(0));
                assertEquals(firstValue, map2.valueAt(0));
            }

            assertTrue(map.skipLast(3).isEmpty());
            assertTrue(map.skipLast(4).isEmpty());
            assertTrue(map.skipLast(24).isEmpty());
        })));
    }

    @Test
    @Override
    public void testTakeLastWhenEmpty() {
        final ImmutableIntValueSortedMap<String> map = newBuilder().build();
        assertSame(map, map.takeLast(0));
        assertSame(map, map.takeLast(1));
        assertSame(map, map.takeLast(2));
        assertSame(map, map.takeLast(24));
    }

    @Test
    @Override
    public void testTakeLast() {
        withKey(a -> withKey(b -> withKey(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);
            final ImmutableIntValueSortedMap<String> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            assertTrue(map.takeLast(0).isEmpty());

            final int size = map.size();
            final String secondKey = (size >= 2)? map.keyAt(1) : null;
            final int secondValue = (size >= 2)? map.valueAt(1) : 0;
            final String thirdKey = (size >= 3)? map.keyAt(2) : null;
            final int thirdValue = (size >= 3)? map.valueAt(2) : 0;

            final ImmutableIntValueSortedMap<String> take1 = map.takeLast(1);
            if (size == 1) {
                assertSame(map, take1);
            }
            else {
                assertEquals(1, take1.size());
                assertSame((size == 2)? secondKey : thirdKey, take1.keyAt(0));
                assertEquals((size == 2)? secondValue : thirdValue, take1.valueAt(0));
            }

            final ImmutableIntValueSortedMap<String> take2 = map.takeLast(2);
            if (size <= 2) {
                assertSame(map, take2);
            }
            else {
                assertEquals(2, take2.size());
                assertSame(secondKey, take2.keyAt(0));
                assertEquals(secondValue, take2.valueAt(0));
                assertSame(thirdKey, take2.keyAt(1));
                assertEquals(thirdValue, take2.valueAt(1));
            }

            assertSame(map, map.takeLast(3));
            assertSame(map, map.takeLast(4));
            assertSame(map, map.takeLast(24));
        })));
    }

    private static final class SameKeyAndValueTraversableBuilder implements ImmutableIntTransformableBuilder {
        private final ImmutableIntValueSortedMap.Builder<String> builder;

        SameKeyAndValueTraversableBuilder(SortFunction<String> sortFunction) {
            builder = new ImmutableIntValueSortedMap.Builder<>(sortFunction);
        }

        @Override
        public SameKeyAndValueTraversableBuilder add(int value) {
            builder.put(Integer.toString(value), value);
            return this;
        }

        @Override
        public ImmutableIntTransformable build() {
            return builder.build();
        }
    }
}

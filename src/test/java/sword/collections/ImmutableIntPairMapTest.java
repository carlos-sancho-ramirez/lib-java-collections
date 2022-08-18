package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withInt;

public final class ImmutableIntPairMapTest implements IntPairMapTest<ImmutableIntTransformableBuilder, ImmutableIntPairMap.Builder>, ImmutableIntTransformableTest<ImmutableIntTransformableBuilder> {

    @Override
    public ImmutableIntPairMap.Builder newBuilder() {
        return new ImmutableIntPairMap.Builder();
    }

    @Override
    public void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    @Override
    public void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v * v);
        procedure.apply(v -> v + 1);
    }

    private static final ImmutableIntPairMap[] IMMUTABLE_INT_PAIR_MAP_VALUEs = new ImmutableIntPairMap[] {
            null,
            new ImmutableIntPairMap.Builder().build(),
            new ImmutableIntPairMap.Builder().put(0, 0).build(),
            new ImmutableIntPairMap.Builder().put(0, -4).build(),
            new ImmutableIntPairMap.Builder().put(0, 6).build(),
            new ImmutableIntPairMap.Builder().put(Integer.MIN_VALUE, 0).build(),
            new ImmutableIntPairMap.Builder().put(Integer.MAX_VALUE, 0).build(),
            new ImmutableIntPairMap.Builder().put(124, 12).build(),
            new ImmutableIntPairMap.Builder().put(127, -17).build(),
            new ImmutableIntPairMap.Builder().put(125, 0).build(),
            new ImmutableIntPairMap.Builder().put(-3, 13).build(),
            new ImmutableIntPairMap.Builder().put(-45, 0).build(),
            new ImmutableIntPairMap.Builder().put(-42, -1).build(),
            new ImmutableIntPairMap.Builder().put(0, -4).put(12234, 12345).build(),
            new ImmutableIntPairMap.Builder().put(0, 4).put(1, 4).build(), // Intentionally no reversable
            new ImmutableIntPairMap.Builder().put(-34, -33).put(2, 3).put(Integer.MAX_VALUE, Integer.MIN_VALUE).build()
    };

    private void withImmutableSparseIntArray(Procedure<ImmutableIntPairMap> procedure) {
        final int length = IMMUTABLE_INT_PAIR_MAP_VALUEs.length;
        for (int i = 0; i < length; i++) {
            procedure.apply(IMMUTABLE_INT_PAIR_MAP_VALUEs[i]);
        }
    }

    private boolean valueIsEven(int value) {
        return (value & 1) == 0;
    }

    @Override
    public IntTransformableBuilder newIntBuilder() {
        return new SameKeyAndValueTraversableBuilder();
    }

    @Override
    public void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::valueIsEven);
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntTransformableBuilder>> procedure) {
        procedure.apply(SameKeyAndValueTraversableBuilder::new);
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntPairMapBuilderSupplier<ImmutableIntPairMap.Builder>> procedure) {
        procedure.apply(ImmutableIntPairMap.Builder::new);
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withInt(procedure);
    }

    @Test
    void testPutMethod() {
        withImmutableSparseIntArray(array -> withInt(key -> withInt(value -> {
            if (array != null) {
                boolean contained = false;
                for (int i = 0; i < array.size(); i++) {
                    if (array.keyAt(i) == key) {
                        contained = true;
                        break;
                    }
                }

                final ImmutableIntPairMap newArray = array.put(key, value);

                if (!contained) {
                    final ImmutableIntPairMap.Builder builder = new ImmutableIntPairMap.Builder();
                    for (IntPairMap.Entry entry : array.entries()) {
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
            final ImmutableIntPairMap thisMap = newBuilder().build();
            final ImmutableIntPairMap thatMap = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .build();

            assertEquals(thatMap, thisMap.putAll(thatMap));
        }));
    }

    @Test
    void testPutAllMethodForEmptyGivenMap() {
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap thisMap = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .build();

            assertSame(thisMap, thisMap.putAll(newBuilder().build()));
        }));
    }

    @Test
    void testPutAllMethodForMultipleElementsInTheGivenMap() {
        withInt(a -> withInt(b -> withInt(c -> withInt(d -> {
            final ImmutableIntPairMap thisMap = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .build();

            final ImmutableIntPairMap thatMap = newBuilder()
                    .put(c, c)
                    .put(d, d)
                    .build();

            final ImmutableIntPairMap.Builder builder = newBuilder();
            for (IntPairMap.Entry entry : thisMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            for (IntPairMap.Entry entry : thatMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            assertEquals(builder.build(), thisMap.putAll(thatMap));
        }))));
    }

    @Test
    void testReverseMethod() {
        withImmutableSparseIntArray(array -> {
            if (array != null) {
                // Check if the array is reversable, so no duplicated values should be found
                final int length = array.size();
                boolean duplicated = false;
                for (int i = 0; i < length - 1; i++) {
                    for (int j = i + 1; j < length; j++) {
                        if (array.valueAt(i) == array.valueAt(j)) {
                            duplicated = true;
                        }
                        break;
                    }

                    if (duplicated) {
                        break;
                    }
                }

                if (!duplicated) {
                    final ImmutableIntPairMap reversed = array.reverse();
                    assertEquals(length, reversed.size());

                    for (int i = 0; i < length; i++) {
                        assertEquals(reversed.keyAt(i), array.get(reversed.valueAt(i)));
                    }
                }
            }
        });
    }

    @Test
    void testKeySetWhenEmpty() {
        final ImmutableIntPairMap empty = ImmutableIntPairMap.empty();
        assertSame(ImmutableIntArraySet.empty(), empty.keySet());
    }

    @Test
    void testToImmutableForEmpty() {
        final ImmutableIntPairMap map = newBuilder().build();
        assertSame(map, map.toImmutable());
    }

    @Test
    void testMutateForEmpty() {
        final ImmutableIntPairMap map1 = newBuilder().build();
        final MutableIntPairMap map2 = map1.mutate();

        assertTrue(map2.isEmpty());

        map2.put(1, 4);
        assertEquals(0, map1.get(1, 0));
    }

    @Test
    void testToImmutable() {
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap map1 = newBuilder().put(a, 1).put(b, 2).build();
            final ImmutableIntPairMap map2 = map1.toImmutable();
            assertSame(map1, map2);
        }));
    }

    @Test
    void testMutate() {
        final int defValue = -4;
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap map1 = newBuilder().put(a, 1).put(b, 2).build();
            final MutableIntPairMap map2 = map1.mutate();

            final Iterator<IntPairMap.Entry> it1 = map1.entries().iterator();
            final Iterator<IntPairMap.Entry> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                final IntPairMap.Entry item1 = it1.next();
                final IntPairMap.Entry item2 = it2.next();
                assertEquals(item1.key(), item2.key());
                assertEquals(item1.value(), item2.value());
            }
            assertFalse(it2.hasNext());

            map2.remove(b);
            assertEquals(2, map1.get(b, defValue));
            assertEquals(defValue, map2.get(b, defValue));
        }));
    }

    @Test
    void testFilterByKeyReturnTheSameInstanceAndTypeWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        final ImmutableIntPairMap map = newBuilder().build();
        final ImmutableIntPairMap filtered = map.filterByKey(f);
        assertSame(map, filtered);
    }

    @Test
    void testFilterByKeyReturnTheSameInstanceAndType() {
        final IntPredicate f = unused -> true;
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableIntPairMap filtered = map.filterByKey(f);
            assertSame(map, filtered);
        }));
    }

    @Test
    @Override
    public void testFilterByEntryWhenEmpty() {
        final Predicate<IntPairMapEntry> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            final ImmutableIntPairMap map = supplier.newBuilder().build();
            final ImmutableIntPairMap filtered = map.filterByEntry(f);
            assertSame(map, filtered);
            assertTrue(filtered.isEmpty());
        });
    }

    @Test
    @Override
    public void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withInt(key -> withMapBuilderSupplier(supplier -> {
            final IntPairMap.Entry entry = new IntPairMap.Entry(0, key, valueFromKey(key));
            final ImmutableIntPairMap map = supplier.newBuilder().put(key, entry.value()).build();
            final ImmutableIntPairMap filtered = map.filterByEntry(f);
            final ImmutableIntPairMap expected = f.apply(entry)? map : supplier.newBuilder().build();
            assertSame(expected, filtered);
        })));
    }

    @Test
    @Override
    public void testFilterByEntryForMultipleElements() {
        withFilterByEntryFunc(f -> withInt(a -> withInt(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableIntPairMap map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableIntPairMap filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            if (filteredSize == 0) {
                assertSame(supplier.newBuilder().build(), filtered);
            }
            else if (filteredSize == map.size()) {
                assertSame(map, filtered);
            }
            else {
                int counter = 0;
                for (IntPairMap.Entry entry : map.entries()) {
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
    @Override
    public void testSliceWhenEmpty() {
        final ImmutableIntPairMap map = newBuilder().build();
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
        withInt(a -> withInt(b -> withInt(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);
            final ImmutableIntPairMap map = newBuilder()
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

            final ImmutableIntPairMap sliceA = map.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertEquals(firstKey, sliceA.keyAt(0));
            assertEquals(firstValue, sliceA.valueAt(0));

            final ImmutableIntPairMap sliceB = map.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertEquals(secondKey, sliceB.keyAt(0));
                assertEquals(secondValue, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final ImmutableIntPairMap sliceC = map.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertEquals(thirdKey, sliceC.keyAt(0));
                assertEquals(thirdValue, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final ImmutableIntPairMap sliceAB = map.slice(new ImmutableIntRange(0, 1));
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

            final ImmutableIntPairMap sliceBC = map.slice(new ImmutableIntRange(1, 2));
            if (size == 1) {
                assertEquals(0, sliceBC.size());
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

            assertSame(map, map.slice(new ImmutableIntRange(0, 2)));
            assertSame(map, map.slice(new ImmutableIntRange(0, 3)));
        })));
    }

    @Test
    @Override
    public void testSkipWhenEmpty() {
        final ImmutableIntPairMap map = newBuilder().build();
        assertSame(map, map.skip(0));
        assertSame(map, map.skip(1));
        assertSame(map, map.skip(20));
    }

    @Test
    @Override
    public void testSkip() {
        withInt(a -> withInt(b -> withInt(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);

            final ImmutableIntPairMap map = newBuilder()
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

            final ImmutableIntPairMap skip1 = map.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertEquals(secondKey, skip1.keyAt(0));
                assertEquals(secondValue, skip1.valueAt(0));
                if (size == 3) {
                    assertEquals(thirdKey, skip1.keyAt(1));
                    assertEquals(thirdValue, skip1.valueAt(1));
                }
            }

            final ImmutableIntPairMap empty = ImmutableIntPairMap.empty();
            final ImmutableIntPairMap skip2 = map.skip(2);
            if (size == 3) {
                assertEquals(thirdKey, skip2.keyAt(0));
                assertEquals(thirdValue, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertSame(empty, skip2);
            }

            assertSame(empty, map.skip(3));
            assertSame(empty, map.skip(4));
            assertSame(empty, map.skip(24));
        })));
    }

    static final class SameKeyAndValueTraversableBuilder implements ImmutableIntTransformableBuilder {
        private final ImmutableIntPairMap.Builder builder = new ImmutableIntPairMap.Builder();

        @Override
        public SameKeyAndValueTraversableBuilder add(int value) {
            builder.put(value, value);
            return this;
        }

        @Override
        public ImmutableIntPairMap build() {
            return builder.build();
        }
    }
}

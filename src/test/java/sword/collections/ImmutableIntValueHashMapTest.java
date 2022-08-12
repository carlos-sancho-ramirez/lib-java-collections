package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withInt;

public final class ImmutableIntValueHashMapTest implements ImmutableIntValueMapTest<String, ImmutableIntTransformableBuilder, ImmutableIntValueHashMap.Builder<String>> {

    @Override
    public ImmutableIntValueHashMap.Builder<String> newBuilder() {
        return new ImmutableIntValueHashMap.Builder<>();
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<String, ImmutableIntValueHashMap.Builder<String>>> procedure) {
        procedure.apply(ImmutableIntValueHashMap.Builder::new);
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
    public void assertEmpty(ImmutableIntValueMap<String> map) {
        assertSame(newBuilder().build(), map);
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntTransformableBuilder>> procedure) {
        procedure.apply(SameKeyAndValueTraversableBuilder::new);
    }

    @Override
    public IntTransformableBuilder newIntBuilder() {
        return new SameKeyAndValueTraversableBuilder();
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withInt(procedure);
    }

    @Test
    void testFilterByKeyReturnTheSameInstanceAndTypeWhenEmpty() {
        final Predicate<String> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        final ImmutableIntValueHashMap<String> map = newBuilder().build();
        final ImmutableIntValueHashMap<String> filtered = map.filterByKey(f);
        assertSame(map, filtered);
    }

    @Test
    void testFilterByKeyReturnTheSameInstanceAndType() {
        final Predicate<String> f = unused -> true;
        withKey(a -> withKey(b -> {
            final ImmutableIntValueHashMap<String> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableIntValueMap<String> filtered = map.filterByKey(f);
            assertSame(map, filtered);
        }));
    }

    @Test
    @Override
    public void testFilterByEntryWhenEmpty() {
        final Predicate<IntValueMapEntry<String>> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            final ImmutableIntValueHashMap<String> empty = supplier.newBuilder().build();
            final ImmutableIntValueHashMap<String> filtered = empty.filterByEntry(f);
            assertSame(empty, filtered);
            assertTrue(filtered.isEmpty());
        });
    }

    @Test
    @Override
    public void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final IntValueMap.Entry<String> entry = new IntValueMap.Entry<>(0, key, valueFromKey(key));
            final ImmutableIntValueHashMap<String> map = supplier.newBuilder().put(key, entry.value()).build();
            final ImmutableIntValueHashMap<String> expected = f.apply(entry)? map : supplier.newBuilder().build();
            assertSame(expected, map.filterByEntry(f));
        })));
    }

    @Test
    @Override
    public void testFilterByEntryForMultipleElements() {
        withFilterByEntryFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableIntValueHashMap<String> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableIntValueHashMap<String> filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            if (filteredSize == 0) {
                assertSame(supplier.newBuilder().build(), filtered);
            }
            else if (filteredSize == map.size()) {
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
        final ImmutableIntValueHashMap<String> map = newBuilder().build();
        final ImmutableIntValueHashMap<String> result = map.putAll(map);
        assertSame(result, map);
    }

    @Test
    void testSliceWhenEmpty() {
        final ImmutableIntValueHashMap<String> map = newBuilder().build();
        assertSame(map, map.slice(new ImmutableIntRange(0, 0)));
        assertSame(map, map.slice(new ImmutableIntRange(1, 1)));
        assertSame(map, map.slice(new ImmutableIntRange(2, 2)));
        assertSame(map, map.slice(new ImmutableIntRange(0, 1)));
        assertSame(map, map.slice(new ImmutableIntRange(1, 2)));
        assertSame(map, map.slice(new ImmutableIntRange(0, 2)));
    }

    @Test
    public void testSlice() {
        withKey(a -> withKey(b -> withKey(c -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final int cValue = valueFromKey(c);
            final ImmutableIntValueHashMap<String> map = newBuilder()
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

            final ImmutableIntValueHashMap<String> sliceA = map.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(firstKey, sliceA.keyAt(0));
            assertEquals(firstValue, sliceA.valueAt(0));

            final ImmutableIntValueHashMap<String> sliceB = map.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(secondKey, sliceB.keyAt(0));
                assertEquals(secondValue, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final ImmutableIntValueHashMap<String> sliceC = map.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(thirdKey, sliceC.keyAt(0));
                assertEquals(thirdValue, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final ImmutableIntValueHashMap<String> sliceAB = map.slice(new ImmutableIntRange(0, 1));
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

            final ImmutableIntValueHashMap<String> sliceBC = map.slice(new ImmutableIntRange(1, 2));
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

    static final class SameKeyAndValueTraversableBuilder implements ImmutableIntTransformableBuilder {
        private final ImmutableIntValueHashMap.Builder<String> builder = new ImmutableIntValueHashMap.Builder<>();

        @Override
        public SameKeyAndValueTraversableBuilder add(int value) {
            builder.put(Integer.toString(value), value);
            return this;
        }

        @Override
        public ImmutableIntValueHashMap<String> build() {
            return builder.build();
        }
    }
}

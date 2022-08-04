package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class ImmutableSortedMapTest implements ImmutableMapTest<Integer, String, ImmutableTransformableBuilder<String>, ImmutableSortedMap.Builder<Integer, String>> {

    private static boolean sortInDescendantOrder(int a, int b) {
        return b > a;
    }

    @Override
    public ImmutableSortedMap.Builder<Integer, String> newBuilder() {
        return new ImmutableSortedMap.Builder<>(ImmutableSortedMapTest::sortInDescendantOrder);
    }

    @Override
    public void withKey(Procedure<Integer> procedure) {
        withInt(procedure::apply);
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableTransformableBuilder<String>>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new HashCodeKeyTransformableBuilder(sortFunc)));
    }

    @Override
    public void withValue(Procedure<String> procedure) {
        withString(procedure);
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
        procedure.apply(str -> (str == null)? 0 : str.hashCode());
    }

    @Override
    public void withSortFunc(Procedure<SortFunction<Integer>> procedure) {
        procedure.apply((a, b) -> a < b);
        procedure.apply((a, b) -> a > b);
    }

    @Override
    public void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
        procedure.apply((a, b) -> a + b);
    }

    @Override
    public String getTestValue() {
        return "value";
    }

    @Override
    public Integer keyFromInt(int value) {
        return value;
    }

    @Override
    public String valueFromKey(Integer key) {
        return (key == null)? null : Integer.toString(key);
    }

    @Override
    public void withMapBuilderSupplier(Procedure<MapBuilderSupplier<Integer, String, ImmutableSortedMap.Builder<Integer, String>>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new ImmutableSortedMap.Builder<>(sortFunc)));
    }

    @Test
    void testToImmutableMethod() {
        withKey(a -> withKey(b -> {
            final ImmutableMap<Integer, String> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            assertSame(map, map.toImmutable());
        }));
    }

    @Test
    void testPutMethod() {
        withKey(a -> withKey(b -> withKey(key -> withValue(value -> {
            final ImmutableMap<Integer, String> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final boolean contained = map.containsKey(key);
            final ImmutableMap<Integer, String> newMap = map.put(key, value);

            if (!contained) {
                final ImmutableHashMap.Builder<Integer, String> builder = new ImmutableHashMap.Builder<>();
                for (Map.Entry<Integer, String> entry : map.entries()) {
                    builder.put(entry.key(), entry.value());
                }
                assertEquals(builder.put(key, value).build(), newMap);
            }
            else {
                assertSame(map, map.put(key, valueFromKey(key)));

                final ImmutableSet<Integer> keySet = map.keySet();
                assertEquals(keySet, newMap.keySet());

                for (Integer k : keySet) {
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
    void testPutAllMustReturnAnImmutableHashMap() {
        final ImmutableSortedMap<Integer, String> map = newBuilder().build();
        final ImmutableSortedMap<Integer, String> result = map.putAll(map);
        assertSame(result, map);
    }

    private boolean hashCodeIsEven(Object value) {
        return value == null || (value.hashCode() & 1) == 0;
    }

    @Override
    public void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::hashCodeIsEven);
    }

    @Override
    public void withFilterByKeyFunc(Procedure<Predicate<Integer>> procedure) {
        procedure.apply(this::hashCodeIsEven);
    }

    @Test
    void testFilterByKeyReturnTheSameInstanceAndType() {
        final Predicate<Integer> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        final ImmutableSortedMap<Integer, String> map = newBuilder().build();
        final ImmutableSortedMap<Integer, String> filtered = map.filterByKey(f);
        assertSame(map, filtered);
    }

    @Test
    @Override
    public void testFilterByKeyNotWhenEmpty() {
        final Predicate<Integer> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            final ImmutableSortedMap<Integer, String> empty = supplier.newBuilder().build();
            final ImmutableSortedMap<Integer, String> filtered = empty.filterByKeyNot(f);
            assertSame(empty, filtered);
            assertTrue(filtered.isEmpty());
        });
    }

    @Test
    @Override
    public void testFilterByKeyNotForSingleElement() {
        withFilterByKeyFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final ImmutableSortedMap<Integer, String> map = supplier.newBuilder().put(key, valueFromKey(key)).build();
            final ImmutableSortedMap<Integer, String> filtered = map.filterByKeyNot(f);

            if (!f.apply(key)) {
                assertSame(map, filtered);
            }
            else {
                assertTrue(filtered.isEmpty());
            }
        })));
    }

    @Test
    @Override
    public void testFilterByKeyNotForMultipleElements() {
        withFilterByKeyFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableSortedMap<Integer, String> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableSortedMap<Integer, String> filtered = map.filterByKeyNot(f);

            if (filtered.size() == map.size()) {
                assertSame(map, filtered);
            }
            else {
                final TransformerWithKey<Integer, String> tr = filtered.iterator();
                for (Integer key : map.keySet()) {
                    if (!f.apply(key)) {
                        assertTrue(tr.hasNext());
                        assertSame(map.get(key), tr.next());
                        assertSame(key, tr.key());
                    }
                }
                assertFalse(tr.hasNext());
            }
        }))));
    }

    @Test
    @Override
    public void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final Map.Entry<Integer, String> entry = new Map.Entry<>(0, key, valueFromKey(key));
            final ImmutableSortedMap<Integer, String> map = supplier.newBuilder().put(key, entry.value()).build();
            final ImmutableSortedMap<Integer, String> filtered = map.filterByEntry(f);

            if (f.apply(entry)) {
                assertSame(map, filtered);
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        })));
    }

    @Test
    @Override
    public void testFilterByEntryForMultipleElements() {
        withFilterByEntryFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableSortedMap<Integer, String> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableSortedMap<Integer, String> filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            if (filteredSize == map.size()) {
                assertSame(map, filtered);
            }
            else {
                int counter = 0;
                for (Map.Entry<Integer, String> entry : map.entries()) {
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
    @Override
    public void testSlice() {
        withKey(a -> withKey(b -> withKey(c -> {
            final String aValue = valueFromKey(a);
            final String bValue = valueFromKey(b);
            final String cValue = valueFromKey(c);
            final ImmutableSortedMap<Integer, String> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final Integer firstKey = map.keyAt(0);
            final Integer secondKey = (size >= 2)? map.keyAt(1) : null;
            final Integer thirdKey = (size >= 3)? map.keyAt(2) : null;
            final String firstValue = map.valueAt(0);
            final String secondValue = (size >= 2)? map.valueAt(1) : null;
            final String thirdValue = (size >= 3)? map.valueAt(2) : null;

            final ImmutableSortedMap<Integer, String> sliceA = map.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(firstKey, sliceA.keyAt(0));
            assertSame(firstValue, sliceA.valueAt(0));

            final ImmutableSortedMap<Integer, String> sliceB = map.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(secondKey, sliceB.keyAt(0));
                assertSame(secondValue, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final ImmutableSortedMap<Integer, String> sliceC = map.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(thirdKey, sliceC.keyAt(0));
                assertSame(thirdValue, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final ImmutableSortedMap<Integer, String> sliceAB = map.slice(new ImmutableIntRange(0, 1));
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

            final ImmutableSortedMap<Integer, String> sliceBC = map.slice(new ImmutableIntRange(1, 2));
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

            assertSame(map, map.slice(new ImmutableIntRange(0, 2)));
            assertSame(map, map.slice(new ImmutableIntRange(0, 3)));
        })));
    }

    private static final class HashCodeKeyTransformableBuilder implements ImmutableTransformableBuilder<String> {
        private final ImmutableSortedMap.Builder<Integer, String> builder;

        HashCodeKeyTransformableBuilder(SortFunction<Integer> sortFunc) {
            builder = new ImmutableSortedMap.Builder<>(sortFunc);
        }

        @Override
        public HashCodeKeyTransformableBuilder add(String element) {
            builder.put(SortUtils.hashCode(element), element);
            return this;
        }

        @Override
        public ImmutableSortedMap<Integer, String> build() {
            return builder.build();
        }
    }
}

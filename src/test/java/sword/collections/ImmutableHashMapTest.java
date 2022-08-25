package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class ImmutableHashMapTest implements ImmutableMapTest<Integer, String, ImmutableTransformableBuilder<String>, ImmutableHashMap.Builder<Integer, String>> {

    @Override
    public ImmutableHashMap.Builder<Integer, String> newBuilder() {
        return new ImmutableHashMap.Builder<>();
    }

    @Override
    public void withKey(Procedure<Integer> procedure) {
        withInt(procedure::apply);
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableTransformableBuilder<String>>> procedure) {
        procedure.apply(HashCodeKeyTraversableBuilder::new);
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
    public void withMapBuilderSupplier(Procedure<MapBuilderSupplier<Integer, String, ImmutableHashMap.Builder<Integer, String>>> procedure) {
        procedure.apply(ImmutableHashMap.Builder::new);
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
            final ImmutableHashMap<Integer, String> map = newBuilder()
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

                final ImmutableHashSet<Integer> keySet = map.keySet();
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
        final ImmutableHashMap<Integer, String> map = newBuilder().build();
        final ImmutableHashMap<Integer, String> result = map.putAll(map);
        assertSame(result, map);
    }

    @Test
    @Override
    public void testFilterByKeyNotWhenEmpty() {
        final Predicate<Integer> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            final ImmutableHashMap<Integer, String> empty = supplier.newBuilder().build();
            final ImmutableHashMap<Integer, String> filtered = empty.filterByKeyNot(f);
            assertSame(empty, filtered);
            assertTrue(filtered.isEmpty());
        });
    }

    @Test
    @Override
    public void testFilterByKeyNotForSingleElement() {
        withFilterByKeyFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final ImmutableHashMap<Integer, String> map = supplier.newBuilder().put(key, valueFromKey(key)).build();
            final ImmutableHashMap<Integer, String> filtered = map.filterByKeyNot(f);
            final ImmutableHashMap<Integer, String> expected = f.apply(key)? supplier.newBuilder().build() : map;
            assertSame(expected, filtered);
        })));
    }

    @Test
    @Override
    public void testFilterByKeyNotForMultipleElements() {
        withFilterByKeyFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableHashMap<Integer, String> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableHashMap<Integer, String> filtered = map.filterByKeyNot(f);
            final int filteredSize = filtered.size();

            if (filteredSize == 0) {
                assertSame(supplier.newBuilder().build(), filtered);
            }
            else if (filteredSize == map.size()) {
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
            final ImmutableHashMap<Integer, String> map = supplier.newBuilder().put(key, entry.value()).build();
            final ImmutableHashMap<Integer, String> filtered = map.filterByEntry(f);

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
            final ImmutableHashMap<Integer, String> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableHashMap<Integer, String> filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            if (filteredSize == map.size()) {
                assertSame(map, filtered);
            }
            else if (filteredSize == 0) {
                assertSame(ImmutableHashMap.empty(), filtered);
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

    @Override
    public void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
        procedure.apply((a, b) -> a + b);
    }

    @Test
    void testFilterByKeyReturnTheSameInstanceAndType() {
        final Predicate<Integer> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        final ImmutableHashMap<Integer, String> map = newBuilder().build();
        final ImmutableHashMap<Integer, String> filtered = map.filterByKey(f);
        assertSame(map, filtered);
    }

    @Test
    @Override
    public void testSlice() {
        withKey(a -> withKey(b -> withKey(c -> {
            final String aValue = valueFromKey(a);
            final String bValue = valueFromKey(b);
            final String cValue = valueFromKey(c);
            final ImmutableHashMap<Integer, String> map = newBuilder()
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

            final ImmutableHashMap<Integer, String> sliceA = map.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(firstKey, sliceA.keyAt(0));
            assertSame(firstValue, sliceA.valueAt(0));

            final ImmutableHashMap<Integer, String> sliceB = map.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(secondKey, sliceB.keyAt(0));
                assertSame(secondValue, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final ImmutableHashMap<Integer, String> sliceC = map.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(thirdKey, sliceC.keyAt(0));
                assertSame(thirdValue, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final ImmutableHashMap<Integer, String> sliceAB = map.slice(new ImmutableIntRange(0, 1));
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

            final ImmutableHashMap<Integer, String> sliceBC = map.slice(new ImmutableIntRange(1, 2));
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

    @Test
    @Override
    public void testSkipWhenEmpty() {
        final ImmutableHashMap<Integer, String> set = newBuilder().build();
        assertSame(set, set.skip(0));
        assertSame(set, set.skip(1));
        assertSame(set, set.skip(20));
    }

    @Test
    @Override
    public void testSkip() {
        withKey(a -> withKey(b -> withKey(c -> {
            final String aValue = valueFromKey(a);
            final String bValue = valueFromKey(b);
            final String cValue = valueFromKey(c);

            final ImmutableHashMap<Integer, String> set = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            final int size = set.size();
            final Integer secondKey = (size >= 2)? set.keyAt(1) : null;
            final String secondValue = (size >= 2)? set.valueAt(1) : null;
            final Integer thirdKey = (size == 3)? set.keyAt(2) : null;
            final String thirdValue = (size == 3)? set.valueAt(2) : null;

            assertSame(set, set.skip(0));

            final ImmutableHashMap<Integer, String> skip1 = set.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertSame(secondKey, skip1.keyAt(0));
                assertSame(secondValue, skip1.valueAt(0));
                if (size == 3) {
                    assertSame(thirdKey, skip1.keyAt(1));
                    assertSame(thirdValue, skip1.valueAt(1));
                }
            }

            final ImmutableHashMap<Integer, String> empty = ImmutableHashMap.empty();
            final ImmutableHashMap<Integer, String> skip2 = set.skip(2);
            if (size == 3) {
                assertSame(thirdKey, skip2.keyAt(0));
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
    @Override
    public void testTakeWhenEmpty() {
        final ImmutableHashMap<Integer, String> map = newBuilder().build();
        assertSame(map, map.take(0));
        assertSame(map, map.take(1));
        assertSame(map, map.take(2));
        assertSame(map, map.take(24));
    }

    @Test
    @Override
    public void testTake() {
        withKey(a -> withKey(b -> withKey(c -> {
            final String aValue = valueFromKey(a);
            final String bValue = valueFromKey(b);
            final String cValue = valueFromKey(c);
            final ImmutableHashMap<Integer, String> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();

            final int size = map.size();
            final Integer firstKey = map.keyAt(0);
            final String firstValue = map.valueAt(0);

            assertSame(ImmutableHashMap.empty(), map.take(0));

            final ImmutableHashMap<Integer, String> take1 = map.take(1);
            if (size > 1) {
                assertEquals(1, take1.size());
                assertSame(firstKey, take1.keyAt(0));
                assertSame(firstValue, take1.valueAt(0));
            }
            else {
                assertSame(map, take1);
            }

            final ImmutableHashMap<Integer, String> take2 = map.take(2);
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
    @Override
    public void testSkipLastWhenEmpty() {
        final ImmutableHashMap<Integer, String> map = newBuilder().build();
        assertSame(map, map.skipLast(0));
        assertSame(map, map.skipLast(1));
        assertSame(map, map.skipLast(2));
        assertSame(map, map.skipLast(24));
    }

    @Test
    @Override
    public void testSkipLast() {
        withKey(a -> withKey(b -> withKey(c -> {
            final String aValue = valueFromKey(a);
            final String bValue = valueFromKey(b);
            final String cValue = valueFromKey(c);
            final ImmutableHashMap<Integer, String> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            assertSame(map, map.skipLast(0));

            final int size = map.size();
            final Integer firstKey = map.keyAt(0);
            final String firstValue = map.valueAt(0);
            final Integer secondKey = (size >= 2)? map.keyAt(1) : null;
            final String secondValue = (size >= 2)? map.valueAt(1) : null;
            final ImmutableHashMap<Integer, String> empty = ImmutableHashMap.empty();

            final ImmutableHashMap<Integer, String> map1 = map.skipLast(1);
            if (size == 1) {
                assertSame(empty, map1);
            }
            else {
                assertEquals(size - 1, map1.size());
                assertSame(firstKey, map1.keyAt(0));
                assertSame(firstValue, map1.valueAt(0));
                if (size == 3) {
                    assertSame(secondKey, map1.keyAt(1));
                    assertSame(secondValue, map1.valueAt(1));
                }
            }

            final ImmutableHashMap<Integer, String> map2 = map.skipLast(2);
            if (size < 3) {
                assertSame(empty, map2);
            }
            else {
                assertEquals(1, map2.size());
                assertSame(firstKey, map2.keyAt(0));
                assertSame(firstValue, map2.valueAt(0));
            }

            assertSame(empty, map.skipLast(3));
            assertSame(empty, map.skipLast(4));
            assertSame(empty, map.skipLast(24));
        })));
    }

    @Test
    public void testTakeLastWhenEmpty() {
        final ImmutableHashMap<Integer, String> map = newBuilder().build();
        assertSame(map, map.takeLast(0));
        assertSame(map, map.takeLast(1));
        assertSame(map, map.takeLast(2));
        assertSame(map, map.takeLast(24));
    }

    @Test
    public void testTakeLast() {
        withKey(a -> withKey(b -> withKey(c -> {
            final String aValue = valueFromKey(a);
            final String bValue = valueFromKey(b);
            final String cValue = valueFromKey(c);
            final ImmutableHashMap<Integer, String> map = newBuilder()
                    .put(a, aValue)
                    .put(b, bValue)
                    .put(c, cValue)
                    .build();
            assertSame(ImmutableHashMap.empty(), map.takeLast(0));

            final int size = map.size();
            final Integer secondKey = (size >= 2)? map.keyAt(1) : null;
            final String secondValue = (size >= 2)? map.valueAt(1) : null;
            final Integer thirdKey = (size >= 3)? map.keyAt(2) : null;
            final String thirdValue = (size >= 3)? map.valueAt(2) : null;

            final ImmutableHashMap<Integer, String> take1 = map.takeLast(1);
            if (size == 1) {
                assertSame(map, take1);
            }
            else {
                assertEquals(1, take1.size());
                assertSame((size == 2)? secondKey : thirdKey, take1.keyAt(0));
                assertSame((size == 2)? secondValue : thirdValue, take1.valueAt(0));
            }

            final ImmutableHashMap<Integer, String> take2 = map.takeLast(2);
            if (size <= 2) {
                assertSame(map, take2);
            }
            else {
                assertEquals(2, take2.size());
                assertSame(secondKey, take2.keyAt(0));
                assertSame(secondValue, take2.valueAt(0));
                assertSame(thirdKey, take2.keyAt(1));
                assertSame(thirdValue, take2.valueAt(1));
            }

            assertSame(map, map.takeLast(3));
            assertSame(map, map.takeLast(4));
            assertSame(map, map.takeLast(24));
        })));
    }

    static final class HashCodeKeyTraversableBuilder<E> implements ImmutableTransformableBuilder<E> {
        private final ImmutableHashMap.Builder<Integer, E> builder = new ImmutableHashMap.Builder<>();

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

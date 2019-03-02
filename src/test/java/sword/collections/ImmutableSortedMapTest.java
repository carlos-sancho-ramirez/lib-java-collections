package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class ImmutableSortedMapTest extends MapTest<Integer, String> implements ImmutableTransformableTest<String> {

    private static boolean sortInDescendantOrder(int a, int b) {
        return b > a;
    }

    @Override
    ImmutableMap.Builder<Integer, String> newBuilder() {
        return new ImmutableSortedMap.Builder<>(ImmutableSortedMapTest::sortInDescendantOrder);
    }

    @Override
    void withKey(Procedure<Integer> procedure) {
        withInt(procedure::apply);
    }

    @Override
    public void withTransformableBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableTransformableBuilder<String>>> procedure) {
        withSortFunc(sortFunc -> {
            procedure.apply(() -> new HashCodeKeyTransformableBuilder(sortFunc));
        });
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
    void withSortFunc(Procedure<SortFunction<Integer>> procedure) {
        procedure.apply((a, b) -> a < b);
        procedure.apply((a, b) -> a > b);
    }

    @Override
    String getTestValue() {
        return "value";
    }

    @Override
    Integer keyFromInt(int value) {
        return value;
    }

    @Override
    String valueFromKey(Integer key) {
        return (key == null)? null : Integer.toString(key);
    }

    @Override
    void withMapBuilderSupplier(Procedure<MapBuilderSupplier<Integer, String, MapBuilder<Integer, String>>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new ImmutableSortedMap.Builder<>(sortFunc)));
    }

    @Test
    public void testToImmutableMethod() {
        withKey(a -> withKey(b -> {
            final ImmutableMap<Integer, String> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            assertSame(map, map.toImmutable());
        }));
    }

    @Test
    public void testPutMethod() {
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

    private boolean hashCodeIsEven(String value) {
        return value == null || (value.hashCode() & 1) == 0;
    }

    @Override
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::hashCodeIsEven);
    }

    @Test
    @Override
    public void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableMap<Integer, String> map = newBuilder().build();
            assertSame(map, map.filter(f));
        });
    }

    @Test
    @Override
    public void testFilterForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final String value = Integer.toString(key);
            final ImmutableMap<Integer, String> map = newBuilder().put(key, value).build();
            final ImmutableMap<Integer, String> filtered = map.filter(f);

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
    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withInt(a -> withInt(b -> {
            final Integer keyA = a;
            final Integer keyB = b;
            final String valueA = Integer.toString(keyA);
            final String valueB = Integer.toString(keyB);
            final ImmutableMap<Integer, String> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableMap<Integer, String> filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertSame(map, filtered);
            }
            else if (aPassed) {
                Iterator<Map.Entry<Integer, String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final Map.Entry<Integer, String> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<Map.Entry<Integer, String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final Map.Entry<Integer, String> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertSame(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertTrue(filtered.isEmpty());
            }
        })));
    }

    @Test
    @Override
    public void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableMap<Integer, String> map = newBuilder().build();
            assertSame(map, map.filterNot(f));
        });
    }

    @Test
    @Override
    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final String value = Integer.toString(key);
            final ImmutableMap<Integer, String> map = newBuilder().put(key, value).build();
            final ImmutableMap<Integer, String> filtered = map.filterNot(f);

            if (f.apply(value)) {
                assertTrue(filtered.isEmpty());
            }
            else {
                assertSame(map, filtered);
            }
        }));
    }

    @Test
    @Override
    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withInt(a -> withInt(b -> {
            final Integer keyA = a;
            final Integer keyB = b;
            final String valueA = Integer.toString(keyA);
            final String valueB = Integer.toString(keyB);
            final ImmutableMap<Integer, String> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableMap<Integer, String> filtered = map.filterNot(f);

            final boolean aRemoved = f.apply(valueA);
            final boolean bRemoved = f.apply(valueB);

            if (aRemoved && bRemoved) {
                assertTrue(filtered.isEmpty());
            }
            else if (aRemoved) {
                Iterator<Map.Entry<Integer, String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final Map.Entry<Integer, String> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertSame(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<Map.Entry<Integer, String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final Map.Entry<Integer, String> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(map, filtered);
            }
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

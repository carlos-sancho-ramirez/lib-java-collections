package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class MutableIntKeyMapTest implements IntKeyMapTest<String, MutableTransformableBuilder<String>, MutableIntKeyMap.Builder<String>>, MutableTraversableTest<String, MutableTransformableBuilder<String>> {

    @Override
    public MutableIntKeyMap.Builder<String> newMapBuilder() {
        return new MutableIntKeyMap.Builder<>();
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, MutableTransformableBuilder<String>>> procedure) {
        procedure.apply(HashCodeKeyTraversableBuilder::new);
    }

    @Override
    public void withValue(Procedure<String> procedure) {
        withString(procedure);
    }

    private boolean filterFunc(String value) {
        return value != null && !value.isEmpty();
    }

    @Override
    public void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::filterFunc);
    }

    private String reduceFunc(String left, String right) {
        return String.valueOf(left) + '-' + right;
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
    public void withMapBuilderSupplier(Procedure<IntKeyMapBuilderSupplier<String, MutableIntKeyMap.Builder<String>>> procedure) {
        procedure.apply(MutableIntKeyMap.Builder::new);
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

    @Test
    void testToImmutableForEmpty() {
        assertTrue(newMapBuilder().build().toImmutable().isEmpty());
    }

    @Test
    void testMutateForEmpty() {
        final MutableIntKeyMap<String> map1 = new MutableIntKeyMap.Builder<String>().build();
        final MutableIntKeyMap<String> map2 = map1.mutate();

        assertEquals(map1, map2);
        assertNotSame(map1, map2);

        map1.put(1, "");
        assertNull(map2.get(1, null));
    }

    @Test
    void testToImmutable() {
        withInt(a -> withInt(b -> {
            final MutableIntKeyMap.Builder<String> builder = newMapBuilder();
            final MutableIntKeyMap<String> map1 = builder.put(a, "").put(b, "").build();
            final ImmutableIntKeyMap<String> map2 = map1.toImmutable();

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
        }));
    }

    @Test
    void testMutate() {
        final String defValue = "notFound!";
        withInt(a -> withInt(b -> {
            final MutableIntKeyMap.Builder<String> builder = newMapBuilder();
            final MutableIntKeyMap<String> map1 = builder.put(a, "").put(b, "").build();
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
            assertEquals("", map1.get(b, defValue));
            assertEquals(defValue, map2.get(b, defValue));
        }));
    }

    @Test
    void testHashCode() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntKeyMap<String> mutable = newMapBuilder()
                    .put(a, Integer.toString(a))
                    .put(b, Integer.toString(b))
                    .put(c, Integer.toString(c))
                    .build();
            final IntKeyMap<String> immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable.hashCode(), immutable.hashCode());
        })));
    }

    @Test
    void testEquals() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntKeyMap<String> mutable = newMapBuilder()
                    .put(a, Integer.toString(a))
                    .put(b, Integer.toString(b))
                    .put(c, Integer.toString(c))
                    .build();
            final IntKeyMap<String> immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable, immutable);
            assertEquals(immutable, mutable);
        })));
    }

    @Test
    void testDonateWhenEmpty() {
        final MutableIntKeyMap<String> map = newMapBuilder().build();
        final MutableIntKeyMap<String> map2 = map.donate();
        assertTrue(map.isEmpty());
        assertTrue(map2.isEmpty());
        assertNotSame(map, map2);
    }

    @Test
    void testDonateForSingleElement() {
        withInt(key -> {
            final String value = valueFromKey(key);
            final MutableIntKeyMap<String> map = newMapBuilder().put(key, value).build();
            final MutableIntKeyMap<String> map2 = map.donate();
            assertTrue(map.isEmpty());
            assertEquals(1, map2.size());
            assertEquals(key, map2.keyAt(0));
            assertSame(value, map2.valueAt(0));
        });
    }

    @Test
    void testDonateForMultipleElements() {
        withInt(a -> withInt(b -> {
            final String aValue = valueFromKey(a);
            final String bValue = valueFromKey(b);
            final MutableIntKeyMap<String> map = newMapBuilder().put(a, aValue).put(b, bValue).build();
            final MutableIntKeyMap<String> map2 = map.donate();
            assertTrue(map.isEmpty());

            if (equal(a, b)) {
                assertEquals(1, map2.size());
                assertEquals(a, map2.keyAt(0));
                assertSame(aValue, map2.valueAt(0));
            }
            else {
                assertEquals(2, map2.size());
                if (a == map2.keyAt(0)) {
                    assertSame(aValue, map2.valueAt(0));
                    assertEquals(b, map2.keyAt(1));
                    assertSame(bValue, map2.valueAt(1));
                }
                else {
                    assertEquals(b, map2.keyAt(0));
                    assertSame(bValue, map2.valueAt(0));
                    assertEquals(a, map2.keyAt(1));
                    assertSame(aValue, map2.valueAt(1));
                }
            }
        }));
    }

    @Test
    void testPick() {
        withInt(k1 -> withInt(k2 -> {
            final String v1 = valueFromKey(k1);
            if (equal(k1, k2)) {
                final MutableIntKeyMap<String> map = newMapBuilder().put(k1, v1).build();
                assertSame(v1, map.pick(k1));
                assertTrue(map.isEmpty());
            }
            else {
                final String v2 = valueFromKey(k2);
                final MutableIntKeyMap<String> map = newMapBuilder().put(k1, v1).put(k2, v2).build();
                assertSame(v1, map.pick(k1));
                assertEquals(1, map.size());
                assertSame(v2, map.get(k2));
            }
        }));
    }

    @Test
    void testPutAllMethodForMultipleElementsInThisMap() {
        withInt(a -> withInt(b -> {
            final MutableIntKeyMap<String> thisMap = newMapBuilder().build();
            final MutableIntKeyMap<String> thatMap = newMapBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            assertTrue(thisMap.putAll(thatMap));
            assertEquals(thatMap, thisMap);
        }));
    }

    @Test
    void testPutAllMethodForEmptyGivenMap() {
        withInt(a -> withInt(b -> {
            final MutableIntKeyMap<String> thisMap = newMapBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final int size = thisMap.size();
            assertFalse(thisMap.putAll(newMapBuilder().build()));
            assertEquals(size, thisMap.size());
        }));
    }

    @Test
    void testPutAllMethodForMultipleElementsInTheGivenMap() {
        withInt(a -> withInt(b -> withInt(c -> withInt(d -> {
            final MutableIntKeyMap<String> thisMap = newMapBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final MutableIntKeyMap<String> thatMap = newMapBuilder()
                    .put(c, valueFromKey(c))
                    .put(d, valueFromKey(d))
                    .build();

            final MutableIntKeyMap.Builder<String> builder = newMapBuilder();
            for (IntKeyMap.Entry<String> entry : thisMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            for (IntKeyMap.Entry<String> entry : thatMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            final int originalSize = thisMap.size();
            final MutableIntKeyMap<String> expected = builder.build();
            assertEquals(originalSize != expected.size(), thisMap.putAll(thatMap));
            assertEquals(expected, thisMap);
        }))));
    }

    static final class HashCodeKeyTraversableBuilder<E> implements MutableTransformableBuilder<E> {
        private final MutableIntKeyMap<E> map = MutableIntKeyMap.empty();

        @Override
        public HashCodeKeyTraversableBuilder<E> add(E element) {
            map.put(SortUtils.hashCode(element), element);
            return this;
        }

        @Override
        public MutableTransformable<E> build() {
            return map;
        }
    }
}

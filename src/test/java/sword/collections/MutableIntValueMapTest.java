package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sword.collections.SortUtils.equal;

interface MutableIntValueMapTest<K, B extends MutableIntTransformableBuilder> extends IntValueMapTest<K, B>, MutableIntTraversableTest<B> {

    @Override
    MutableIntValueMap.Builder<K> newBuilder();

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
    @Override
    default void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final IntValueMap<K> map = newBuilder().build();
            assertTrue(map.filter(f).isEmpty());
        });
    }

    @Test
    @Override
    default void testFilterForSingleElement() {
        withFilterFunc(f -> withKey(key -> {
            final int value = valueFromKey(key);
            final IntValueMap<K> map = newBuilder().put(key, value).build();
            final IntValueMap<K> filtered = map.filter(f);

            if (f.apply(value)) {
                assertEquals(map, filtered);
            }
            else {
                assertTrue(filtered.isEmpty());
            }
        }));
    }

    @Test
    @Override
    default void testFilterForMultipleElements() {
        withFilterFunc(f -> withKey(keyA -> withKey(keyB -> {
            final int valueA = valueFromKey(keyA);
            final int valueB = valueFromKey(keyB);
            final IntValueMap<K> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final IntValueMap<K> filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                if (!equal(map, filtered)) {
                    fail();
                }
            }
            else if (aPassed) {
                Iterator<IntValueMap.Entry<K>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<K> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntValueMap.Entry<K>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<K> entry = iterator.next();
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
    default void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final IntValueMap<K> map = newBuilder().build();
            assertTrue(map.filterNot(f).isEmpty());
        });
    }

    @Test
    @Override
    default void testFilterNotForSingleElement() {
        withFilterFunc(f -> withKey(key -> {
            final int value = valueFromKey(key);
            final IntValueMap<K> map = newBuilder().put(key, value).build();
            final IntValueMap<K> filtered = map.filterNot(f);

            if (f.apply(value)) {
                assertTrue(filtered.isEmpty());
            }
            else {
                assertEquals(map, filtered);
            }
        }));
    }

    @Test
    default void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withKey(keyA -> withKey(keyB -> {
            final int valueA = valueFromKey(keyA);
            final int valueB = valueFromKey(keyB);
            final IntValueMap<K> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final IntValueMap<K> filtered = map.filterNot(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertTrue(filtered.isEmpty());
            }
            else if (aPassed) {
                Iterator<IntValueMap.Entry<K>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<K> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertSame(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntValueMap.Entry<K>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<K> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertEquals(map, filtered);
            }
        })));
    }

    @Test
    default void testPutAllMethodForMultipleElementsInThisMap() {
        withKey(a -> withKey(b -> {
            final MutableIntValueMap<K> thisMap = newBuilder().build();
            final MutableIntValueMap<K> thatMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            assertTrue(thisMap.putAll(thatMap));
            assertEquals(thatMap, thisMap);
        }));
    }

    @Test
    default void testPutAllMethodForEmptyGivenMap() {
        withKey(a -> withKey(b -> {
            final MutableIntValueMap<K> thisMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final int size = thisMap.size();
            assertFalse(thisMap.putAll(newBuilder().build()));
            assertEquals(size, thisMap.size());
        }));
    }

    @Test
    default void testPutAllMethodForMultipleElementsInTheGivenMap() {
        withKey(a -> withKey(b -> withKey(c -> withKey(d -> {
            final MutableIntValueMap<K> thisMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final MutableIntValueMap<K> thatMap = newBuilder()
                    .put(c, valueFromKey(c))
                    .put(d, valueFromKey(d))
                    .build();

            final MutableIntValueMap.Builder<K> builder = newBuilder();
            for (IntValueMap.Entry<K> entry : thisMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            for (IntValueMap.Entry<K> entry : thatMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            final int originalSize = thisMap.size();
            final MutableIntValueMap<K> expected = builder.build();
            assertEquals(originalSize != expected.size(), thisMap.putAll(thatMap));
            assertEquals(expected, thisMap);
        }))));
    }

    @Test
    default void testDonateWhenEmpty() {
        final MutableIntValueMap<K> map = newBuilder().build();
        final MutableIntValueMap<K> map2 = map.donate();
        assertTrue(map.isEmpty());
        assertTrue(map2.isEmpty());
        assertNotSame(map, map2);
    }

    @Test
    default void testDonateForSingleElement() {
        withKey(key -> {
            final int value = valueFromKey(key);
            final MutableIntValueMap<K> map = newBuilder().put(key, value).build();
            final MutableIntValueMap<K> map2 = map.donate();
            assertTrue(map.isEmpty());
            assertEquals(1, map2.size());
            assertSame(key, map2.keyAt(0));
            assertEquals(value, map2.valueAt(0));
        });
    }

    @Test
    default void testDonateForSingleMultipleElements() {
        withKey(a -> withKey(b -> {
            final int aValue = valueFromKey(a);
            final int bValue = valueFromKey(b);
            final MutableIntValueMap<K> map = newBuilder().put(a, aValue).put(b, bValue).build();
            final MutableIntValueMap<K> map2 = map.donate();
            assertTrue(map.isEmpty());

            if (equal(a, b)) {
                assertEquals(1, map2.size());
                assertSame(a, map2.keyAt(0));
                assertEquals(aValue, map2.valueAt(0));
            }
            else {
                assertEquals(2, map2.size());
                if (a == map2.keyAt(0)) {
                    assertEquals(aValue, map2.valueAt(0));
                    assertSame(b, map2.keyAt(1));
                    assertEquals(bValue, map2.valueAt(1));
                }
                else {
                    assertSame(b, map2.keyAt(0));
                    assertEquals(bValue, map2.valueAt(0));
                    assertSame(a, map2.keyAt(1));
                    assertEquals(aValue, map2.valueAt(1));
                }
            }
        }));
    }

    @Test
    default void testPick() {
        withKey(k1 -> withKey(k2 -> {
            final int v1 = valueFromKey(k1);
            if (k1 == k2) {
                final MutableIntValueMap<K> map = newBuilder().put(k1, v1).build();
                assertEquals(v1, map.pick(k1));
                assertTrue(map.isEmpty());
            }
            else {
                final int v2 = valueFromKey(k2);
                final MutableIntValueMap<K> map = newBuilder().put(k1, v1).put(k2, v2).build();
                assertEquals(v1, map.pick(k1));
                assertEquals(1, map.size());
                assertEquals(v2, map.get(k2));
            }
        }));
    }
}

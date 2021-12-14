package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;

public interface MutableMapTest<K, V, B extends MutableTransformableBuilder<V>> extends MapTest<K, V, B>, MutableTraversableTest<V, B> {

    MutableMap.Builder<K, V> newMapBuilder();
    void withKey(Procedure<K> procedure);
    V valueFromKey(K key);

    @Test
    default void testPutAllMethodForMultipleElementsInThisMap() {
        withKey(a -> withKey(b -> {
            final MutableMap<K, V> thisMap = newMapBuilder().build();
            final MutableMap<K, V> thatMap = newMapBuilder()
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
            final MutableMap<K, V> thisMap = newMapBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final int size = thisMap.size();
            assertFalse(thisMap.putAll(newMapBuilder().build()));
            assertEquals(size, thisMap.size());
        }));
    }

    @Test
    default void testPutAllMethodForMultipleElementsInTheGivenMap() {
        withKey(a -> withKey(b -> withKey(c -> withKey(d -> {
            final MutableMap<K, V> thisMap = newMapBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final MutableMap<K, V> thatMap = newMapBuilder()
                    .put(c, valueFromKey(c))
                    .put(d, valueFromKey(d))
                    .build();

            final MutableMap.Builder<K, V> builder = newMapBuilder();
            for (Map.Entry<K, V> entry : thisMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            for (Map.Entry<K, V> entry : thatMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            final int originalSize = thisMap.size();
            final MutableMap<K, V> expected = builder.build();
            assertEquals(originalSize != expected.size(), thisMap.putAll(thatMap));
            assertEquals(expected, thisMap);
        }))));
    }

    @Test
    default void testDonateWhenEmpty() {
        final MutableMap<K, V> map = newMapBuilder().build();
        final MutableMap<K, V> map2 = map.donate();
        assertTrue(map.isEmpty());
        assertTrue(map2.isEmpty());
    }

    @Test
    default void testDonateForSingleElement() {
        withKey(key -> {
            final V value = valueFromKey(key);
            final MutableMap<K, V> map = newMapBuilder().put(key, value).build();
            final MutableMap<K, V> map2 = map.donate();
            assertTrue(map.isEmpty());
            assertEquals(1, map2.size());
            assertSame(key, map2.keyAt(0));
            assertSame(value, map2.valueAt(0));
        });
    }

    @Test
    default void testDonateForSingleMultipleElements() {
        withKey(a -> withKey(b -> {
            final V aValue = valueFromKey(a);
            final V bValue = valueFromKey(b);
            final MutableMap<K, V> map = newMapBuilder().put(a, aValue).put(b, bValue).build();
            final MutableMap<K, V> map2 = map.donate();
            assertTrue(map.isEmpty());

            if (equal(a, b)) {
                assertEquals(1, map2.size());
                assertSame(a, map2.keyAt(0));
                assertSame(aValue, map2.valueAt(0));
            }
            else {
                assertEquals(2, map2.size());
                if (a == map2.keyAt(0)) {
                    assertSame(aValue, map2.valueAt(0));
                    assertSame(b, map2.keyAt(1));
                    assertSame(bValue, map2.valueAt(1));
                }
                else {
                    assertSame(b, map2.keyAt(0));
                    assertSame(bValue, map2.valueAt(0));
                    assertSame(a, map2.keyAt(1));
                    assertSame(aValue, map2.valueAt(1));
                }
            }
        }));
    }

    @Test
    default void testPick() {
        withKey(k1 -> withKey(k2 -> {
            final V v1 = valueFromKey(k1);
            if (equal(k1, k2)) {
                final MutableMap<K, V> map = newMapBuilder().put(k1, v1).build();
                assertSame(v1, map.pick(k1));
                assertTrue(map.isEmpty());
            }
            else {
                final V v2 = valueFromKey(k2);
                final MutableMap<K, V> map = newMapBuilder().put(k1, v1).put(k2, v2).build();
                assertSame(v1, map.pick(k1));
                assertEquals(1, map.size());
                assertSame(v2, map.get(k2));
            }
        }));
    }
}

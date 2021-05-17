package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;

public interface MutableMapTest<K, V> {

    MutableMap.Builder<K, V> newMapBuilder();
    void withKey(Procedure<K> procedure);
    V valueFromKey(K value);

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

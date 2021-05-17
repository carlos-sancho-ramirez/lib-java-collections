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

abstract class MutableIntValueMapTest<K, B extends MutableIntTransformableBuilder> extends IntValueMapTest<K, B> implements MutableIntTraversableTest<B> {

    @Override
    abstract MutableIntValueMap.Builder<K> newBuilder();
    abstract void withFilterFunc(Procedure<IntPredicate> procedure);

    @Test
    void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final IntValueMap<K> map = newBuilder().build();
            assertTrue(map.filter(f).isEmpty());
        });
    }

    @Test
    void testFilterForSingleElement() {
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
    void testFilterForMultipleElements() {
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
                //assertEquals(map, filtered);
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
    void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final IntValueMap<K> map = newBuilder().build();
            assertTrue(map.filterNot(f).isEmpty());
        });
    }

    @Test
    void testFilterNotForSingleElement() {
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
    void testFilterNotForMultipleElements() {
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
    void testDonateWhenEmpty() {
        final MutableIntValueMap<K> map = newBuilder().build();
        final MutableIntValueMap<K> map2 = map.donate();
        assertTrue(map.isEmpty());
        assertTrue(map2.isEmpty());
        assertNotSame(map, map2);
    }

    @Test
    void testDonateForSingleElement() {
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
    void testDonateForSingleMultipleElements() {
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
}

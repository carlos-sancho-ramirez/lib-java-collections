package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.SortUtils.equal;

abstract class MutableIntValueMapTest<K, B extends MutableIntTransformableBuilder> extends IntValueMapTest<K, B> implements MutableIntTraversableTest<B> {

    @Override
    abstract MutableIntValueMap.Builder<K> newBuilder();
    abstract void withFilterFunc(Procedure<IntPredicate> procedure);

    @Test
    public void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final IntValueMap<K> map = newBuilder().build();
            assertTrue(map.filter(f).isEmpty());
        });
    }

    @Test
    public void testFilterForSingleElement() {
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
    public void testFilterForMultipleElements() {
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
    public void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final IntValueMap<K> map = newBuilder().build();
            assertTrue(map.filterNot(f).isEmpty());
        });
    }

    @Test
    public void testFilterNotForSingleElement() {
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
    public void testFilterNotForMultipleElements() {
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
}

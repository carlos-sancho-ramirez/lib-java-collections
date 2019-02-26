package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;

abstract class MutableIntValueMapTest<T> extends IntValueMapTest<T> {

    @Override
    abstract MutableIntValueMap.Builder<T> newBuilder();
    abstract void withFilterFunc(Procedure<IntPredicate> procedure);

    @Test
    public void testClearWhenEmpty() {
        final MutableIntValueMap<T> collection = newBuilder().build().mutate();
        assertFalse(collection.clear());
        assertTrue(collection.isEmpty());
    }

    @Test
    public void testClearForSingleItem() {
        withInt(value -> {
            final MutableIntValueMap<T> collection = newBuilder()
                    .put(keyFromInt(value), value)
                    .build().mutate();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        });
    }

    @Test
    public void testClearForMultipleItems() {
        withInt(a -> withInt(b -> {
            final MutableIntValueMap<T> collection = newBuilder()
                    .put(keyFromInt(a), a)
                    .put(keyFromInt(b), b)
                    .build().mutate();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        }));
    }

    @Test
    public void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final IntValueMap<T> map = newBuilder().build();
            assertTrue(map.filter(f).isEmpty());
        });
    }

    @Test
    public void testFilterForSingleElement() {
        withFilterFunc(f -> withKey(key -> {
            final int value = valueFromKey(key);
            final IntValueMap<T> map = newBuilder().put(key, value).build();
            final IntValueMap<T> filtered = map.filter(f);

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
            final IntValueMap<T> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final IntValueMap<T> filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                if (!equal(map, filtered)) {
                    fail();
                }
                //assertEquals(map, filtered);
            }
            else if (aPassed) {
                Iterator<IntValueMap.Entry<T>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<T> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertSame(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntValueMap.Entry<T>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<T> entry = iterator.next();
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
            final IntValueMap<T> map = newBuilder().build();
            assertTrue(map.filterNot(f).isEmpty());
        });
    }

    @Test
    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withKey(key -> {
            final int value = valueFromKey(key);
            final IntValueMap<T> map = newBuilder().put(key, value).build();
            final IntValueMap<T> filtered = map.filterNot(f);

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
            final IntValueMap<T> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final IntValueMap<T> filtered = map.filterNot(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertTrue(filtered.isEmpty());
            }
            else if (aPassed) {
                Iterator<IntValueMap.Entry<T>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<T> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertSame(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntValueMap.Entry<T>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<T> entry = iterator.next();
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

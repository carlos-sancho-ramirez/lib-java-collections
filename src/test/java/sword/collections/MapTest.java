package sword.collections;

import junit.framework.TestCase;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class MapTest<K, V> extends TestCase {

    abstract MapBuilder<K, V> newBuilder();
    abstract void withKey(Procedure<K> procedure);
    abstract void withValue(Procedure<V> procedure);
    abstract V getTestValue();
    abstract K keyFromInt(int value);
    abstract V valueFromKey(K key);

    public void testEmptyBuilderBuildsEmptyArray() {
        Map<K, V> array = newBuilder().build();
        assertEquals(0, array.size());
    }

    public void testSize() {
        final V value = getTestValue();
        withKey(a -> withKey(b -> withKey(c -> withKey(d -> {
            Map<K, V> map = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .put(d, value)
                    .build();

            int expectedSize = 1;
            if (!equal(a, b)) {
                expectedSize++;
            }

            if (!equal(b, c) && !equal(a, c)) {
                expectedSize++;
            }

            if (!equal(c, d) && !equal(b, d) && !equal(a, d)) {
                expectedSize++;
            }

            assertEquals(expectedSize, map.size());
        }))));
    }

    public void testGet() {
        final V value = getTestValue();
        withKey(a -> withKey(b -> {
            Map<K, V> array = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .build();

            withKey(other -> {
                final V expectedValue = (equal(other, a) || equal(other, b))? value : null;
                assertEquals(expectedValue, array.get(other, null));
            });
        }));
    }

    public void testKeyAtMethod() {
        withValue(value -> withKey(a -> withKey(b -> withKey(c -> {
            Map<K, V> array = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .build();

            MutableSet<K> keySet = new MutableSet.Builder<K>().add(a).add(b).add(c).build();

            final int size = array.size();
            for (int i = 0; i < size; i++) {
                assertTrue(keySet.remove(array.keyAt(i)));
            }

            assertTrue(keySet.isEmpty());
        }))));
    }

    public void testValueAtMethod() {
        withKey(a -> withKey(b -> withKey(c -> {
            Map<K, V> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int size = map.size();
            for (int i = 0; i < size; i++) {
                final K key = map.keyAt(i);
                assertEquals(valueFromKey(key), map.valueAt(i));
            }
        })));
    }

    public void testKeySet() {
        final V value = getTestValue();
        for (int amount = 0; amount < 3; amount++) {
            final MapBuilder<K, V> mapBuilder = newBuilder();
            final ImmutableSet.Builder<K> setBuilder = new ImmutableSet.Builder<>();
            for (int i = 0; i < amount; i++) {
                final K key = keyFromInt(i);
                setBuilder.add(key);
                mapBuilder.put(key, value);
            }

            final ImmutableSet<K> expectedKeys = setBuilder.build();
            final ImmutableSet<K> keySet = mapBuilder.build().keySet().toImmutable();
            assertEquals(expectedKeys, keySet);
        }
    }

    public void testIndexOfKey() {
        final V value = getTestValue();
        withKey(a -> withKey(b -> withKey(c -> {
            final Map map = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .build();

            assertEquals(a, map.keyAt(map.indexOfKey(a)));
            assertEquals(b, map.keyAt(map.indexOfKey(b)));
            assertEquals(c, map.keyAt(map.indexOfKey(c)));
        })));
    }

    public void testIterator() {
        withKey(a -> withKey(b -> withKey(c -> {
            Map<K, V> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int size = map.size();
            final Iterator<Map.Entry<K, V>> iterator = map.iterator();
            for (int i = 0; i < size; i++) {
                assertTrue(iterator.hasNext());

                final Map.Entry<K, V> entry = iterator.next();
                assertEquals(i, entry.getIndex());
                assertEquals(map.keyAt(i), entry.getKey());
                assertEquals(map.valueAt(i), entry.getValue());
            }

            assertFalse(iterator.hasNext());
        })));
    }

    public void testMutateMethod() {
        withKey(a -> withKey(b -> {
            Map<K, V> map1 = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            MutableMap<K, V> map2 = map1.mutate();

            final Iterator<Map.Entry<K, V>> it1 = map1.iterator();
            final Iterator<Map.Entry<K, V>> it2 = map2.iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());

                final Map.Entry<K, V> entry1 = it1.next();
                final Map.Entry<K, V> entry2 = it2.next();

                assertEquals(entry1.getKey(), entry2.getKey());
                assertEquals(entry1.getValue(), entry2.getValue());
            }
            assertFalse(it2.hasNext());

            map2.remove(b);
            assertTrue(map1.containsKey(b));
            assertFalse(map2.containsKey(b));
        }));
    }
}

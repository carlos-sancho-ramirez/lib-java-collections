package sword.collections;

import junit.framework.TestCase;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class IntValueMapTest extends TestCase {

    abstract IntValueMapBuilder<String> newBuilder();

    private void withInt(IntProcedure procedure) {
        final int[] values = {Integer.MIN_VALUE, -1, 0, 1, 5, Integer.MAX_VALUE};
        for (int value : values) {
            procedure.apply(value);
        }
    }

    private void withString(Procedure<String> procedure) {
        final String[] values = {null, "", " ", "abcd", "0"};
        for (String value : values) {
            procedure.apply(value);
        }
    }

    public void testEmptyBuilderBuildsEmptyArray() {
        assertEquals(0, newBuilder().build().size());
    }

    public void testSize() {
        final int value = 4;
        withString(a -> withString(b -> withString(c -> {
            IntValueMap<String> map = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .build();

            int expectedSize = 1;
            if (!equal(a, b)) {
                expectedSize++;
            }

            if (!equal(b, c) && !equal(a, c)) {
                expectedSize++;
            }

            assertEquals(expectedSize, map.size());
        })));
    }

    public void testGet() {
        final int value = 45;
        final int defValue = 3;
        withString(a -> withString(b -> {
            IntValueMap<String> array = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .build();

            withString(other -> {
                final int expectedValue = (equal(other, a) || equal(other, b))? value : defValue;
                assertEquals(expectedValue, array.get(other, defValue));
            });
        }));
    }

    public void testKeyAtMethod() {
        final int value = 6;
        withString(a -> withString(b -> withString(c -> {
            IntValueMap<String> array = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .build();

            MutableSet<String> keySet = new MutableSet.Builder<String>().add(a).add(b).add(c).build();

            final int size = array.size();
            for (int i = 0; i < size; i++) {
                assertTrue(keySet.remove(array.keyAt(i)));
            }

            assertTrue(keySet.isEmpty());
        })));
    }

    private int valueFromKey(String str) {
        return (str != null)? str.hashCode() : 0;
    }

    private String keyFromInt(int value) {
        return Integer.toString(value);
    }

    public void testValueAtMethod() {
        withString(a -> withString(b -> withString(c -> {
            IntValueMap<String> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int size = map.size();
            for (int i = 0; i < size; i++) {
                final String key = map.keyAt(i);
                assertEquals(valueFromKey(key), map.valueAt(i));
            }
        })));
    }

    public void testKeySet() {
        final int value = 125;
        for (int amount = 0; amount < 3; amount++) {
            final IntValueMapBuilder<String> mapBuilder = newBuilder();
            final ImmutableSet.Builder<String> setBuilder = new ImmutableSet.Builder<>();
            for (int i = 0; i < amount; i++) {
                final String key = keyFromInt(i);
                setBuilder.add(key);
                mapBuilder.put(key, value);
            }

            final ImmutableSet<String> expectedKeys = setBuilder.build();
            final ImmutableSet<String> keySet = mapBuilder.build().keySet().toImmutable();
            assertEquals(expectedKeys, keySet);
        }
    }

    public void testIndexOfKey() {
        final int value = 37;
        withString(a -> withString(b -> withString(c -> {
            final IntValueMap<String> map = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .build();

            assertEquals(a, map.keyAt(map.indexOfKey(a)));
            assertEquals(b, map.keyAt(map.indexOfKey(b)));
            assertEquals(c, map.keyAt(map.indexOfKey(c)));
        })));
    }

    public void testEntryIterator() {
        withString(a -> withString(b -> withString(c -> {
            IntValueMap<String> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int size = map.size();
            final Iterator<IntValueMap.Entry<String>> iterator = map.entries().iterator();
            for (int i = 0; i < size; i++) {
                assertTrue(iterator.hasNext());

                final IntValueMap.Entry<String> entry = iterator.next();
                assertEquals(i, entry.getIndex());
                assertEquals(map.keyAt(i), entry.getKey());
                assertEquals(map.valueAt(i), entry.getValue());
            }

            assertFalse(iterator.hasNext());
        })));
    }

    public void testMutateMethod() {
        withString(a -> withString(b -> {
            IntValueMap<String> map1 = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            MutableIntValueMap<String> map2 = map1.mutate();

            final Iterator<IntValueMap.Entry<String>> it1 = map1.entries().iterator();
            final Iterator<IntValueMap.Entry<String>> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());

                final IntValueMap.Entry<String> entry1 = it1.next();
                final IntValueMap.Entry<String> entry2 = it2.next();

                assertEquals(entry1.getKey(), entry2.getKey());
                assertEquals(entry1.getValue(), entry2.getValue());
            }
            assertFalse(it2.hasNext());

            final ImmutableIntValueMap<String> immutableMap1 = map1.toImmutable();
            assertEquals(immutableMap1, map2.toImmutable());
            map2.removeAt(0);
            assertFalse(immutableMap1.equals(map2.toImmutable()));
        }));
    }
}

package sword.collections;

import junit.framework.TestCase;

import java.util.Iterator;

import static sword.collections.TestUtils.withInt;

abstract class IntPairMapTest extends TestCase {

    abstract IntPairMapBuilder newBuilder();

    public void testEmptyBuilderBuildsEmptyArray() {
        IntPairMap array = newBuilder().build();
        assertEquals(0, array.size());
    }

    public void testSize() {
        final int value = 14;
        withInt(a -> withInt(b -> withInt(c -> withInt(d -> {
            IntPairMap array = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .put(d, value)
                    .build();

            int expectedSize = 1;
            if (b != a) {
                expectedSize++;
            }

            if (c != b && c != a) {
                expectedSize++;
            }

            if (d != c && d != b && d != a) {
                expectedSize++;
            }

            assertEquals(expectedSize, array.size());
        }))));
    }

    public void testGet() {
        final int defValue = -3;
        final int value = 21;
        withInt(a -> withInt(b -> {
            IntPairMap array = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .build();

            withInt(other -> {
                final int expectedValue = (other == a || other == b)? value : defValue;
                assertEquals(expectedValue, array.get(other, defValue));
            });
        }));
    }

    public void testKeyAtMethod() {
        withInt(value -> withInt(a -> withInt(b -> withInt(c -> {
            IntPairMap array = newBuilder()
                    .put(a, value)
                    .put(b, value)
                    .put(c, value)
                    .build();

            int lastKey = array.keyAt(0);
            assertTrue(lastKey == a || lastKey == b || lastKey == c);

            final int size = array.size();
            for (int i = 1; i < size; i++) {
                int newKey = array.keyAt(i);
                assertTrue(newKey > lastKey);

                lastKey = newKey;
                assertTrue(lastKey == a || lastKey == b || lastKey == c);
            }
        }))));
    }

    public void testValueAtMethod() {
        withInt(a -> withInt(b -> withInt(c -> {
            IntPairMap array = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .put(c, c)
                    .build();

            final int size = array.size();
            for (int i = 1; i < size; i++) {
                final int key = array.keyAt(i);
                assertEquals(key, array.valueAt(i));
            }
        })));
    }

    public void testKeySet() {
        for (int amount = 0; amount < 3; amount++) {
            final IntPairMapBuilder mapBuilder = newBuilder();
            final ImmutableIntSetBuilder setBuilder = new ImmutableIntSetBuilder();
            for (int i = 0; i < amount; i++) {
                setBuilder.add(i);
                mapBuilder.put(i, 5);
            }

            final ImmutableIntSet expectedKeys = setBuilder.build();
            final ImmutableIntSet keySet = mapBuilder.build().keySet().toImmutable();
            assertEquals(expectedKeys, keySet);
        }
    }

    public void testIndexOfKey() {
        withInt(a -> withInt(b -> withInt(c -> {
            final int value = 34;
            final IntPairMap map = newBuilder()
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
        withInt(a -> withInt(b -> withInt(c -> {
            IntPairMap array = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .put(c, c)
                    .build();

            final int size = array.size();
            final Iterator<IntPairMap.Entry> iterator = array.iterator();
            for (int i = 0; i < size; i++) {
                assertTrue(iterator.hasNext());

                final IntPairMap.Entry entry = iterator.next();
                assertEquals(i, entry.getIndex());
                assertEquals(array.keyAt(i), entry.getKey());
                assertEquals(array.valueAt(i), entry.getValue());
            }

            assertFalse(iterator.hasNext());
        })));
    }
}

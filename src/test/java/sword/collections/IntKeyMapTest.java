package sword.collections;

import junit.framework.TestCase;

import java.util.Iterator;

import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

abstract class IntKeyMapTest extends TestCase {

    abstract <E> IntKeyMapBuilder<E> newBuilder();

    public void testEmptyBuilderBuildsEmptyArray() {
        IntKeyMapBuilder<String> builder = newBuilder();
        IntKeyMap<String> array = builder.build();
        assertEquals(0, array.size());
    }

    public void testSize() {
        final String value = "value";
        withInt(a -> withInt(b -> withInt(c -> withInt(d -> {
            IntKeyMapBuilder<String> builder = newBuilder();
            IntKeyMap<String> array = builder
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
        final String defValue = "defValue";
        final String value = "value";
        withInt(a -> withInt(b -> {
            IntKeyMapBuilder<String> builder = newBuilder();
            IntKeyMap<String> array = builder
                    .put(a, value)
                    .put(b, value)
                    .build();

            withInt(other -> {
                final String expectedValue = (other == a || other == b)? value : defValue;
                assertEquals(expectedValue, array.get(other, defValue));
            });
        }));
    }

    public void testKeyAtMethod() {
        withString(value -> withInt(a -> withInt(b -> withInt(c -> {
            IntKeyMapBuilder<String> builder = newBuilder();
            IntKeyMap<String> array = builder
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
            IntKeyMapBuilder<String> builder = newBuilder();
            IntKeyMap<String> array = builder
                    .put(a, Integer.toString(a))
                    .put(b, Integer.toString(b))
                    .put(c, Integer.toString(c))
                    .build();

            final int size = array.size();
            for (int i = 1; i < size; i++) {
                final int key = array.keyAt(i);
                assertEquals(Integer.toString(key), array.valueAt(i));
            }
        })));
    }

    public void testKeySetWhenEmpty() {
        final IntKeyMapBuilder<String> builder = newBuilder();
        final IntKeyMap<String> map = builder.build();
        assertTrue(map.keySet().isEmpty());
    }

    public void testKeySet() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntKeyMapBuilder<String> builder = newBuilder();
            final IntKeyMap<String> map = builder
                    .put(a, Integer.toString(a))
                    .put(b, Integer.toString(b))
                    .put(c, Integer.toString(c))
                    .build();

            final ImmutableIntSet set = new ImmutableIntSetBuilder()
                    .add(a).add(b).add(c).build();
            assertEquals(set, map.keySet().toImmutable());
        })));
    }

    public void testIndexOfKey() {
        withInt(a -> withInt(b -> withInt(c -> {
            final Object value = new Object();
            final IntKeyMapBuilder<Object> builder = newBuilder();
            final IntKeyMap map = builder
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
            IntKeyMapBuilder<String> builder = newBuilder();
            IntKeyMap<String> array = builder
                    .put(a, Integer.toString(a))
                    .put(b, Integer.toString(b))
                    .put(c, Integer.toString(c))
                    .build();

            final int size = array.size();
            final Iterator<IntKeyMap.Entry<String>> iterator = array.iterator();
            for (int i = 0; i < size; i++) {
                assertTrue(iterator.hasNext());

                final IntKeyMap.Entry<String> entry = iterator.next();
                assertEquals(i, entry.getIndex());
                assertEquals(array.keyAt(i), entry.getKey());
                assertEquals(array.valueAt(i), entry.getValue());
            }

            assertFalse(iterator.hasNext());
        })));
    }
}

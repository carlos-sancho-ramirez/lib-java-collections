package sword.collections;

import junit.framework.TestCase;

import java.util.Iterator;

import static sword.collections.TestUtils.withInt;

abstract class IntPairMapTest extends TestCase {

    abstract IntPairMapBuilder newBuilder();
    abstract void withFilterFunc(Procedure<IntPredicate> procedure);

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
            for (int i = 2; i < amount + 2; i++) {
                setBuilder.add(i);
                mapBuilder.put(i, 5);
            }

            final ImmutableIntSet expectedKeys = setBuilder.build();
            final ImmutableIntSet keySet = mapBuilder.build().keySet().toImmutable();
            assertEquals(expectedKeys, keySet);
        }
    }

    public void testValueListWhenEmpty() {
        assertTrue(newBuilder().build().valueList().isEmpty());
    }

    public void testValueList() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntPairMap map = newBuilder()
                    .put(a, a * a)
                    .put(b, b * b)
                    .put(c, c * c)
                    .build();

            final ImmutableIntList.Builder listBuilder = new ImmutableIntList.Builder();
            for (int value : map) {
                listBuilder.add(value);
            }

            assertEquals(listBuilder.build(), map.valueList().toImmutable());
        })));
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

    public void testEntryIterator() {
        withInt(a -> withInt(b -> withInt(c -> {
            IntPairMap array = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .put(c, c)
                    .build();

            final int size = array.size();
            final Iterator<IntPairMap.Entry> iterator = array.entries().iterator();
            for (int i = 0; i < size; i++) {
                assertTrue(iterator.hasNext());

                final IntPairMap.Entry entry = iterator.next();
                assertEquals(i, entry.index());
                assertEquals(array.keyAt(i), entry.key());
                assertEquals(array.valueAt(i), entry.value());
            }

            assertFalse(iterator.hasNext());
        })));
    }

    public void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final IntPairMap map = newBuilder().build();
            assertTrue(map.filter(f).isEmpty());
        });
    }

    public void testFilterForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final int value = key * key;
            final IntPairMap map = newBuilder().put(key, value).build();
            final IntPairMap filtered = map.filter(f);

            if (f.apply(value)) {
                assertEquals(map, filtered);
            }
            else {
                assertTrue(filtered.isEmpty());
            }
        }));
    }

    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withInt(keyA -> withInt(keyB -> {
            final int valueA = keyA * keyA;
            final int valueB = keyB * keyB;
            final IntPairMap map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final IntPairMap filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertEquals(map, filtered);
            }
            else if (aPassed) {
                Iterator<IntPairMap.Entry> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntPairMap.Entry entry = iterator.next();
                assertEquals(keyA, entry.key());
                assertEquals(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntPairMap.Entry> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntPairMap.Entry entry = iterator.next();
                assertEquals(keyB, entry.key());
                assertEquals(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertTrue(filtered.isEmpty());
            }
        })));
    }

    public void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final IntPairMap map = newBuilder().build();
            assertTrue(map.filterNot(f).isEmpty());
        });
    }

    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withInt(key -> {
            final int value = key * key;
            final IntPairMap map = newBuilder().put(key, value).build();
            final IntPairMap filtered = map.filterNot(f);

            if (f.apply(value)) {
                assertTrue(filtered.isEmpty());
            }
            else {
                assertEquals(map, filtered);
            }
        }));
    }

    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withInt(keyA -> withInt(keyB -> {
            final int valueA = keyA * keyA;
            final int valueB = keyB * keyB;
            final IntPairMap map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final IntPairMap filtered = map.filterNot(f);

            final boolean aRemoved = f.apply(valueA);
            final boolean bRemoved = f.apply(valueB);

            if (aRemoved && bRemoved) {
                assertTrue(filtered.isEmpty());
            }
            else if (aRemoved) {
                Iterator<IntPairMap.Entry> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntPairMap.Entry entry = iterator.next();
                assertEquals(keyB, entry.key());
                assertEquals(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<IntPairMap.Entry> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntPairMap.Entry entry = iterator.next();
                assertEquals(keyA, entry.key());
                assertEquals(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertEquals(map, filtered);
            }
        })));
    }
}

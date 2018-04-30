package sword.collections;

import java.util.Iterator;

import static sword.collections.TestUtils.withInt;

abstract class IntKeyMapTest<T> extends AbstractIterableTest<T> {

    abstract IntKeyMapBuilder<T> newMapBuilder();
    abstract T getTestValue();
    abstract T getTestValue2();
    abstract T valueForKey(int key);

    private final class IterableBuilderAdapter implements CollectionBuilder<T> {

        private final IntKeyMapBuilder<T> _builder = newMapBuilder();
        private int _key = 0;

        @Override
        public CollectionBuilder<T> add(T element) {
            _builder.put(_key++, element);
            return this;
        }

        @Override
        public IterableCollection<T> build() {
            return _builder.build();
        }
    }

    @Override
    CollectionBuilder<T> newIterableBuilder() {
        return new IterableBuilderAdapter();
    }

    public void testEmptyBuilderBuildsEmptyArray() {
        IntKeyMapBuilder<T> builder = newMapBuilder();
        IntKeyMap<T> array = builder.build();
        assertEquals(0, array.size());
    }

    public void testSize() {
        final T value = getTestValue();
        withInt(a -> withInt(b -> withInt(c -> withInt(d -> {
            IntKeyMapBuilder<T> builder = newMapBuilder();
            IntKeyMap<T> array = builder
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
        final T value = getTestValue();
        final T defValue = getTestValue2();
        withInt(a -> withInt(b -> {
            IntKeyMapBuilder<T> builder = newMapBuilder();
            IntKeyMap<T> array = builder
                    .put(a, value)
                    .put(b, value)
                    .build();

            withInt(other -> {
                final T expectedValue = (other == a || other == b)? value : defValue;
                assertEquals(expectedValue, array.get(other, defValue));
            });
        }));
    }

    public void testKeyAtMethod() {
        final T value = getTestValue();
        withInt(a -> withInt(b -> withInt(c -> {
            IntKeyMapBuilder<T> builder = newMapBuilder();
            IntKeyMap<T> array = builder
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
        })));
    }

    public void testValueAtMethod() {
        withInt(a -> withInt(b -> withInt(c -> {
            IntKeyMapBuilder<T> builder = newMapBuilder();
            IntKeyMap<T> array = builder
                    .put(a, valueForKey(a))
                    .put(b, valueForKey(b))
                    .put(c, valueForKey(c))
                    .build();

            final int size = array.size();
            for (int i = 1; i < size; i++) {
                final int key = array.keyAt(i);
                assertEquals(Integer.toString(key), array.valueAt(i));
            }
        })));
    }

    public void testKeySetWhenEmpty() {
        final IntKeyMapBuilder<T> builder = newMapBuilder();
        final IntKeyMap<T> map = builder.build();
        assertTrue(map.keySet().isEmpty());
    }

    public void testKeySet() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntKeyMapBuilder<T> builder = newMapBuilder();
            final IntKeyMap<T> map = builder
                    .put(a, valueForKey(a))
                    .put(b, valueForKey(b))
                    .put(c, valueForKey(c))
                    .build();

            final ImmutableIntSet set = new ImmutableIntSetBuilder()
                    .add(a).add(b).add(c).build();
            assertEquals(set, map.keySet().toImmutable());
        })));
    }

    public void testIndexOfKey() {
        withInt(a -> withInt(b -> withInt(c -> {
            final T value = getTestValue();
            final IntKeyMapBuilder<T> builder = newMapBuilder();
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

    public void testEntryIterator() {
        withInt(a -> withInt(b -> withInt(c -> {
            IntKeyMapBuilder<T> builder = newMapBuilder();
            IntKeyMap<T> array = builder
                    .put(a, valueForKey(a))
                    .put(b, valueForKey(b))
                    .put(c, valueForKey(c))
                    .build();

            final int size = array.size();
            final Iterator<IntKeyMap.Entry<T>> iterator = array.entries().iterator();
            for (int i = 0; i < size; i++) {
                assertTrue(iterator.hasNext());

                final IntKeyMap.Entry<T> entry = iterator.next();
                assertEquals(i, entry.index());
                assertEquals(array.keyAt(i), entry.key());
                assertEquals(array.valueAt(i), entry.value());
            }

            assertFalse(iterator.hasNext());
        })));
    }
}

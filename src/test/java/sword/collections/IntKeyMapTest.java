package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.TestUtils.withInt;

abstract class IntKeyMapTest<T> extends TransformableTest<T> {

    abstract IntKeyMapBuilder<T> newMapBuilder();
    abstract T getTestValue();
    abstract T getTestValue2();
    abstract T valueFromKey(int key);
    abstract void withMapBuilderSupplier(Procedure<IntKeyMapBuilderSupplier<T, IntKeyMapBuilder<T>>> procedure);
    abstract void withMapToIntFunc(Procedure<IntResultFunction<T>> procedure);

    private void withArbitraryMapBuilderSupplier(Procedure<IntKeyMapBuilderSupplier<T, IntKeyMapBuilder<T>>> procedure) {
        procedure.apply(ImmutableIntKeyMap.Builder::new);
        procedure.apply(MutableIntKeyMap.Builder::new);
    }

    private final class IterableBuilderAdapter implements TransformableBuilder<T> {

        private final IntKeyMapBuilder<T> _builder = newMapBuilder();
        private int _key = 0;

        @Override
        public TransformableBuilder<T> add(T element) {
            _builder.put(_key++, element);
            return this;
        }

        @Override
        public Transformable<T> build() {
            return _builder.build();
        }
    }

    @Override
    TransformableBuilder<T> newIterableBuilder() {
        return new IterableBuilderAdapter();
    }

    @Test
    public void testEmptyBuilderBuildsEmptyArray() {
        IntKeyMapBuilder<T> builder = newMapBuilder();
        IntKeyMap<T> array = builder.build();
        assertEquals(0, array.size());
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testValueAtMethod() {
        withInt(a -> withInt(b -> withInt(c -> {
            IntKeyMapBuilder<T> builder = newMapBuilder();
            IntKeyMap<T> array = builder
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int size = array.size();
            for (int i = 1; i < size; i++) {
                final int key = array.keyAt(i);
                assertEquals(Integer.toString(key), array.valueAt(i));
            }
        })));
    }

    @Test
    public void testKeySetWhenEmpty() {
        final IntKeyMapBuilder<T> builder = newMapBuilder();
        final IntKeyMap<T> map = builder.build();
        assertTrue(map.keySet().isEmpty());
    }

    @Test
    public void testKeySet() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntKeyMapBuilder<T> builder = newMapBuilder();
            final IntKeyMap<T> map = builder
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final ImmutableIntSet set = new ImmutableIntSetBuilder()
                    .add(a).add(b).add(c).build();
            assertEquals(set, map.keySet().toImmutable());
        })));
    }

    @Test
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

    @Test
    public void testEntryIterator() {
        withInt(a -> withInt(b -> withInt(c -> {
            IntKeyMapBuilder<T> builder = newMapBuilder();
            IntKeyMap<T> array = builder
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
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

    @Test
    public void testEqualMapReturnsFalseWhenAPairIsMissing() {
        withInt(a -> withInt(b -> withInt(c -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int mapSize = map.size();
            final IntKeyMapBuilder<T> mapBuilder = supplier.newBuilder();
            for (int i = 1; i < mapSize; i++) {
                mapBuilder.put(map.keyAt(i), map.valueAt(i));
            }
            final IntKeyMap<T> reducedMap = mapBuilder.build();

            assertFalse(map.equalMap(reducedMap));
            assertFalse(reducedMap.equalMap(map));
        }))));
    }

    @Test
    public void testEqualMapReturnsFalseWhenKeyMatchesButNotValues() {
        withInt(a -> withInt(b -> withInt(c -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            final int mapSize = map.size();
            for (int j = 0; j < mapSize; j++) {
                final IntKeyMapBuilder<T> mapBuilder = supplier.newBuilder();
                for (int i = 0; i < mapSize; i++) {
                    T value = (i == j) ? null : map.valueAt(i);
                    mapBuilder.put(map.keyAt(i), value);
                }
                final IntKeyMap<T> modifiedMap = mapBuilder.build();

                assertFalse(map.equalMap(modifiedMap));
                assertFalse(modifiedMap.equalMap(map));
            }
        }))));
    }

    @Test
    public void testEqualMapReturnsTrueForOtherSortingsAndMutabilities() {
        withInt(a -> withInt(b -> withInt(c -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .put(c, valueFromKey(c))
                    .build();

            withArbitraryMapBuilderSupplier(mapSupplier -> {
                final IntKeyMap<T> arbitraryMap = mapSupplier.newBuilder()
                        .put(a, valueFromKey(a))
                        .put(b, valueFromKey(b))
                        .put(c, valueFromKey(c))
                        .build();

                assertTrue(map.equalMap(arbitraryMap));
            });
        }))));
    }

    @Test
    public void testMapResultingKeysForMultipleElements() {
        withMapFunc(f -> withInt(keyA -> withInt(keyB -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder()
                    .put(keyA, valueFromKey(keyA))
                    .put(keyB, valueFromKey(keyB))
                    .build();
            final IntKeyMap<String> mapped = map.map(f);

            final int size = map.size();
            assertEquals(size, mapped.size());

            for (int i = 0; i < size; i++) {
                assertEquals(map.keyAt(i), mapped.keyAt(i));
            }
        }))));
    }

    @Test
    public void testMapToIntForMultipleElements() {
        withMapToIntFunc(f -> withInt(a -> withInt(b -> withMapBuilderSupplier(supplier -> {
            final IntKeyMap<T> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final IntPairMap mapped = map.mapToInt(f);

            final int size = map.size();
            assertEquals(size, mapped.size());

            for (int i = 0; i < size; i++) {
                assertEquals(map.keyAt(i), mapped.keyAt(i));
            }
        }))));
    }
}

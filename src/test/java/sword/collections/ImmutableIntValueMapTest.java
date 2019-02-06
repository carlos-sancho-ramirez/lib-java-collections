package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;

abstract class ImmutableIntValueMapTest<T> extends IntValueMapTest<T> {

    private void withInvertibleArray(Procedure<ImmutableIntValueMap<T>> action) {
        withKey(key1 -> withKey(key2 -> withKey(key3 -> {
            if (!equal(key1, key2) && !equal(key1, key3) && !equal(key2, key3)) {
                withInt(value1 -> withInt(value2 -> withInt(value3 -> {
                    if (value1 != value2 && value1 != value3 && value2 != value3) {
                        final ImmutableIntValueMap<T> invertibleArray = new ImmutableIntValueHashMap.Builder<T>()
                                .put(key1, value1)
                                .put(key2, value2)
                                .put(key3, value3)
                                .build();

                        action.apply(invertibleArray);
                    }
                })));
            }
        })));
    }

    @Override
    abstract ImmutableIntValueMap.Builder<T> newBuilder();

    public void testInverted() {
        withInvertibleArray(invertedArray -> {
            final ImmutableIntKeyMap<T> array = invertedArray.invert();
            assertEquals(invertedArray.size(), array.size());

            for (ImmutableIntValueMap.Entry<T> entry : invertedArray.entries()) {
                assertEquals(entry.key(), array.get(entry.value()));
            }
        });
    }

    public void testKeySet() {
        withInvertibleArray(array -> {
            final ImmutableSet<T> result = array.keySet();

            final ImmutableHashSet.Builder<T> builder = new ImmutableHashSet.Builder<>();
            for (ImmutableIntValueMap.Entry<T> entry : array.entries()) {
                builder.add(entry.key());
            }

            assertEquals(builder.build(), result);
        });
    }

    private boolean valueIsEven(int value) {
        return (value & 1) == 0;
    }

    void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::valueIsEven);
    }

    public void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableIntValueMap<T> map = newBuilder().build();
            assertSame(map, map.filter(f));
        });
    }

    abstract void assertEmpty(ImmutableIntValueMap<T> map);

    public void testFilterForSingleElement() {
        withFilterFunc(f -> withInt(value -> {
            final T key = keyFromInt(value);
            final ImmutableIntValueMap<T> map = newBuilder().put(key, value).build();
            final ImmutableIntValueMap<T> filtered = map.filter(f);

            if (f.apply(value)) {
                assertSame(map, filtered);
            }
            else {
                assertEmpty(filtered);
            }
        }));
    }

    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withInt(valueA -> withInt(valueB -> {
            final T keyA = keyFromInt(valueA);
            final T keyB = keyFromInt(valueB);
            final ImmutableIntValueMap<T> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableIntValueMap<T> filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertSame(map, filtered);
            }
            else if (aPassed) {
                Iterator<IntValueMap.Entry<T>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntValueMap.Entry<T> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertEquals(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntValueMap.Entry<T>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntValueMap.Entry<T> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertEquals(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertEmpty(filtered);
            }
        })));
    }

    public void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableIntValueMap<T> map = newBuilder().build();
            assertSame(map, map.filterNot(f));
        });
    }

    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withInt(value -> {
            final T key = keyFromInt(value);
            final ImmutableIntValueMap<T> map = newBuilder().put(key, value).build();
            final ImmutableIntValueMap<T> filtered = map.filterNot(f);

            if (f.apply(value)) {
                assertEmpty(filtered);
            }
            else {
                assertSame(map, filtered);
            }
        }));
    }

    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withInt(valueA -> withInt(valueB -> {
            final T keyA = keyFromInt(valueA);
            final T keyB = keyFromInt(valueB);
            final ImmutableIntValueMap<T> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableIntValueMap<T> filtered = map.filterNot(f);

            final boolean aRemoved = f.apply(valueA);
            final boolean bRemoved = f.apply(valueB);

            if (aRemoved && bRemoved) {
                assertEmpty(filtered);
            }
            else if (aRemoved) {
                Iterator<IntValueMap.Entry<T>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntValueMap.Entry<T> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertEquals(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<IntValueMap.Entry<T>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntValueMap.Entry<T> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertEquals(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(map, filtered);
            }
        })));
    }

    public void testMapToIntMethod() {
        withInt(a -> withInt(b -> {
            final ImmutableIntValueMap<T> map = newBuilder()
                    .put(keyFromInt(a), a)
                    .put(keyFromInt(b), b)
                    .build();

            final IntToIntFunction mapFunc = value -> value + 3;
            final ImmutableIntValueMap<T> map2 = map.mapToInt(mapFunc);

            final ImmutableSet<T> keySet = map.keySet();
            assertEquals(keySet, map2.keySet());

            for (T key : keySet) {
                assertEquals(mapFunc.apply(map.get(key)), map2.get(key));
            }
        }));
    }

    public void testMapValues() {
        withInt(a -> withInt(b -> {
            final ImmutableIntValueMap<T> map = newBuilder()
                    .put(keyFromInt(a), a)
                    .put(keyFromInt(b), b)
                    .build();

            final IntFunction<T> mapFunc = this::keyFromInt;
            final ImmutableMap<T, T> map2 = map.map(mapFunc);

            final ImmutableSet<T> keySet = map.keySet();
            assertEquals(keySet, map2.keySet());

            for (T key : keySet) {
                assertEquals(mapFunc.apply(map.get(key)), map2.get(key));
            }
        }));
    }
}

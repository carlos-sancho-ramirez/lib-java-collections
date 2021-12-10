package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;

abstract class ImmutableIntValueMapTest<T, B extends ImmutableIntTransformableBuilder> extends IntValueMapTest<T, B>
        implements ImmutableIntTransformableTest<B> {

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

    @Test
    void testInverted() {
        withInvertibleArray(invertedArray -> {
            final ImmutableIntKeyMap<T> array = invertedArray.invert();
            assertEquals(invertedArray.size(), array.size());

            for (ImmutableIntValueMap.Entry<T> entry : invertedArray.entries()) {
                assertEquals(entry.key(), array.get(entry.value()));
            }
        });
    }

    @Test
    void testKeySet() {
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

    @Test
    void testFilterWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableIntValueMap<T> map = newBuilder().build();
            assertSame(map, map.filter(f));
        });
    }

    abstract void assertEmpty(ImmutableIntValueMap<T> map);

    @Test
    void testFilterForSingleElement() {
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

    @Test
    void testFilterForMultipleElements() {
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

    @Test
    void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableIntValueMap<T> map = newBuilder().build();
            assertSame(map, map.filterNot(f));
        });
    }

    @Test
    void testFilterNotForSingleElement() {
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

    @Test
    void testFilterNotForMultipleElements() {
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

    @Test
    void testMapToIntMethod() {
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

    @Test
    void testMapValues() {
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

    @Test
    void testPutMethod() {
        withKey(a -> withKey(b -> withKey(key -> withValue(value -> {
            final ImmutableIntValueMap<T> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final boolean contained = map.containsKey(key);
            final ImmutableIntValueMap<T> newMap = map.put(key, value);

            if (!contained) {
                final ImmutableIntValueMap.Builder<T> builder = newBuilder();
                for (IntValueMap.Entry<T> entry : map.entries()) {
                    builder.put(entry.key(), entry.value());
                }
                assertEquals(builder.put(key, value).build(), newMap);
            }
            else {
                assertSame(map, map.put(key, valueFromKey(key)));

                final ImmutableSet<T> keySet = map.keySet();
                assertEquals(keySet, newMap.keySet());

                for (T k : keySet) {
                    if (equal(k, key)) {
                        assertEquals(value, newMap.get(k));
                    }
                    else {
                        assertEquals(map.get(k), newMap.get(k));
                    }
                }
            }
        }))));
    }

    @Test
    void testPutAllMethodForMultipleElementsInThisMap() {
        withKey(a -> withKey(b -> {
            final ImmutableIntValueMap<T> thisMap = newBuilder().build();
            final ImmutableIntValueMap<T> thatMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            assertEquals(thatMap, thisMap.putAll(thatMap));
        }));
    }

    @Test
    void testPutAllMethodForEmptyGivenMap() {
        withKey(a -> withKey(b -> {
            final ImmutableIntValueMap<T> thisMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            assertSame(thisMap, thisMap.putAll(newBuilder().build()));
        }));
    }

    @Test
    void testPutAllMethodForMultipleElementsInTheGivenMap() {
        withKey(a -> withKey(b -> withKey(c -> withKey(d -> {
            final ImmutableIntValueMap<T> thisMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final ImmutableIntValueMap<T> thatMap = newBuilder()
                    .put(c, valueFromKey(c))
                    .put(d, valueFromKey(d))
                    .build();

            final ImmutableIntValueMap.Builder<T> builder = newBuilder();
            for (IntValueMap.Entry<T> entry : thisMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            for (IntValueMap.Entry<T> entry : thatMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            assertEquals(builder.build(), thisMap.putAll(thatMap));
        }))));
    }
}

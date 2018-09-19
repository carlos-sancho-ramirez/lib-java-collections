package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class ImmutableIntValueMapTest extends IntValueMapTest {

    private void withReversableArray(Procedure<ImmutableIntValueMap<String>> action) {
        withString(key1 -> withString(key2 -> withString(key3 -> {
            if (!equal(key1, key2) && !equal(key1, key3) && !equal(key2, key3)) {
                withInt(value1 -> withInt(value2 -> withInt(value3 -> {
                    if (value1 != value2 && value1 != value3 && value2 != value3) {
                        final ImmutableIntValueMap<String> reversableArray = new ImmutableIntValueMap.Builder<String>()
                                .put(key1, value1)
                                .put(key2, value2)
                                .put(key3, value3)
                                .build();

                        action.apply(reversableArray);
                    }
                })));
            }
        })));
    }

    @Override
    ImmutableIntValueMap.Builder<String> newBuilder() {
        return new ImmutableIntValueMap.Builder<>();
    }

    public void testEmptyBuilderBuildsEmptyArray() {
        final ImmutableIntValueMap<String> array = new ImmutableIntValueMap.Builder<String>()
                .build();

        assertEquals(0, array.size());
        assertFalse(array.iterator().hasNext());
    }

    public void testBuilderWithSingleElementBuildsExpectedArray() {
        withString(key -> withInt(value -> {
            final ImmutableIntValueMap<String> array = new ImmutableIntValueMap.Builder<String>()
                    .put(key, value)
                    .build();

            assertEquals(1, array.size());

            final Iterator<ImmutableIntValueMap.Entry<String>> iterator = array.entries().iterator();
            assertTrue(iterator.hasNext());

            final ImmutableIntValueMap.Entry<String> entry = iterator.next();
            assertFalse(iterator.hasNext());

            assertEquals(key, entry.key());
            assertEquals(value, entry.value());

            assertEquals(value, array.get(key));
        }));
    }

    public void testReversed() {
        withReversableArray(reversedArray -> {
            final ImmutableIntKeyMap<String> array = reversedArray.reverse();
            assertEquals(reversedArray.size(), array.size());

            for (ImmutableIntValueMap.Entry<String> entry : reversedArray.entries()) {
                assertEquals(entry.key(), array.get(entry.value()));
            }
        });
    }

    public void testKeySetWhenEmpty() {
        final ImmutableIntValueMap<String> empty = ImmutableIntValueMap.empty();
        assertSame(ImmutableHashSet.empty(), empty.keySet());
    }

    public void testKeySet() {
        withReversableArray(array -> {
            final ImmutableHashSet<String> result = array.keySet();

            final ImmutableHashSet.Builder<String> builder = new ImmutableHashSet.Builder<>();
            for (ImmutableIntValueMap.Entry<String> entry : array.entries()) {
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
            final ImmutableIntValueMap<String> map = newBuilder().build();
            assertSame(map, map.filter(f));
        });
    }

    public void testFilterForSingleElement() {
        withFilterFunc(f -> withInt(value -> {
            final String key = Integer.toString(value);
            final ImmutableIntValueMap<String> map = newBuilder().put(key, value).build();
            final ImmutableIntValueMap<String> filtered = map.filter(f);

            final ImmutableIntValueMap<String> expected = f.apply(value)? map : newBuilder().build();
            assertSame(expected, filtered);
        }));
    }

    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withInt(valueA -> withInt(valueB -> {
            final String keyA = Integer.toString(valueA);
            final String keyB = Integer.toString(valueB);
            final ImmutableIntValueMap<String> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableIntValueMap<String> filtered = map.filter(f);

            final boolean aPassed = f.apply(valueA);
            final boolean bPassed = f.apply(valueB);

            if (aPassed && bPassed) {
                assertSame(map, filtered);
            }
            else if (aPassed) {
                Iterator<IntValueMap.Entry<String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntValueMap.Entry<String> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertEquals(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<IntValueMap.Entry<String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntValueMap.Entry<String> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertEquals(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(newBuilder().build(), filtered);
            }
        })));
    }

    public void testFilterNotWhenEmpty() {
        withFilterFunc(f -> {
            final ImmutableIntValueMap<String> map = newBuilder().build();
            assertSame(map, map.filterNot(f));
        });
    }

    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withInt(value -> {
            final String key = Integer.toString(value);
            final ImmutableIntValueMap<String> map = newBuilder().put(key, value).build();
            final ImmutableIntValueMap<String> filtered = map.filterNot(f);

            final ImmutableIntValueMap<String> expected = f.apply(value)? newBuilder().build() : map;
            assertSame(expected, filtered);
        }));
    }

    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withInt(valueA -> withInt(valueB -> {
            final String keyA = Integer.toString(valueA);
            final String keyB = Integer.toString(valueB);
            final ImmutableIntValueMap<String> map = newBuilder().put(keyA, valueA).put(keyB, valueB).build();
            final ImmutableIntValueMap<String> filtered = map.filterNot(f);

            final boolean aRemoved = f.apply(valueA);
            final boolean bRemoved = f.apply(valueB);

            if (aRemoved && bRemoved) {
                assertSame(newBuilder().build(), filtered);
            }
            else if (aRemoved) {
                Iterator<IntValueMap.Entry<String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntValueMap.Entry<String> entry = iterator.next();
                assertSame(keyB, entry.key());
                assertEquals(valueB, entry.value());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<IntValueMap.Entry<String>> iterator = filtered.entries().iterator();
                assertTrue(iterator.hasNext());
                final IntValueMap.Entry<String> entry = iterator.next();
                assertSame(keyA, entry.key());
                assertEquals(valueA, entry.value());
                assertFalse(iterator.hasNext());
            }
            else {
                assertSame(map, filtered);
            }
        })));
    }

    public void testMapValuesForIntResult() {
        withInt(a -> withInt(b -> {
            final ImmutableIntValueMap<String> map = new ImmutableIntValueMap.Builder<String>()
                    .put(Integer.toString(a), a)
                    .put(Integer.toString(b), b)
                    .build();

            final IntToIntFunction mapFunc = value -> value + 3;
            final ImmutableIntValueMap<String> map2 = map.map(mapFunc);

            final ImmutableHashSet<String> keySet = map.keySet();
            assertEquals(keySet, map2.keySet());

            for (String key : keySet) {
                assertEquals(mapFunc.apply(map.get(key)), map2.get(key));
            }
        }));
    }

    public void testMapValues() {
        withInt(a -> withInt(b -> {
            final ImmutableIntValueMap<Integer> map = new ImmutableIntValueMap.Builder<Integer>()
                    .put(a, a)
                    .put(b, b)
                    .build();

            final IntFunction<String> mapFunc = Integer::toString;
            final ImmutableMap<Integer, String> map2 = map.map(mapFunc);

            final ImmutableHashSet<Integer> keySet = map.keySet();
            assertEquals(keySet, map2.keySet());

            for (Integer key : keySet) {
                assertEquals(mapFunc.apply(map.get(key)), map2.get(key));
            }
        }));
    }
}

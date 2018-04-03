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
    IntValueMapBuilder<String> newBuilder() {
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

            final Iterator<ImmutableIntValueMap.Entry<String>> iterator = array.iterator();
            assertTrue(iterator.hasNext());

            final ImmutableIntValueMap.Entry<String> entry = iterator.next();
            assertFalse(iterator.hasNext());

            assertEquals(key, entry.getKey());
            assertEquals(value, entry.getValue());

            assertEquals(value, array.get(key));
        }));
    }

    public void testReversed() {
        withReversableArray(reversedArray -> {
            final ImmutableIntKeyMap<String> array = reversedArray.reverse();
            assertEquals(reversedArray.size(), array.size());

            for (ImmutableIntValueMap.Entry<String> entry : reversedArray) {
                assertEquals(entry.getKey(), array.get(entry.getValue()));
            }
        });
    }

    public void testKeySetWhenEmpty() {
        final ImmutableIntValueMap<String> empty = ImmutableIntValueMap.empty();
        assertSame(ImmutableSet.empty(), empty.keySet());
    }

    public void testKeySet() {
        withReversableArray(array -> {
            final ImmutableSet<String> result = array.keySet();

            final ImmutableSet.Builder<String> builder = new ImmutableSet.Builder<>();
            for (ImmutableIntValueMap.Entry<String> entry : array) {
                builder.add(entry.getKey());
            }

            assertEquals(builder.build(), result);
        });
    }

    public void testMapValuesForIntResult() {
        withInt(a -> withInt(b -> {
            final ImmutableIntValueMap<String> map = new ImmutableIntValueMap.Builder<String>()
                    .put(Integer.toString(a), a)
                    .put(Integer.toString(b), b)
                    .build();

            final IntToIntFunction mapFunc = value -> value + 3;
            final ImmutableIntValueMap<String> map2 = map.mapValues(mapFunc);

            final ImmutableSet<String> keySet = map.keySet();
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
            final ImmutableMap<Integer, String> map2 = map.mapValues(mapFunc);

            final ImmutableSet<Integer> keySet = map.keySet();
            assertEquals(keySet, map2.keySet());

            for (Integer key : keySet) {
                assertEquals(mapFunc.apply(map.get(key)), map2.get(key));
            }
        }));
    }
}

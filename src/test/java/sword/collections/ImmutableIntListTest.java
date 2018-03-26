package sword.collections;

import java.util.Iterator;

import static sword.collections.TestUtils.withInt;

public class ImmutableIntListTest extends AbstractImmutableIntIterableTest {

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE, -1023, -2, -1, 0, 1, 2, 7, 108, Integer.MAX_VALUE
    };

    @Override
    void withItem(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    @Override
    void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    private boolean isPositiveValue(int value) {
        return value >= 0;
    }

    @Override
    void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::isPositiveValue);
    }

    @Override
    ImmutableIntList emptyCollection() {
        return ImmutableIntList.empty();
    }

    @Override
    ImmutableList<String> mapTargetEmptyCollection() {
        return ImmutableList.empty();
    }

    @Override
    ImmutableIntList.Builder newIntBuilder() {
        return new ImmutableIntList.Builder();
    }

    public void testSizeForTwoElements() {
        withItem(a -> withItem(b -> {
            final ImmutableIntList list = newIntBuilder().add(a).add(b).build();
            final int size = list.size();
            if (size != 2) {
                fail("Expected size 2 after building it adding values " + a + " and " + b +
                        ". But it was " + size);
            }
        }));
    }

    public void testIteratingForMultipleElements() {
        withItem(a -> withItem(b -> {
            final ImmutableIntList list = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> iterator = list.iterator();

            assertTrue(iterator.hasNext());
            assertEquals(a, iterator.next().intValue());

            assertTrue(iterator.hasNext());
            assertEquals(b, iterator.next().intValue());

            assertFalse(iterator.hasNext());
        }));
    }

    public void testMapForMultipleElements() {
        withMapFunc(f -> withItem(a -> withItem(b -> {
            final ImmutableIntList collection = newIntBuilder().add(a).add(b).build();
            final ImmutableList<String> mapped = collection.map(f);
            final Iterator<String> iterator = mapped.iterator();

            for (int item : collection) {
                assertTrue(iterator.hasNext());
                assertEquals(f.apply(item), iterator.next());
            }
            assertFalse(iterator.hasNext());
        })));
    }

    public void testFindFirstWhenEmpty() {
        withFilterFunc(f -> withItem(defaultValue -> {
            final ImmutableIntList list = newIntBuilder().build();
            assertEquals(defaultValue, list.findFirst(f, defaultValue));
        }));
    }

    public void testFindFirstForSingleElement() {
        withFilterFunc(f -> withItem(defaultValue -> withItem(value -> {
            final ImmutableIntList list = newIntBuilder().append(value).build();
            final int first = list.findFirst(f, defaultValue);

            if (f.apply(value)) {
                assertEquals(value, first);
            }
            else {
                assertEquals(defaultValue, first);
            }
        })));
    }

    public void testFindFirstForMultipleElements() {
        withFilterFunc(f -> withItem(defaultValue -> withItem(a -> withItem(b -> {
            final ImmutableIntList list = newIntBuilder().append(a).append(b).build();
            final int first = list.findFirst(f, defaultValue);

            if (f.apply(a)) {
                assertEquals(a, first);
            }
            else if (f.apply(b)) {
                assertEquals(b, first);
            }
            else {
                assertEquals(defaultValue, first);
            }
        }))));
    }

    public void testReduceFor1Item() {
        withInt(value ->  {
            final ImmutableIntList list = new ImmutableIntList.Builder()
                    .add(value)
                    .build();

            final ReduceIntFunction func = (l,r) -> {
                fail("Should not be called for a single item");
                return l;
            };

            assertEquals(list.get(0), list.reduce(func));
        });
    }

    public void testReduceFor2Items() {
        withInt(a -> withInt(b -> {
            final ImmutableIntList list = new ImmutableIntList.Builder()
                    .add(a)
                    .add(b)
                    .build();

            final ReduceIntFunction func = (l,r) -> l + r;
            assertEquals(func.apply(a, b), list.reduce(func));
        }));
    }

    public void testReduceFor3Items() {
        withInt(a -> withInt(b -> withInt(c -> {
            final ImmutableIntList list = new ImmutableIntList.Builder()
                    .add(a)
                    .add(b)
                    .add(c)
                    .build();

            final ReduceIntFunction func = (l,r) -> l + r;
            final int result = list.reduce(func);
            assertEquals(func.apply(func.apply(a, b), c), result);
        })));
    }

    public void testAppendWhenEmpty() {
        withItem(value -> {
            final ImmutableIntList empty = ImmutableIntList.empty();
            final ImmutableIntList list = empty.append(value);
            assertNotSame(empty, list);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    public void testAppendForASingleElement() {
        withItem(a -> withItem(value -> {
            final ImmutableIntList initList = new ImmutableIntList.Builder().append(a).build();
            final ImmutableIntList list = initList.append(value);
            assertEquals(2, list.size());
            assertEquals(a, list.get(0));
            assertEquals(value, list.get(1));
        }));
    }

    public void testAppendAllWhenBothEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        final ImmutableList<String> result = empty.appendAll(empty);
        assertSame(empty, result);
    }

    public void testAppendANonEmptyListWhenEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        withItem(value -> {
            final ImmutableIntList list = new ImmutableIntList.Builder().append(value).build();
            final ImmutableIntList result = empty.appendAll(list);
            assertSame(list, result);
        });
    }

    public void testAppendAnEmptyListWhenNoEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        withItem(value -> {
            final ImmutableIntList list = new ImmutableIntList.Builder().append(value).build();
            final ImmutableIntList result = list.appendAll(empty);
            assertSame(list, result);
        });
    }

    public void testAppendAll() {
        withItem(a -> withItem(b -> withItem(c -> {
            final ImmutableIntList list1 = new ImmutableIntList.Builder().append(a).append(b).build();
            final ImmutableIntList list2 = new ImmutableIntList.Builder().append(c).build();

            final ImmutableIntList result12 = list1.appendAll(list2);
            assertEquals(3, result12.size());
            assertEquals(a, result12.get(0));
            assertEquals(b, result12.get(1));
            assertEquals(c, result12.get(2));

            final ImmutableIntList result21 = list2.appendAll(list1);
            assertEquals(3, result21.size());
            assertEquals(c, result21.get(0));
            assertEquals(a, result21.get(1));
            assertEquals(b, result21.get(2));
        })));
    }
}

package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

public class ImmutableListTest extends AbstractIterableImmutableTest<String> {

    private static final String[] STRING_VALUES = {
            null, "", "_", "0", "abcd"
    };

    private void withString(Procedure<String> procedure) {
        for (String str : STRING_VALUES) {
            procedure.apply(str);
        }
    }

    @Override
    void withItem(Procedure<String> procedure) {
        for (String str : STRING_VALUES) {
            procedure.apply(str);
        }
    }

    private String prefixUnderscore(String value) {
        return "_" + value;
    }

    private String charCounter(String value) {
        final int length = (value != null)? value.length() : 0;
        return Integer.toString(length);
    }

    @Override
    void withMapFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(this::prefixUnderscore);
        procedure.apply(this::charCounter);
    }

    private boolean filterFunc(String value) {
        return value != null && !value.isEmpty();
    }

    @Override
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::filterFunc);
    }

    @Override
    <E> ImmutableList<E> emptyCollection() {
        return ImmutableList.empty();
    }

    @Override
    ImmutableList.Builder<String> newBuilder() {
        return new ImmutableList.Builder<String>();
    }

    public void testSizeForTwoElements() {
        withItem(a -> withItem(b -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).build();
            final int size = list.size();
            if (size != 2) {
                fail("Expected size 2 after building it adding values " + a + " and " + b +
                        ". But it was " + size);
            }
        }));
    }

    public void testIteratingForMultipleElements() {
        withItem(a -> withItem(b -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).build();
            final Iterator<String> iterator = list.iterator();

            assertTrue(iterator.hasNext());
            assertEquals(a, iterator.next());

            assertTrue(iterator.hasNext());
            assertEquals(b, iterator.next());

            assertFalse(iterator.hasNext());
        }));
    }

    public void testMapForMultipleElements() {
        withMapFunc(f -> withItem(a -> withItem(b -> {
            final ImmutableList<String> collection = newBuilder().add(a).add(b).build();
            final ImmutableList<String> mapped = collection.map(f);
            final Iterator<String> iterator = mapped.iterator();

            for (String item : collection) {
                assertTrue(iterator.hasNext());
                assertEquals(f.apply(item), iterator.next());
            }
            assertFalse(iterator.hasNext());
        })));
    }

    public void testIndexOfWhenEmpty() {
        withItem(value -> {
            assertEquals(-1, emptyCollection().indexOf(value));
        });
    }

    public void testIndexOfForSingleElement() {
        withItem(a -> withItem(value -> {
            final ImmutableList<String> list = newBuilder().add(a).build();
            final int index = list.indexOf(value);

            if (equal(a, value)) {
                assertEquals(0, index);
            }
            else {
                assertEquals(-1, index);
            }
        }));
    }

    public void testIndexOfForMultipleElements() {
        withItem(a -> withItem(b -> withItem(value -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).build();
            final int index = list.indexOf(value);

            if (equal(a, value)) {
                assertEquals(0, index);
            }
            else if (equal(b, value)) {
                assertEquals(1, index);
            }
            else {
                assertEquals(-1, index);
            }
        })));
    }

    public void testFindFirstWhenEmpty() {
        withFilterFunc(f -> withString(defaultValue -> {
            final ImmutableList<String> list = newBuilder().build();
            assertEquals(defaultValue, list.findFirst(f, defaultValue));
        }));
    }

    public void testFindFirstForSingleElement() {
        withFilterFunc(f -> withString(defaultValue -> withString(value -> {
            final ImmutableList<String> list = newBuilder().append(value).build();
            final String first = list.findFirst(f, defaultValue);

            if (f.apply(value)) {
                assertSame(value, first);
            }
            else {
                assertSame(defaultValue, first);
            }
        })));
    }

    public void testFindFirstForMultipleElements() {
        withFilterFunc(f -> withString(defaultValue -> withString(a -> withString(b -> {
            final ImmutableList<String> list = newBuilder().append(a).append(b).build();
            final String first = list.findFirst(f, defaultValue);

            if (f.apply(a)) {
                assertSame(a, first);
            }
            else if (f.apply(b)) {
                assertSame(b, first);
            }
            else {
                assertSame(defaultValue, first);
            }
        }))));
    }

    public void testSkip() {
        withFilterFunc(f -> withItem(a -> withItem(b -> withItem(c -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).add(c).build();

            assertSame(list, list.skip(0));
            assertEquals(newBuilder().add(b).add(c).build(), list.skip(1));
            assertEquals(newBuilder().add(c).build(), list.skip(2));

            final ImmutableList<String> emptyList = ImmutableList.empty();
            assertSame(emptyList, list.skip(3));
            assertSame(emptyList, list.skip(4));
            assertSame(emptyList, list.skip(24));
        }))));
    }

    public void testSpan() {
        withFilterFunc(f -> withItem(a -> withItem(b -> withItem(c -> {
            final ImmutableList<String> list = newBuilder().add(a).add(b).add(c).build();
            final ImmutableList<String> filtered = list.filter(f);
            final ImmutableList<String> filteredNot = list.filterNot(f);

            ImmutablePair<ImmutableList<String>, ImmutableList<String>> pair = list.span(f);
            assertEquals(filtered, pair.left);
            assertEquals(filteredNot, pair.right);

            if (filtered.isEmpty()) {
                assertSame(ImmutableList.empty(), pair.left);
                assertSame(list, pair.right);
            }

            if (filteredNot.isEmpty()) {
                assertSame(list, pair.left);
                assertSame(ImmutableList.empty(), pair.right);
            }
        }))));
    }

    public void testAppendWhenEmpty() {
        withString(value -> {
            final ImmutableList<String> empty = ImmutableList.empty();
            final ImmutableList<String> list = empty.append(value);
            assertNotSame(empty, list);
            assertEquals(1, list.size());
            assertSame(value, list.get(0));
        });
    }

    public void testAppendForASingleElement() {
        withString(a -> withString(value -> {
            final ImmutableList<String> initList = new ImmutableList.Builder<String>().append(a).build();
            final ImmutableList<String> list = initList.append(value);
            assertEquals(2, list.size());
            assertSame(a, list.get(0));
            assertSame(value, list.get(1));
        }));
    }

    public void testAppendAllWhenBothEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        final ImmutableList<String> result = empty.appendAll(empty);
        assertSame(empty, result);
    }

    public void testPrependWhenEmpty() {
        withString(value -> {
            final ImmutableList<String> empty = ImmutableList.empty();
            final ImmutableList<String> list = empty.prepend(value);
            assertNotSame(empty, list);
            assertEquals(1, list.size());
            assertSame(value, list.get(0));
        });
    }

    public void testPrependForASingleElement() {
        withString(a -> withString(value -> {
            final ImmutableList<String> initList = new ImmutableList.Builder<String>().append(a).build();
            final ImmutableList<String> list = initList.prepend(value);
            assertEquals(2, list.size());
            assertSame(value, list.get(0));
            assertSame(a, list.get(1));
        }));
    }

    public void testAppendANonEmptyListWhenEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        withString(value -> {
            final ImmutableList<String> list = new ImmutableList.Builder<String>().append(value).build();
            final ImmutableList<String> result = empty.appendAll(list);
            assertSame(list, result);
        });
    }

    public void testAppendAnEmptyListWhenNoEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        withString(value -> {
            final ImmutableList<String> list = new ImmutableList.Builder<String>().append(value).build();
            final ImmutableList<String> result = list.appendAll(empty);
            assertSame(list, result);
        });
    }

    public void testAppendAll() {
        withString(a -> withString(b -> withString(c -> {
            final ImmutableList<String> list1 = new ImmutableList.Builder<String>().append(a).append(b).build();
            final ImmutableList<String> list2 = new ImmutableList.Builder<String>().append(c).build();

            final ImmutableList<String> result12 = list1.appendAll(list2);
            assertEquals(3, result12.size());
            assertEquals(a, result12.get(0));
            assertEquals(b, result12.get(1));
            assertEquals(c, result12.get(2));

            final ImmutableList<String> result21 = list2.appendAll(list1);
            assertEquals(3, result21.size());
            assertEquals(c, result21.get(0));
            assertEquals(a, result21.get(1));
            assertEquals(b, result21.get(2));
        })));
    }
}

package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

public final class MutableListTest extends AbstractIterableTest<String> {

    private static final String[] stringValues = {
            null, "", "_", "0", "abcd"
    };

    private void withString(Procedure<String> procedure) {
        for (String str : stringValues) {
            procedure.apply(str);
        }
    }

    @Override
    void withItem(Procedure<String> procedure) {
        for (String str : stringValues) {
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
    MutableList.Builder<String> newBuilder() {
        return new MutableList.Builder<>();
    }

    public void testSizeForTwoElements() {
        withItem(a -> withItem(b -> {
            final MutableList<String> list = newBuilder().add(a).add(b).build();
            final int size = list.size();
            if (size != 2) {
                fail("Expected size 2 after building it adding values " + a + " and " + b +
                        ". But it was " + size);
            }
        }));
    }

    public void testIteratingForMultipleElements() {
        withItem(a -> withItem(b -> {
            final MutableList<String> list = newBuilder().add(a).add(b).build();
            final Iterator<String> iterator = list.iterator();

            assertTrue(iterator.hasNext());
            assertEquals(a, iterator.next());

            assertTrue(iterator.hasNext());
            assertEquals(b, iterator.next());

            assertFalse(iterator.hasNext());
        }));
    }

    public void testIndexOfWhenEmpty() {
        withItem(value -> {
            assertEquals(-1, emptyCollection().indexOf(value));
        });
    }

    public void testIndexOfForSingleElement() {
        withItem(a -> withItem(value -> {
            final MutableList<String> list = newBuilder().add(a).build();
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
            final MutableList<String> list = newBuilder().add(a).add(b).build();
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
            final MutableList<String> list = newBuilder().build();
            assertEquals(defaultValue, list.findFirst(f, defaultValue));
        }));
    }

    public void testFindFirstForSingleElement() {
        withFilterFunc(f -> withString(defaultValue -> withString(value -> {
            final MutableList<String> list = newBuilder().add(value).build();
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
            final MutableList<String> list = newBuilder().add(a).add(b).build();
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

    public void testAppendWhenEmpty() {
        withString(value -> {
            final MutableList<String> list = MutableList.empty();
            list.append(value);
            assertEquals(1, list.size());
            assertSame(value, list.get(0));
        });
    }

    public void testAppendForASingleElement() {
        withString(a -> withString(value -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).build();
            list.append(value);
            assertEquals(2, list.size());
            assertSame(a, list.get(0));
            assertSame(value, list.get(1));
        }));
    }

    public void testAppendAllWhenBothEmpty() {
        final MutableList<String> list = MutableList.empty();
        list.appendAll(MutableList.empty());
        assertTrue(list.isEmpty());
    }

    public void testAppendANonEmptyListWhenEmpty() {
        withString(value -> {
            final MutableList<String> list = MutableList.empty();
            final MutableList<String> nonEmpty = new MutableList.Builder<String>().add(value).build();
            list.appendAll(nonEmpty);
            assertEquals(list, nonEmpty);
        });
    }

    public void testAppendAnEmptyListWhenNoEmpty() {
        final MutableList<String> empty = MutableList.empty();
        withString(value -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(value).build();
            list.appendAll(empty);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    public void testAppendAllA() {
        withString(a -> withString(b -> withString(c -> {
            final MutableList<String> list1 = new MutableList.Builder<String>().add(a).add(b).build();
            final MutableList<String> list2 = new MutableList.Builder<String>().add(c).build();

            list1.appendAll(list2);
            assertEquals(3, list1.size());
            assertEquals(a, list1.get(0));
            assertEquals(b, list1.get(1));
            assertEquals(c, list1.get(2));
        })));
    }

    public void testAppendAllB() {
        withString(a -> withString(b -> withString(c -> {
            final MutableList<String> list1 = new MutableList.Builder<String>().add(a).build();
            final MutableList<String> list2 = new MutableList.Builder<String>().add(b).add(c).build();

            list1.appendAll(list2);
            assertEquals(3, list1.size());
            assertEquals(a, list1.get(0));
            assertEquals(b, list1.get(1));
            assertEquals(c, list1.get(2));
        })));
    }
}

package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.SortUtils.equal;

public final class MutableListTest extends AbstractTransformableTest<String> {

    private static final String[] stringValues = {
            null, "", "_", "0", "abcd"
    };

    private void withString(Procedure<String> procedure) {
        for (String str : stringValues) {
            procedure.apply(str);
        }
    }

    @Override
    void withValue(Procedure<String> procedure) {
        for (String str : stringValues) {
            procedure.apply(str);
        }
    }

    private String reduceFunc(String left, String right) {
        return String.valueOf(left) + '-' + String.valueOf(right);
    }

    @Override
    void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
        procedure.apply(this::reduceFunc);
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
    void assertEmptyCollection(Transformable<String> collection) {
        assertFalse(collection.iterator().hasNext());
    }

    @Override
    void assertNotChanged(Object expected, Object given) {
        assertEquals(expected, given);
    }

    private boolean sortAlphabetically(String a, String b) {
        if (b == null) {
            return false;
        }

        if (a == null) {
            return true;
        }

        final int aLength = a.length();
        final int bLength = b.length();
        for (int i = 0; i < aLength; i++) {
            if (bLength == i) {
                return false;
            }

            final char charA = a.charAt(i);
            final char charB = b.charAt(i);
            if (charA < charB) {
                return true;
            }
            else if (charA > charB) {
                return false;
            }
        }

        return bLength > aLength;
    }

    MutableList.Builder<String> newBuilder() {
        return new MutableList.Builder<>();
    }

    @Override
    MutableList.Builder<String> newIterableBuilder() {
        return newBuilder();
    }

    @Test
    public void testSizeForTwoElements() {
        withValue(a -> withValue(b -> {
            final MutableList<String> list = newBuilder().add(a).add(b).build();
            final int size = list.size();
            if (size != 2) {
                fail("Expected size 2 after building it adding values " + a + " and " + b +
                        ". But it was " + size);
            }
        }));
    }

    @Test
    public void testIteratingForMultipleElements() {
        withValue(a -> withValue(b -> {
            final MutableList<String> list = newBuilder().add(a).add(b).build();
            final Iterator<String> iterator = list.iterator();

            assertTrue(iterator.hasNext());
            assertEquals(a, iterator.next());

            assertTrue(iterator.hasNext());
            assertEquals(b, iterator.next());

            assertFalse(iterator.hasNext());
        }));
    }

    @Test
    public void testIndexOfWhenEmpty() {
        withValue(value -> {
            assertEquals(-1, newBuilder().build().indexOf(value));
        });
    }

    @Test
    public void testIndexOfForSingleElement() {
        withValue(a -> withValue(value -> {
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

    @Test
    public void testFindFirstWhenEmpty() {
        withFilterFunc(f -> withString(defaultValue -> {
            final MutableList<String> list = newBuilder().build();
            assertEquals(defaultValue, list.findFirst(f, defaultValue));
        }));
    }

    @Test
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

    @Test
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

    @Test
    public void testAppendWhenEmpty() {
        withString(value -> {
            final MutableList<String> list = MutableList.empty();
            list.append(value);
            assertEquals(1, list.size());
            assertSame(value, list.get(0));
        });
    }

    @Test
    public void testAppendForASingleElement() {
        withString(a -> withString(value -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).build();
            list.append(value);
            assertEquals(2, list.size());
            assertSame(a, list.get(0));
            assertSame(value, list.get(1));
        }));
    }

    @Test
    public void testAppendAllWhenBothEmpty() {
        final MutableList<String> list = MutableList.empty();
        list.appendAll(MutableList.empty());
        assertTrue(list.isEmpty());
    }

    @Test
    public void testAppendANonEmptyListWhenEmpty() {
        withString(value -> {
            final MutableList<String> list = MutableList.empty();
            final MutableList<String> nonEmpty = new MutableList.Builder<String>().add(value).build();
            list.appendAll(nonEmpty);
            assertEquals(list, nonEmpty);
        });
    }

    @Test
    public void testAppendAnEmptyListWhenNoEmpty() {
        final MutableList<String> empty = MutableList.empty();
        withString(value -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(value).build();
            list.appendAll(empty);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    @Test
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

    @Test
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

    @Test
    public void testRemoveThroughIterator() {
        final Predicate<String> predicate = str -> str != null && str.length() > 0;
        withString(a -> withString(b -> withString(c -> withString(d -> withString(e -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).add(b).add(c).add(d).add(e).build();
            final MutableList.Builder<String> builder = new MutableList.Builder<>();
            for (String str : list) {
                if (predicate.apply(str)) {
                    builder.add(str);
                }
            }

            final Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                if (!predicate.apply(it.next())) {
                    it.remove();
                }
            }

            assertEquals(builder.build(), list);
        })))));
    }

    @Test
    public void testInsertForEmptyList() {
        withString(a -> {
            final MutableList<String> list = MutableList.empty();
            list.insert(0, a);
            assertEquals(1, list.size());
            assertEquals(a, list.get(0));
        });
    }

    @Test
    public void testInsertFirstForSingleElementList() {
        withString(a -> withString(b -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).build();
            list.insert(0, b);
            assertEquals(2, list.size());
            assertEquals(b, list.get(0));
            assertEquals(a, list.get(1));
        }));
    }

    @Test
    public void testInsertLastForSingleElementList() {
        withString(a -> withString(b -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).build();
            list.insert(1, b);
            assertEquals(2, list.size());
            assertEquals(a, list.get(0));
            assertEquals(b, list.get(1));
        }));
    }

    @Test
    public void testInsert() {
        withString(a -> withString(b -> withString(c -> {
            for (int i = 0; i <= 4; i++) {
                MutableList<String> list = new MutableList.Builder<String>().add(a).add(b).add(b).add(a).build();

                MutableList.Builder<String> builder = new MutableList.Builder<>();
                for (int j = 0; j < 4; j++) {
                    if (j == i) {
                        builder.add(c);
                    }
                    builder.add(list.get(j));
                }

                if (i == 4) {
                    builder.add(c);
                }

                list.insert(i, c);
                assertEquals(builder.build(), list);
            }
        })));
    }

    @Test
    public void testRemoveAtForSingleElementList() {
        withString(a -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).build();
            list.removeAt(0);
            assertTrue(list.isEmpty());
        });
    }

    @Test
    public void testRemoveFirstFor2ElementsList() {
        withString(a -> withString(b -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).add(b).build();
            list.removeAt(0);
            assertEquals(1, list.size());
            assertEquals(b, list.get(0));
        }));
    }

    @Test
    public void testRemoveLastFor2ElementsList() {
        withString(a -> withString(b -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).add(b).build();
            list.removeAt(1);
            assertEquals(1, list.size());
            assertEquals(a, list.get(0));
        }));
    }

    @Test
    public void testRemoveAt() {
        withString(a -> withString(b -> {
            for (int i = 0; i <= 4; i++) {
                MutableList<String> list = new MutableList.Builder<String>().add(a).add(b).add(a).add(b).add(a).build();

                MutableList.Builder<String> builder = new MutableList.Builder<>();
                for (int j = 0; j <= 4; j++) {
                    if (j != i) {
                        builder.add(list.get(j));
                    }
                }

                list.removeAt(i);
                assertEquals(builder.build(), list);
            }
        }));
    }

    @Test
    public void testPut() {
        withString(a -> withString(b -> withString(c -> {
            for (int i = 0; i < 2; i++) {
                final MutableList<String> list = new MutableList.Builder<String>().add(a).add(b).build();
                final MutableList<String> expectedList = new MutableList.Builder<String>()
                        .add((i == 0) ? c : a)
                        .add((i == 1) ? c : b)
                        .build();
                if (i == 0 && equal(a, c) || i == 1 && equal(b, c)) {
                    assertFalse(list.put(i, c));
                } else {
                    assertTrue(list.put(i, c));
                }

                assertEquals(expectedList, list);
            }
        })));
    }

    @Test
    public void testToSetWhenEmpty() {
        final List<String> list = newBuilder().build();
        assertTrue(list.toSet().isEmpty());
    }

    @Test
    public void testToSetWithSingleElement() {
        withValue(a -> {
            final List<String> list = newBuilder().add(a).build();
            final Set<String> set = list.toSet();
            assertEquals(1, set.size());
            assertEquals(a, set.valueAt(0));
        });
    }

    @Test
    public void testToSetWithTwoElements() {
        withValue(a -> withValue(b -> {
            final List<String> list = newBuilder().add(a).add(b).build();
            final Set<String> set = list.toSet();

            if (equal(a, b)) {
                assertEquals(1, set.size());
                assertEquals(a, set.valueAt(0));
            }
            else {
                assertEquals(2, set.size());
                if (equal(a, set.valueAt(0))) {
                    assertEquals(b, set.valueAt(1));
                }
                else {
                    assertEquals(a, set.valueAt(1));
                    assertEquals(b, set.valueAt(0));
                }
            }
        }));
    }

    @Test
    public void testSort() {
        withValue(a -> withValue(b -> withValue(c -> {
            final MutableList<String> list = newBuilder().add(a).add(b).add(c).build();
            final boolean changed = list.sort(this::sortAlphabetically);

            if (sortAlphabetically(b, a)) {
                if (sortAlphabetically(c, b)) {
                    assertEquals(c, list.valueAt(0));
                    assertEquals(b, list.valueAt(1));
                    assertEquals(a, list.valueAt(2));
                    assertTrue(changed);
                }
                else if (sortAlphabetically(c, a)) {
                    assertEquals(b, list.valueAt(0));
                    assertEquals(c, list.valueAt(1));
                    assertEquals(a, list.valueAt(2));
                    assertTrue(changed);
                }
                else {
                    assertEquals(b, list.valueAt(0));
                    assertEquals(a, list.valueAt(1));
                    assertEquals(c, list.valueAt(2));
                    assertTrue(changed);
                }
            }
            else {
                if (sortAlphabetically(c, a)) {
                    assertEquals(c, list.valueAt(0));
                    assertEquals(a, list.valueAt(1));
                    assertEquals(b, list.valueAt(2));
                    assertTrue(changed);
                }
                else if (sortAlphabetically(c, b)) {
                    assertEquals(a, list.valueAt(0));
                    assertEquals(c, list.valueAt(1));
                    assertEquals(b, list.valueAt(2));
                    assertTrue(changed);
                }
                else {
                    assertEquals(a, list.valueAt(0));
                    assertEquals(b, list.valueAt(1));
                    assertEquals(c, list.valueAt(2));
                    assertFalse(changed);
                }
            }
        })));
    }

    @Test
    public void testClearWhenEmpty() {
        final MutableList<String> collection = newBuilder().build();
        assertFalse(collection.clear());
        assertTrue(collection.isEmpty());
    }

    @Test
    public void testClearForSingleItem() {
        withValue(value -> {
            final MutableList<String> collection = newBuilder().add(value).build();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        });
    }

    @Test
    public void testClearForMultipleItems() {
        withValue(a -> withValue(b -> {
            final MutableList<String> collection = newBuilder().add(a).add(b).build();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        }));
    }

    @Test
    public void testMapWhenEmpty() {
        withMapFunc(f -> {
            final MutableList<String> list = newBuilder().build();
            final List<String> mapped = list.map(f);
            assertTrue(mapped.isEmpty());

            withValue(value -> {
                list.clear();
                list.append(value);

                assertEquals(1, mapped.size());
                assertEquals(f.apply(value), mapped.valueAt(0));
            });

            list.clear();
            assertTrue(mapped.isEmpty());
        });
    }

    @Test
    public void testMapForSingleElement() {
        withMapFunc(f -> withValue(value -> {
            final MutableList<String> collection = newBuilder().add(value).build();
            final List<String> mapped = collection.map(f);
            final Iterator<String> iterator = mapped.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next());
            assertFalse(iterator.hasNext());

            collection.removeAt(0);
            assertTrue(mapped.isEmpty());
        }));
    }

    @Test
    public void testMapForMultipleElements() {
        withMapFunc(f -> withValue(a -> withValue(b -> {
            final MutableList<String> collection = newIterableBuilder().add(a).add(b).build();
            final List<String> mapped = collection.map(f);

            final Iterator<String> collectionIterator = collection.iterator();
            final Iterator<String> mappedIterator = mapped.iterator();
            while (collectionIterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(collectionIterator.next()), mappedIterator.next());
            }

            assertFalse(mappedIterator.hasNext());
        })));
    }
}

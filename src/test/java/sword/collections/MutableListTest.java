package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sword.collections.SortUtils.equal;

public final class MutableListTest extends ListTest<String, MutableList.Builder<String>> implements MutableTraversableTest<String, MutableList.Builder<String>> {

    private static final String[] stringValues = {
            null, "", "_", "0", "abcd"
    };

    private void withString(Procedure<String> procedure) {
        for (String str : stringValues) {
            procedure.apply(str);
        }
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, MutableList.Builder<String>>> procedure) {
        procedure.apply(MutableList.Builder::new);
    }

    @Override
    public void withValue(Procedure<String> procedure) {
        for (String str : stringValues) {
            procedure.apply(str);
        }
    }

    private String reduceFunc(String left, String right) {
        return String.valueOf(left) + '-' + String.valueOf(right);
    }

    @Override
    public void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
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
    public void withMapFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(this::prefixUnderscore);
        procedure.apply(this::charCounter);
    }

    @Override
    public void withMapToIntFunc(Procedure<IntResultFunction<String>> procedure) {
        procedure.apply(SortUtils::hashCode);
    }

    private boolean filterFunc(String value) {
        return value != null && !value.isEmpty();
    }

    @Override
    public void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::filterFunc);
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

    private boolean sortByLength(String a, String b) {
        return b != null && (a == null || a.length() < b.length());
    }

    void withSortFunc(Procedure<SortFunction<String>> procedure) {
        procedure.apply(this::sortAlphabetically);
        procedure.apply(this::sortByLength);
    }

    MutableList.Builder<String> newBuilder() {
        return new MutableList.Builder<>();
    }

    @Test
    void testSizeForTwoElements() {
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
    void testIteratingForMultipleElements() {
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
    void testAppendWhenEmpty() {
        withString(value -> {
            final MutableList<String> list = MutableList.empty();
            list.append(value);
            assertEquals(1, list.size());
            assertSame(value, list.get(0));
        });
    }

    @Test
    void testAppendForASingleElement() {
        withString(a -> withString(value -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).build();
            list.append(value);
            assertEquals(2, list.size());
            assertSame(a, list.get(0));
            assertSame(value, list.get(1));
        }));
    }

    @Test
    void testAppendAllWhenBothEmpty() {
        final MutableList<String> list = MutableList.empty();
        list.appendAll(MutableList.empty());
        assertTrue(list.isEmpty());
    }

    @Test
    void testAppendANonEmptyListWhenEmpty() {
        withString(value -> {
            final MutableList<String> list = MutableList.empty();
            final MutableList<String> nonEmpty = new MutableList.Builder<String>().add(value).build();
            list.appendAll(nonEmpty);
            assertEquals(list, nonEmpty);
        });
    }

    @Test
    void testAppendAnEmptyListWhenNoEmpty() {
        final MutableList<String> empty = MutableList.empty();
        withString(value -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(value).build();
            list.appendAll(empty);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    @Test
    void testAppendAllA() {
        withString(a -> withString(b -> withString(c -> {
            final MutableList<String> list1 = new MutableList.Builder<String>().add(a).add(b).build();
            final MutableIntKeyMap<String> list2 = new MutableIntKeyMap.Builder<String>().put(1, c).build();

            list1.appendAll(list2);
            assertEquals(3, list1.size());
            assertEquals(a, list1.get(0));
            assertEquals(b, list1.get(1));
            assertEquals(c, list1.get(2));
        })));
    }

    @Test
    void testAppendAllB() {
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
    void testRemoveThroughIterator() {
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
    void testInsertForEmptyList() {
        withString(a -> {
            final MutableList<String> list = MutableList.empty();
            list.insert(0, a);
            assertEquals(1, list.size());
            assertEquals(a, list.get(0));
        });
    }

    @Test
    void testInsertFirstForSingleElementList() {
        withString(a -> withString(b -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).build();
            list.insert(0, b);
            assertEquals(2, list.size());
            assertEquals(b, list.get(0));
            assertEquals(a, list.get(1));
        }));
    }

    @Test
    void testInsertLastForSingleElementList() {
        withString(a -> withString(b -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).build();
            list.insert(1, b);
            assertEquals(2, list.size());
            assertEquals(a, list.get(0));
            assertEquals(b, list.get(1));
        }));
    }

    @Test
    void testInsert() {
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
    void testPrependWhenEmpty() {
        withValue(value -> {
            final MutableList<String> list = newBuilder().build();
            list.prepend(value);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    @Test
    void testPrependForASingleElement() {
        withValue(a -> withValue(value -> {
            final MutableList<String> list = newBuilder().append(a).build();
            list.prepend(value);
            assertEquals(2, list.size());
            assertEquals(value, list.get(0));
            assertEquals(a, list.get(1));
        }));
    }

    @Test
    void testPrependForMultipleElements() {
        withValue(a -> withValue(b -> withValue(value -> {
            final MutableList<String> list = newBuilder().append(a).append(b).build();
            list.prepend(value);
            assertEquals(3, list.size());
            assertEquals(value, list.get(0));
            assertEquals(a, list.get(1));
            assertEquals(b, list.get(2));
        })));
    }

    @Test
    void testRemoveAtForSingleElementList() {
        withString(a -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).build();
            list.removeAt(0);
            assertTrue(list.isEmpty());
        });
    }

    @Test
    void testRemoveFirstFor2ElementsList() {
        withString(a -> withString(b -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).add(b).build();
            list.removeAt(0);
            assertEquals(1, list.size());
            assertEquals(b, list.get(0));
        }));
    }

    @Test
    void testRemoveLastFor2ElementsList() {
        withString(a -> withString(b -> {
            final MutableList<String> list = new MutableList.Builder<String>().add(a).add(b).build();
            list.removeAt(1);
            assertEquals(1, list.size());
            assertEquals(a, list.get(0));
        }));
    }

    @Test
    void testRemoveAt() {
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
    void testPut() {
        withString(a -> withString(b -> withString(c -> {
            for (int i = 0; i < 2; i++) {
                final MutableList<String> list = new MutableList.Builder<String>().add(a).add(b).build();
                final MutableList<String> expectedList = new MutableList.Builder<String>()
                        .add((i == 0) ? c : a)
                        .add((i == 1) ? c : b)
                        .build();
                if (i == 0 && equal(a, c) || i == 1 && equal(b, c)) {
                    assertFalse(list.put(i, c));
                }
                else {
                    assertTrue(list.put(i, c));
                }

                assertEquals(expectedList, list);
            }
        })));
    }

    @Test
    void testArrange() {
        withValue(a -> withValue(b -> withValue(c -> {
            final MutableList<String> list = newBuilder().add(a).add(b).add(c).build();
            final boolean changed = list.arrange(this::sortAlphabetically);

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
    void testMapWhenEmpty() {
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
    void testMapForSingleElement() {
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
    void testMapForMultipleElements() {
        withMapFunc(f -> withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final MutableList<String> collection = supplier.newBuilder().add(a).add(b).build();
            final List<String> mapped = collection.map(f);

            final Iterator<String> collectionIterator = collection.iterator();
            final Iterator<String> mappedIterator = mapped.iterator();
            while (collectionIterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(collectionIterator.next()), mappedIterator.next());
            }

            assertFalse(mappedIterator.hasNext());
        }))));
    }

    @Test
    void testSortWhenEmpty() {
        final SortFunction<String> sortFunc = (a, b) -> fail("This function should not be called");
        assertTrue(newBuilder().build().sort(sortFunc).isEmpty());
    }

    @Test
    void testSortForASingleElementAppendedBeforeCreation() {
        withValue(value -> withSortFunc(sortFunc -> {
            final MutableList<String> original = newBuilder().add(value).build();
            final List<String> list = original.sort(sortFunc);
            assertEquals(1, list.size());
            assertSame(value, list.valueAt(0));
        }));
    }

    @Test
    void testSortForASingleElementAppendedAfterCreation() {
        withValue(value -> withSortFunc(sortFunc -> {
            final MutableList<String> original = newBuilder().build();
            final List<String> list = original.sort(sortFunc);
            assertTrue(list.isEmpty());

            original.append(value);
            assertEquals(1, list.size());
            assertSame(value, list.valueAt(0));
        }));
    }

    @Test
    void testSort() {
        withValue(a -> withValue(b -> withValue(c -> withSortFunc(sortFunc -> {
            final MutableList<String> original = newBuilder().build();
            final List<String> list = original.sort(sortFunc);
            original.append(a);
            original.append(b);
            original.append(c);

            assertEquals(3, list.size());
            if (sortFunc.lessThan(b, a)) {
                if (sortFunc.lessThan(c, b)) {
                    assertEquals(c, list.valueAt(0));
                    assertEquals(b, list.valueAt(1));
                    assertEquals(a, list.valueAt(2));
                }
                else if (sortFunc.lessThan(c, a)) {
                    assertEquals(b, list.valueAt(0));
                    assertEquals(c, list.valueAt(1));
                    assertEquals(a, list.valueAt(2));
                }
                else {
                    assertEquals(b, list.valueAt(0));
                    assertEquals(a, list.valueAt(1));
                    assertEquals(c, list.valueAt(2));
                }
            }
            else {
                if (sortFunc.lessThan(c, a)) {
                    assertEquals(c, list.valueAt(0));
                    assertEquals(a, list.valueAt(1));
                    assertEquals(b, list.valueAt(2));
                }
                else if (sortFunc.lessThan(c, b)) {
                    assertEquals(a, list.valueAt(0));
                    assertEquals(c, list.valueAt(1));
                    assertEquals(b, list.valueAt(2));
                }
                else {
                    assertEquals(a, list.valueAt(0));
                    assertEquals(b, list.valueAt(1));
                    assertEquals(c, list.valueAt(2));
                }
            }
        }))));
    }

    @Test
    void testDonateWhenEmpty() {
        final MutableList<String> list = newBuilder().build();
        final MutableList<String> list2 = list.donate();
        assertTrue(list.isEmpty());
        assertTrue(list2.isEmpty());
        assertNotSame(list, list2);
    }

    @Test
    void testDonateForSingleElement() {
        withValue(value -> {
            final MutableList<String> list = newBuilder().add(value).build();
            final MutableList<String> list2 = list.donate();
            assertTrue(list.isEmpty());
            assertEquals(1, list2.size());
            assertSame(value, list2.valueAt(0));
        });
    }

    @Test
    void testDonateForSingleMultipleElements() {
        withValue(a -> withValue(b -> {
            final MutableList<String> list = newBuilder().add(a).add(b).build();
            final MutableList<String> list2 = list.donate();
            assertTrue(list.isEmpty());

            assertEquals(2, list2.size());
            assertSame(a, list2.valueAt(0));
            assertSame(b, list2.valueAt(1));
        }));
    }
}

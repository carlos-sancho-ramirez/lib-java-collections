package sword.collections;

import junit.framework.TestCase;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class AbstractIterableTest<T> extends TestCase {

    abstract <E> AbstractIterable<E> emptyCollection();
    abstract CollectionBuilder<T> newBuilder();
    abstract void withItem(Procedure<T> procedure);
    abstract void withFilterFunc(Procedure<Predicate<T>> procedure);
    abstract void withMapFunc(Procedure<Function<T, String>> procedure);

    public void testSizeForNoElements() {
        final AbstractIterable<T> iterable = (AbstractIterable<T>) newBuilder().build();
        assertEquals("Expected size 0 after building an empty list", 0, iterable.size());
    }

    public void testSizeForOneElement() {
        withItem(value -> {
            final AbstractIterable<T> iterable = (AbstractIterable<T>) newBuilder().add(value).build();
            assertEquals("Expected size 1 after building it adding a single value " + value, 1, iterable.size());
        });
    }

    public void testIsEmptyForNoElements() {
        final AbstractIterable<T> list = (AbstractIterable<T>) newBuilder().build();
        assertTrue(list.isEmpty());
    }

    public void testIsEmptyForASingleElement() {
        withItem(value -> {
            final AbstractIterable<T> iterable = (AbstractIterable<T>) newBuilder().add(value).build();
            assertFalse("isEmpty is expected to return false when iterable includes " + value, iterable.isEmpty());
        });
    }

    public void testIteratingForEmptyList() {
        final IterableCollection<T> collection = newBuilder().build();
        assertFalse("Expected an empty iterator for an empty collection", collection.iterator().hasNext());
    }

    public void testIteratingForASingleElement() {
        withItem(value -> {
            final IterableCollection<T> list = newBuilder().add(value).build();
            final Iterator<T> iterator = list.iterator();
            assertTrue("Expected true in hasNext for no empty iterators", iterator.hasNext());
            assertEquals(value, iterator.next());
            assertFalse("Expected false in hasNext while all elements loaded. Failing for value " + value, iterator.hasNext());
        });
    }

    public void testContainsForEmptyList() {
        withItem(value -> {
            final IterableCollection<T> list = newBuilder().build();
            if (list.contains(value)) {
                fail("contains method is expected to return false always for any empty set. " +
                        "But returned true for " + value);
            }
        });
    }

    public void testContainsForListContainingASingleElement() {
        withItem(valueIncluded -> {
            final IterableCollection<T> list = newBuilder().add(valueIncluded).build();
            withItem(otherValue -> {
                if (equal(valueIncluded, otherValue) && !list.contains(otherValue)) {
                    fail("contains method is expected to return true when containing the value. But failing for value " + otherValue);
                }
                else if (!equal(valueIncluded, otherValue) && list.contains(otherValue)) {
                    fail("contains method is expected to return false when no containing the value. But failing for value " + otherValue + " while only containing " + valueIncluded);
                }
            });
        });
    }

    public void testContainsForListContainingMultipleElements() {
        withItem(a -> withItem(b -> {
            final IterableCollection<T> list = newBuilder().add(a).add(b).build();
            withItem(value -> {
                if ((equal(a, value) || equal(b, value)) && !list.contains(value)) {
                    fail("contains method is expected to return true when containing the value. But failing for value " + value + " while containing " + a + " and " + b);
                }
                else if (!equal(a, value) && !equal(b, value) && list.contains(value)) {
                    fail("contains method is expected to return false when no containing the value. But failing for value " + value + " while containing " + a + " and " + b);
                }
            });
        }));
    }

    public void testAnyMatchWhenEmpty() {
        final AbstractIterable<T> iterable = emptyCollection();
        withFilterFunc(f -> assertFalse(iterable.anyMatch(f)));
    }

    public void testAnyMatchForSingleElement() {
        withItem(value -> {
            final IterableCollection<T> iterable = newBuilder().add(value).build();
            withFilterFunc(f -> {
                if (f.apply(value)) {
                    assertTrue(iterable.anyMatch(f));
                }
                else {
                    assertFalse(iterable.anyMatch(f));
                }
            });
        });
    }

    public void testAnyMatchForMultipleElements() {
        withItem(a -> withItem(b -> {
            final IterableCollection<T> iterable = newBuilder().add(a).add(b).build();
            withFilterFunc(f -> {
                if (f.apply(a) || f.apply(b)) {
                    assertTrue(iterable.anyMatch(f));
                }
                else {
                    assertFalse(iterable.anyMatch(f));
                }
            });
        }));
    }
}

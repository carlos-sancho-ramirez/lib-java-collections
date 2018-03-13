package sword.collections;

import junit.framework.TestCase;

import java.util.Iterator;

abstract class AbstractIntIterableTest extends TestCase {

    abstract AbstractIntIterable emptyCollection();
    abstract AbstractIterable<String> mapTargetEmptyCollection();

    abstract IntCollectionBuilder newIntBuilder();
    abstract void withItem(IntProcedure procedure);
    abstract void withFilterFunc(Procedure<IntPredicate> procedure);
    abstract void withMapFunc(Procedure<IntFunction<String>> procedure);

    public void testSizeForNoElements() {
        final AbstractIntIterable iterable = (AbstractIntIterable) newIntBuilder().build();
        assertEquals("Expected size 0 after building an empty list", 0, iterable.size());
    }

    public void testSizeForOneElement() {
        withItem(value -> {
            final AbstractIntIterable iterable = (AbstractIntIterable) newIntBuilder().add(value).build();
            assertEquals("Expected size 1 after building it adding a single value " + value, 1, iterable.size());
        });
    }

    public void testIsEmptyForNoElements() {
        final AbstractIntIterable list = (AbstractIntIterable) newIntBuilder().build();
        assertTrue(list.isEmpty());
    }

    public void testIsEmptyForASingleElement() {
        withItem(value -> {
            final AbstractIntIterable iterable = (AbstractIntIterable) newIntBuilder().add(value).build();
            assertFalse("isEmpty is expected to return false when iterable includes " + value, iterable.isEmpty());
        });
    }

    public void testIteratingForEmptyList() {
        final IterableIntCollection collection = newIntBuilder().build();
        assertFalse("Expected an empty iterator for an empty collection", collection.iterator().hasNext());
    }

    public void testIteratingForASingleElement() {
        withItem(value -> {
            final IterableIntCollection list = newIntBuilder().add(value).build();
            final Iterator<Integer> iterator = list.iterator();
            assertTrue("Expected true in hasNext for no empty iterators", iterator.hasNext());
            assertEquals(value, iterator.next().intValue());
            assertFalse("Expected false in hasNext while all elements loaded. Failing for value " + value, iterator.hasNext());
        });
    }

    public void testContainsWhenEmpty() {
        withItem(value -> {
            final IterableIntCollection list = newIntBuilder().build();
            if (list.contains(value)) {
                fail("contains method is expected to return false always for any empty set. " +
                        "But returned true for " + value);
            }
        });
    }

    public void testContainsWhenContainingASingleElement() {
        withItem(valueIncluded -> {
            final IterableIntCollection list = newIntBuilder().add(valueIncluded).build();
            withItem(otherValue -> {
                if (valueIncluded == otherValue && !list.contains(otherValue)) {
                    fail("contains method is expected to return true when containing the value. But failing for value " + otherValue);
                }
                else if (valueIncluded != otherValue && list.contains(otherValue)) {
                    fail("contains method is expected to return false when no containing the value. But failing for value " + otherValue + " while only containing " + valueIncluded);
                }
            });
        });
    }

    public void testContainsWhenContainingMultipleElements() {
        withItem(a -> withItem(b -> {
            final IterableIntCollection list = newIntBuilder().add(a).add(b).build();
            withItem(value -> {
                if ((a == value || b == value) && !list.contains(value)) {
                    fail("contains method is expected to return true when containing the value. But failing for value " + value + " while containing " + a + " and " + b);
                }
                else if (a != value && b != value && list.contains(value)) {
                    fail("contains method is expected to return false when no containing the value. But failing for value " + value + " while containing " + a + " and " + b);
                }
            });
        }));
    }
}

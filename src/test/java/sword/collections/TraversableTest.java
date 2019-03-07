package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.SortUtils.equal;

abstract class TraversableTest<T> {

    abstract TraversableBuilder<T> newIterableBuilder();
    abstract void withValue(Procedure<T> procedure);
    abstract void withFilterFunc(Procedure<Predicate<T>> procedure);
    abstract void withReduceFunction(Procedure<ReduceFunction<T>> procedure);
    abstract void withMapFunc(Procedure<Function<T, String>> procedure);

    @Test
    public void testSizeForNoElements() {
        final AbstractTraversable<T> iterable = (AbstractTraversable<T>) newIterableBuilder().build();
        assertEquals(0, iterable.size(), "Expected size 0 after building an empty list");
    }

    @Test
    public void testSizeForOneElement() {
        withValue(value -> {
            final AbstractTraversable<T> iterable = (AbstractTraversable<T>) newIterableBuilder().add(value).build();
            assertEquals(1, iterable.size(), "Expected size 1 after building it adding a single value " + value);
        });
    }

    @Test
    public void testIsEmptyForNoElements() {
        final AbstractTraversable<T> list = (AbstractTraversable<T>) newIterableBuilder().build();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testIsEmptyForASingleElement() {
        withValue(value -> {
            final AbstractTraversable<T> iterable = (AbstractTraversable<T>) newIterableBuilder().add(value).build();
            assertFalse(iterable.isEmpty(), "isEmpty is expected to return false when iterable includes " + value);
        });
    }

    @Test
    public void testIteratingForEmptyList() {
        final Traversable<T> collection = newIterableBuilder().build();
        assertFalse(collection.iterator().hasNext(), "Expected an empty iterator for an empty collection");
    }

    @Test
    public void testIteratingForASingleElement() {
        withValue(value -> {
            final Traversable<T> list = newIterableBuilder().add(value).build();
            final Iterator<T> iterator = list.iterator();
            assertTrue(iterator.hasNext(), "Expected true in hasNext for no empty iterators");
            assertEquals(value, iterator.next());
            assertFalse(iterator.hasNext(), "Expected false in hasNext while all elements loaded. Failing for value " + value);
        });
    }

    @Test
    public void testContainsForEmptyList() {
        withValue(value -> {
            final Traversable<T> list = newIterableBuilder().build();
            if (list.contains(value)) {
                fail("contains method is expected to return false always for any empty set. " +
                        "But returned true for " + value);
            }
        });
    }

    @Test
    public void testContainsForASingleElement() {
        withValue(valueIncluded -> {
            final Traversable<T> list = newIterableBuilder().add(valueIncluded).build();
            withValue(otherValue -> {
                if (equal(valueIncluded, otherValue) && !list.contains(otherValue)) {
                    fail("contains method is expected to return true when containing the value. But failing for value " + otherValue);
                }
                else if (!equal(valueIncluded, otherValue) && list.contains(otherValue)) {
                    fail("contains method is expected to return false when no containing the value. But failing for value " + otherValue + " while only containing " + valueIncluded);
                }
            });
        });
    }

    @Test
    public void testContainsForMultipleElements() {
        withValue(a -> withValue(b -> {
            final Traversable<T> traversable = newIterableBuilder().add(a).add(b).build();
            boolean aIncluded = false;
            boolean bIncluded = false;
            for (T value : traversable) {
                if (value == a) aIncluded = true;
                if (value == b) bIncluded = true;
            }
            final boolean aIn = aIncluded;
            final boolean bIn = bIncluded;

            withValue(value -> {
                final boolean expected = aIn && equal(a, value) || bIn && equal(b, value);
                assertEquals(expected, traversable.contains(value));
            });
        }));
    }

    @Test
    public void testAnyMatchWhenEmpty() {
        final Traversable<T> iterable = newIterableBuilder().build();
        withFilterFunc(f -> assertFalse(iterable.anyMatch(f)));
    }

    @Test
    public void testAnyMatchForSingleElement() {
        withValue(value -> {
            final Traversable<T> iterable = newIterableBuilder().add(value).build();
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

    @Test
    public void testAnyMatchForMultipleElements() {
        withValue(a -> withValue(b -> {
            final Traversable<T> iterable = newIterableBuilder().add(a).add(b).build();
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

    @Test
    public void testIndexOfWhenEmpty() {
        withValue(value -> {
            assertEquals(-1, newIterableBuilder().build().indexOf(value));
        });
    }

    @Test
    public void testIndexOfForSingleElement() {
        withValue(a -> withValue(value -> {
            final Traversable<T> list = newIterableBuilder().add(a).build();
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
    public void testIndexOfForMultipleElements() {
        withValue(a -> withValue(b -> withValue(value -> {
            final Traversable<T> list = newIterableBuilder().add(a).add(b).build();

            final Iterator<T> it = list.iterator();
            int expectedIndex = -1;
            for (int i = 0; it.hasNext() && expectedIndex == -1; i++) {
                if (equal(it.next(), value)) {
                    expectedIndex = i;
                }
            }

            assertEquals(expectedIndex, list.indexOf(value));
        })));
    }

    @Test
    public void testFindFirstWhenEmpty() {
        withFilterFunc(f -> withValue(defaultValue -> {
            final Traversable<T> collection = newIterableBuilder().build();
            assertEquals(defaultValue, collection.findFirst(f, defaultValue));
        }));
    }

    @Test
    public void testFindFirstForSingleElement() {
        withFilterFunc(f -> withValue(defaultValue -> withValue(value -> {
            final Traversable<T> collection = newIterableBuilder().add(value).build();
            final T first = collection.findFirst(f, defaultValue);

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
        withFilterFunc(f -> withValue(defaultValue -> withValue(a -> withValue(b -> {
            final Traversable<T> collection = newIterableBuilder().add(a).add(b).build();

            T expected = defaultValue;
            boolean found = false;
            final Iterator<T> it = collection.iterator();
            while (it.hasNext() && !found) {
                final T value = it.next();
                if (f.apply(value)) {
                    expected = value;
                    found = true;
                }
            }

            assertSame(expected, collection.findFirst(f, defaultValue));
        }))));
    }

    private T unexpectedReduceFunction(T left, T right) {
        fail("Unexpected call to the reduce function");
        return null;
    }

    @Test
    public void testReduceForSingleElement() {
        withValue(value -> {
            final Traversable<T> iterable = newIterableBuilder().add(value).build();
            assertSame(value, iterable.reduce(this::unexpectedReduceFunction));
        });
    }

    @Test
    public void testReduceForMultipleElements() {
        withReduceFunction(func -> withValue(a -> withValue(b -> withValue(c -> {
            final Traversable<T> iterable = newIterableBuilder().add(a).add(b).add(c).build();
            final Iterator<T> it = iterable.iterator();
            T expectedValue = it.next();
            while (it.hasNext()) {
                expectedValue = func.apply(expectedValue, it.next());
            }

            assertEquals(expectedValue, iterable.reduce(func));
        }))));
    }

    @Test
    public void testReduceWithValueWhenEmpty() {
        withValue(value -> {
            final Traversable<T> iterable = newIterableBuilder().build();
            assertSame(value, iterable.reduce(this::unexpectedReduceFunction, value));
        });
    }

    @Test
    public void testReduceWithValueForSingleElement() {
        withValue(value -> {
            final Traversable<T> iterable = newIterableBuilder().add(value).build();
            assertSame(value, iterable.reduce(this::unexpectedReduceFunction, null));
        });
    }

    @Test
    public void testReduceWithValueForMultipleElements() {
        withReduceFunction(func -> withValue(a -> withValue(b -> withValue(c -> {
            final Traversable<T> iterable = newIterableBuilder().add(a).add(b).add(c).build();
            final Iterator<T> it = iterable.iterator();
            T expectedValue = it.next();
            while (it.hasNext()) {
                expectedValue = func.apply(expectedValue, it.next());
            }

            assertEquals(expectedValue, iterable.reduce(func, null));
        }))));
    }
}

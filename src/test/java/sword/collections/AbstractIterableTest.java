package sword.collections;

import junit.framework.TestCase;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class AbstractIterableTest<T> extends TestCase {

    abstract CollectionBuilder<T> newIterableBuilder();
    abstract void withValue(Procedure<T> procedure);
    abstract void withFilterFunc(Procedure<Predicate<T>> procedure);
    abstract void withReduceFunction(Procedure<ReduceFunction<T>> procedure);
    abstract void withMapFunc(Procedure<Function<T, String>> procedure);

    public void testSizeForNoElements() {
        final AbstractIterable<T> iterable = (AbstractIterable<T>) newIterableBuilder().build();
        assertEquals("Expected size 0 after building an empty list", 0, iterable.size());
    }

    public void testSizeForOneElement() {
        withValue(value -> {
            final AbstractIterable<T> iterable = (AbstractIterable<T>) newIterableBuilder().add(value).build();
            assertEquals("Expected size 1 after building it adding a single value " + value, 1, iterable.size());
        });
    }

    public void testIsEmptyForNoElements() {
        final AbstractIterable<T> list = (AbstractIterable<T>) newIterableBuilder().build();
        assertTrue(list.isEmpty());
    }

    public void testIsEmptyForASingleElement() {
        withValue(value -> {
            final AbstractIterable<T> iterable = (AbstractIterable<T>) newIterableBuilder().add(value).build();
            assertFalse("isEmpty is expected to return false when iterable includes " + value, iterable.isEmpty());
        });
    }

    public void testIteratingForEmptyList() {
        final IterableCollection<T> collection = newIterableBuilder().build();
        assertFalse("Expected an empty iterator for an empty collection", collection.iterator().hasNext());
    }

    public void testIteratingForASingleElement() {
        withValue(value -> {
            final IterableCollection<T> list = newIterableBuilder().add(value).build();
            final Iterator<T> iterator = list.iterator();
            assertTrue("Expected true in hasNext for no empty iterators", iterator.hasNext());
            assertEquals(value, iterator.next());
            assertFalse("Expected false in hasNext while all elements loaded. Failing for value " + value, iterator.hasNext());
        });
    }

    public void testContainsForEmptyList() {
        withValue(value -> {
            final IterableCollection<T> list = newIterableBuilder().build();
            if (list.contains(value)) {
                fail("contains method is expected to return false always for any empty set. " +
                        "But returned true for " + value);
            }
        });
    }

    public void testContainsForListContainingASingleElement() {
        withValue(valueIncluded -> {
            final IterableCollection<T> list = newIterableBuilder().add(valueIncluded).build();
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

    public void testContainsForListContainingMultipleElements() {
        withValue(a -> withValue(b -> {
            final IterableCollection<T> list = newIterableBuilder().add(a).add(b).build();
            withValue(value -> {
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
        final IterableCollection<T> iterable = newIterableBuilder().build();
        withFilterFunc(f -> assertFalse(iterable.anyMatch(f)));
    }

    public void testAnyMatchForSingleElement() {
        withValue(value -> {
            final IterableCollection<T> iterable = newIterableBuilder().add(value).build();
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
        withValue(a -> withValue(b -> {
            final IterableCollection<T> iterable = newIterableBuilder().add(a).add(b).build();
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

    public void testIndexOfWhenEmpty() {
        withValue(value -> {
            assertEquals(-1, newIterableBuilder().build().indexOf(value));
        });
    }

    public void testIndexOfForSingleElement() {
        withValue(a -> withValue(value -> {
            final IterableCollection<T> list = newIterableBuilder().add(a).build();
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
        withValue(a -> withValue(b -> withValue(value -> {
            final IterableCollection<T> list = newIterableBuilder().add(a).add(b).build();
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
        withFilterFunc(f -> withValue(defaultValue -> {
            final IterableCollection<T> collection = newIterableBuilder().build();
            assertEquals(defaultValue, collection.findFirst(f, defaultValue));
        }));
    }

    public void testFindFirstForSingleElement() {
        withFilterFunc(f -> withValue(defaultValue -> withValue(value -> {
            final IterableCollection<T> collection = newIterableBuilder().add(value).build();
            final T first = collection.findFirst(f, defaultValue);

            if (f.apply(value)) {
                assertSame(value, first);
            }
            else {
                assertSame(defaultValue, first);
            }
        })));
    }

    public void testFindFirstForMultipleElements() {
        withFilterFunc(f -> withValue(defaultValue -> withValue(a -> withValue(b -> {
            final IterableCollection<T> collection = newIterableBuilder().add(a).add(b).build();
            final T first = collection.findFirst(f, defaultValue);

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

    private T unexpectedReduceFunction(T left, T right) {
        fail("Unexpected call to the reduce function");
        return null;
    }

    public void testReduceForSingleElement() {
        withValue(value -> {
            final IterableCollection<T> iterable = newIterableBuilder().add(value).build();
            assertSame(value, iterable.reduce(this::unexpectedReduceFunction));
        });
    }

    public void testReduceForMultipleElements() {
        withReduceFunction(func -> withValue(a -> withValue(b -> withValue(c -> {
            final IterableCollection<T> iterable = newIterableBuilder().add(a).add(b).add(c).build();
            final Iterator<T> it = iterable.iterator();
            T expectedValue = it.next();
            while (it.hasNext()) {
                expectedValue = func.apply(expectedValue, it.next());
            }

            assertEquals(expectedValue, iterable.reduce(func));
        }))));
    }
}

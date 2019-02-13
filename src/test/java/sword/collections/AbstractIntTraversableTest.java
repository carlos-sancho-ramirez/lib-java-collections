package sword.collections;

import junit.framework.TestCase;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class AbstractIntTraversableTest extends TestCase {

    abstract AbstractIntTraversable emptyCollection();

    abstract IntTraversableBuilder newIntBuilder();
    abstract void withItem(IntProcedure procedure);
    abstract void withFilterFunc(Procedure<IntPredicate> procedure);

    private int hashReduceFunction(int left, int right) {
        return left * 31 + right;
    }

    private void withReduceFunction(Procedure<IntReduceFunction> procedure) {
        procedure.apply(this::hashReduceFunction);
    }

    public void testSizeForNoElements() {
        final Sizable iterable = (Sizable) newIntBuilder().build();
        assertEquals("Expected size 0 after building an empty list", 0, iterable.size());
    }

    public void testSizeForOneElement() {
        withItem(value -> {
            final Sizable iterable = (Sizable) newIntBuilder().add(value).build();
            assertEquals("Expected size 1 after building it adding a single value " + value, 1, iterable.size());
        });
    }

    public void testIsEmptyForNoElements() {
        final Sizable list = (Sizable) newIntBuilder().build();
        assertTrue(list.isEmpty());
    }

    public void testIsEmptyForASingleElement() {
        withItem(value -> {
            final Sizable iterable = (Sizable) newIntBuilder().add(value).build();
            assertFalse("isEmpty is expected to return false when iterable includes " + value, iterable.isEmpty());
        });
    }

    public void testIteratingForEmptyList() {
        final IntTraversable collection = newIntBuilder().build();
        assertFalse("Expected an empty iterator for an empty collection", collection.iterator().hasNext());
    }

    public void testIteratingForASingleElement() {
        withItem(value -> {
            final IntTraversable list = newIntBuilder().add(value).build();
            final Iterator<Integer> iterator = list.iterator();
            assertTrue("Expected true in hasNext for no empty iterators", iterator.hasNext());
            assertEquals(value, iterator.next().intValue());
            assertFalse("Expected false in hasNext while all elements loaded. Failing for value " + value, iterator.hasNext());
        });
    }

    public void testContainsWhenEmpty() {
        withItem(value -> {
            final IntTraversable list = newIntBuilder().build();
            if (list.contains(value)) {
                fail("contains method is expected to return false always for any empty set. " +
                        "But returned true for " + value);
            }
        });
    }

    public void testContainsWhenContainingASingleElement() {
        withItem(valueIncluded -> {
            final IntTraversable list = newIntBuilder().add(valueIncluded).build();
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
            final IntTraversable list = newIntBuilder().add(a).add(b).build();
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

    public void testAnyMatchWhenEmpty() {
        final IntTraversable iterable = newIntBuilder().build();
        withFilterFunc(f -> assertFalse(iterable.anyMatch(f)));
    }

    public void testAnyMatchForSingleElement() {
        withItem(value -> {
            final IntTraversable iterable = newIntBuilder().add(value).build();
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
            final IntTraversable iterable = newIntBuilder().add(a).add(b).build();
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
        withItem(value -> {
            assertEquals(-1, newIntBuilder().build().indexOf(value));
        });
    }

    public void testIndexOfForSingleElement() {
        withItem(a -> withItem(value -> {
            final IntTraversable list = newIntBuilder().add(a).build();
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
            final IntTraversable list = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> it = list.iterator();
            final int first = it.next();
            final boolean hasSecond = it.hasNext();
            final int second = hasSecond? it.next() : 0;
            assertFalse(it.hasNext());

            final int index = list.indexOf(value);
            if (equal(first, value)) {
                assertEquals(0, index);
            }
            else if (hasSecond && equal(second, value)) {
                assertEquals(1, index);
            }
            else {
                assertEquals(-1, index);
            }
        })));
    }

    public void testFindFirstWhenEmpty() {
        withFilterFunc(f -> withItem(defaultValue -> {
            final IntTraversable collection = newIntBuilder().build();
            assertEquals(defaultValue, collection.findFirst(f, defaultValue));
        }));
    }

    public void testFindFirstForSingleElement() {
        withFilterFunc(f -> withItem(defaultValue -> withItem(value -> {
            final IntTraversable collection = newIntBuilder().add(value).build();
            final int first = collection.findFirst(f, defaultValue);

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
            final IntTraversable collection = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> it = collection.iterator();
            final int first = it.next();
            final boolean hasSecond = it.hasNext();
            final int second = hasSecond? it.next() : 0;

            final int expected = f.apply(first)? first :
                    (hasSecond && f.apply(second))? second : defaultValue;
            assertEquals(expected, collection.findFirst(f, defaultValue));
        }))));
    }

    private int unexpectedReduceFunction(int left, int right) {
        fail("Unexpected call to the reduce function");
        return 0;
    }

    public void testReduceForSingleElement() {
        withItem(value -> {
            final IntTraversable iterable = newIntBuilder().add(value).build();
            assertEquals(value, iterable.reduce(this::unexpectedReduceFunction));
        });
    }

    public void testReduceForMultipleElements() {
        withReduceFunction(func -> withItem(a -> withItem(b -> withItem(c -> {
            final IntTraversable iterable = newIntBuilder().add(a).add(b).add(c).build();
            final Iterator<Integer> it = iterable.iterator();
            int expectedValue = it.next();
            while (it.hasNext()) {
                expectedValue = func.apply(expectedValue, it.next());
            }

            assertEquals(expectedValue, iterable.reduce(func));
        }))));
    }

    public void testReduceWithValueWhenEmpty() {
        withItem(value -> {
            final IntTraversable iterable = newIntBuilder().build();
            assertEquals(value, iterable.reduce(this::unexpectedReduceFunction, value));
        });
    }

    public void testReduceWithValueForSingleElement() {
        withItem(value -> {
            final IntTraversable iterable = newIntBuilder().add(value).build();
            assertEquals(value, iterable.reduce(this::unexpectedReduceFunction, 0));
        });
    }

    public void testReduceWithValueForMultipleElements() {
        withReduceFunction(func -> withItem(a -> withItem(b -> withItem(c -> {
            final IntTraversable iterable = newIntBuilder().add(a).add(b).add(c).build();
            final Iterator<Integer> it = iterable.iterator();
            int expectedValue = it.next();
            while (it.hasNext()) {
                expectedValue = func.apply(expectedValue, it.next());
            }

            assertEquals(expectedValue, iterable.reduce(func, 0));
        }))));
    }

    public void testMinForSingleValue() {
        withItem(a -> {
            final IntTraversable iterable = newIntBuilder().add(a).build();
            assertEquals(a, iterable.min());
        });
    }

    public void testMinForMultipleValues() {
        withItem(a -> withItem(b -> withItem(c -> {
            final IntTraversable iterable = newIntBuilder().add(a).add(b).add(c).build();
            final int halfMin = (a < b)? a : b;
            final int min = (halfMin < c)? halfMin : c;
            assertEquals(min, iterable.min());
        })));
    }

    public void testMaxForSingleValue() {
        withItem(a -> {
            final IntTraversable iterable = newIntBuilder().add(a).build();
            assertEquals(a, iterable.max());
        });
    }

    public void testMaxForMultipleValues() {
        withItem(a -> withItem(b -> withItem(c -> {
            final IntTraversable iterable = newIntBuilder().add(a).add(b).add(c).build();
            final int halfMax = (a > b)? a : b;
            final int max = (halfMax > c)? halfMax : c;
            assertEquals(max, iterable.max());
        })));
    }
}

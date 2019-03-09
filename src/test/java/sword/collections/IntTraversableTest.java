package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.SortUtils.equal;

abstract class IntTraversableTest {

    abstract IntTraversableBuilder newIntBuilder();
    abstract void withItem(IntProcedure procedure);
    abstract void withFilterFunc(Procedure<IntPredicate> procedure);

    private int hashReduceFunction(int left, int right) {
        return left * 31 + right;
    }

    private void withReduceFunction(Procedure<IntReduceFunction> procedure) {
        procedure.apply(this::hashReduceFunction);
    }

    @Test
    public void testSizeForNoElements() {
        final Sizable iterable = newIntBuilder().build();
        assertEquals(0, iterable.size());
    }

    @Test
    public void testSizeForOneElement() {
        withItem(value -> {
            final Sizable iterable = newIntBuilder().add(value).build();
            assertEquals(1, iterable.size());
        });
    }

    @Test
    public void testSizeForMultipleElements() {
        withItem(a -> withItem(b -> withItem(c -> withItem(d -> {
            final IntTraversable traversable = newIntBuilder().add(a).add(b).add(c).add(d).build();
            final IntTraverser traverser = traversable.iterator();
            int count = 0;
            while (traverser.hasNext()) {
                count++;
                traverser.next();
            }

            assertEquals(count, traversable.size());
        }))));
    }

    @Test
    public void testIsEmptyForNoElements() {
        final Sizable list = newIntBuilder().build();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testIsEmptyForASingleElement() {
        withItem(value -> assertFalse(newIntBuilder().add(value).build().isEmpty()));
    }

    @Test
    public void testIteratingForEmptyTraversable() {
        assertFalse(newIntBuilder().build().iterator().hasNext());
    }

    @Test
    public void testIteratingForASingleElement() {
        withItem(value -> {
            final IntTraversable traversable = newIntBuilder().add(value).build();
            final IntTraverser traverser = traversable.iterator();
            assertTrue(traverser.hasNext());
            assertEquals(value, traverser.next().intValue());
            assertFalse(traverser.hasNext());
        });
    }

    @Test
    public void testContainsWhenEmpty() {
        withItem(value -> assertFalse(newIntBuilder().build().contains(value)));
    }

    @Test
    public void testContainsWhenContainingASingleElement() {
        withItem(valueIncluded -> {
            final IntTraversable traversable = newIntBuilder().add(valueIncluded).build();
            withItem(otherValue -> {
                assertFalse(valueIncluded == otherValue && !traversable.contains(otherValue));
                assertFalse(valueIncluded != otherValue && traversable.contains(otherValue));
            });
        });
    }

    @Test
    public void testContainsWhenContainingMultipleElements() {
        withItem(a -> withItem(b -> {
            final IntTraversable traversable = newIntBuilder().add(a).add(b).build();
            withItem(value -> {
                assertFalse((a == value || b == value) && !traversable.contains(value));
                assertFalse(a != value && b != value && traversable.contains(value));
            });
        }));
    }

    @Test
    public void testAnyMatchWhenEmpty() {
        final IntTraversable traversable = newIntBuilder().build();
        withFilterFunc(f -> assertFalse(traversable.anyMatch(f)));
    }

    @Test
    public void testAnyMatchForSingleElement() {
        withItem(value -> {
            final IntTraversable traversable = newIntBuilder().add(value).build();
            withFilterFunc(f -> {
                if (f.apply(value)) {
                    assertTrue(traversable.anyMatch(f));
                }
                else {
                    assertFalse(traversable.anyMatch(f));
                }
            });
        });
    }

    @Test
    public void testAnyMatchForMultipleElements() {
        withItem(a -> withItem(b -> {
            final IntTraversable traversable = newIntBuilder().add(a).add(b).build();
            withFilterFunc(f -> {
                if (f.apply(a) || f.apply(b)) {
                    assertTrue(traversable.anyMatch(f));
                }
                else {
                    assertFalse(traversable.anyMatch(f));
                }
            });
        }));
    }

    @Test
    public void testIndexOfWhenEmpty() {
        withItem(value -> assertEquals(-1, newIntBuilder().build().indexOf(value)));
    }

    @Test
    public void testIndexOfForSingleElement() {
        withItem(a -> withItem(value -> {
            final int index = newIntBuilder().add(a).build().indexOf(value);

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
        withItem(a -> withItem(b -> withItem(value -> {
            final IntTraversable traversable = newIntBuilder().add(a).add(b).build();
            final IntTraverser traverser = traversable.iterator();
            final int first = traverser.next();
            final boolean hasSecond = traverser.hasNext();
            final int second = hasSecond? traverser.next() : 0;
            assertFalse(traverser.hasNext());

            final int index = traversable.indexOf(value);
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

    @Test
    public void testFindFirstWhenEmpty() {
        withFilterFunc(f -> withItem(defaultValue -> {
            assertEquals(defaultValue, newIntBuilder().build().findFirst(f, defaultValue));
        }));
    }

    @Test
    public void testFindFirstForSingleElement() {
        withFilterFunc(f -> withItem(defaultValue -> withItem(value -> {
            final IntTraversable traversable = newIntBuilder().add(value).build();
            final int expected = f.apply(value)? value : defaultValue;
            assertEquals(expected, traversable.findFirst(f, defaultValue));
        })));
    }

    @Test
    public void testFindFirstForMultipleElements() {
        withFilterFunc(f -> withItem(defaultValue -> withItem(a -> withItem(b -> {
            final IntTraversable traversable = newIntBuilder().add(a).add(b).build();
            final IntTraverser traverser = traversable.iterator();
            final int first = traverser.next();
            final boolean hasSecond = traverser.hasNext();
            final int second = hasSecond? traverser.next() : 0;

            final int expected = f.apply(first)? first :
                    (hasSecond && f.apply(second))? second : defaultValue;
            assertEquals(expected, traversable.findFirst(f, defaultValue));
        }))));
    }

    private int unexpectedReduceFunction(int left, int right) {
        fail("Unexpected call to the reduce function");
        return 0;
    }

    @Test
    public void testReduceForSingleElement() {
        withItem(value -> {
            final IntTraversable traversable = newIntBuilder().add(value).build();
            assertEquals(value, traversable.reduce(this::unexpectedReduceFunction));
        });
    }

    @Test
    public void testReduceForMultipleElements() {
        withReduceFunction(func -> withItem(a -> withItem(b -> withItem(c -> {
            final IntTraversable traversable = newIntBuilder().add(a).add(b).add(c).build();
            final IntTraverser traverser = traversable.iterator();
            int expectedValue = traverser.next();
            while (traverser.hasNext()) {
                expectedValue = func.apply(expectedValue, traverser.next());
            }

            assertEquals(expectedValue, traversable.reduce(func));
        }))));
    }

    @Test
    public void testReduceWithValueWhenEmpty() {
        withItem(value -> {
            final IntTraversable traversable = newIntBuilder().build();
            assertEquals(value, traversable.reduce(this::unexpectedReduceFunction, value));
        });
    }

    @Test
    public void testReduceWithValueForSingleElement() {
        withItem(value -> {
            final IntTraversable traversable = newIntBuilder().add(value).build();
            assertEquals(value, traversable.reduce(this::unexpectedReduceFunction, 0));
        });
    }

    @Test
    public void testReduceWithValueForMultipleElements() {
        withReduceFunction(func -> withItem(a -> withItem(b -> withItem(c -> {
            final IntTraversable traversable = newIntBuilder().add(a).add(b).add(c).build();
            final IntTraverser traverser = traversable.iterator();
            int expectedValue = traverser.next();
            while (traverser.hasNext()) {
                expectedValue = func.apply(expectedValue, traverser.next());
            }

            assertEquals(expectedValue, traversable.reduce(func, 0));
        }))));
    }

    @Test
    public void testMinForSingleValue() {
        withItem(a -> assertEquals(a, newIntBuilder().add(a).build().min()));
    }

    @Test
    public void testMinForMultipleValues() {
        withItem(a -> withItem(b -> withItem(c -> {
            final IntTraversable traversable = newIntBuilder().add(a).add(b).add(c).build();
            final int halfMin = (a < b)? a : b;
            final int min = (halfMin < c)? halfMin : c;
            assertEquals(min, traversable.min());
        })));
    }

    @Test
    public void testMaxForSingleValue() {
        withItem(a -> assertEquals(a, newIntBuilder().add(a).build().max()));
    }

    @Test
    public void testMaxForMultipleValues() {
        withItem(a -> withItem(b -> withItem(c -> {
            final IntTraversable iterable = newIntBuilder().add(a).add(b).add(c).build();
            final int halfMax = (a > b)? a : b;
            final int max = (halfMax > c)? halfMax : c;
            assertEquals(max, iterable.max());
        })));
    }
}

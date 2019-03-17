package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.SortUtils.equal;

abstract class TraversableTest<T, B extends TraversableBuilder<T>> {

    abstract void withBuilderSupplier(Procedure<BuilderSupplier<T, B>> procedure);
    abstract void withValue(Procedure<T> procedure);
    abstract void withFilterFunc(Procedure<Predicate<T>> procedure);
    abstract void withReduceFunction(Procedure<ReduceFunction<T>> procedure);
    abstract void withMapFunc(Procedure<Function<T, String>> procedure);
    abstract void withMapToIntFunc(Procedure<IntResultFunction<T>> procedure);

    @Test
    public void testSizeForNoElements() {
        withBuilderSupplier(supplier -> assertEquals(0, supplier.newBuilder().build().size()));
    }

    @Test
    public void testSizeForOneElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().add(value).build();
            assertEquals(1, iterable.size());
        }));
    }

    @Test
    public void testIsEmptyForNoElements() {
        withBuilderSupplier(supplier -> assertTrue(supplier.newBuilder().build().isEmpty()));
    }

    @Test
    public void testIsEmptyForASingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().add(value).build().isEmpty());
        }));
    }

    @Test
    public void testIteratingForEmptyList() {
        withBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().iterator().hasNext());
        });
    }

    @Test
    public void testIteratingForASingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> list = supplier.newBuilder().add(value).build();
            final Iterator<T> iterator = list.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(value, iterator.next());
            assertFalse(iterator.hasNext());
        }));
    }

    @Test
    public void testContainsForEmptyList() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> list = supplier.newBuilder().build();
            assertFalse(list.contains(value));
        }));
    }

    @Test
    public void testContainsForASingleElement() {
        withValue(valueIncluded -> withBuilderSupplier(supplier -> {
            final Traversable<T> list = supplier.newBuilder().add(valueIncluded).build();
            withValue(otherValue -> {
                if (equal(valueIncluded, otherValue) && !list.contains(otherValue)) {
                    fail("contains method is expected to return true when containing the value. But failing for value " + otherValue);
                }
                else if (!equal(valueIncluded, otherValue) && list.contains(otherValue)) {
                    fail("contains method is expected to return false when no containing the value. But failing for value " + otherValue + " while only containing " + valueIncluded);
                }
            });
        }));
    }

    @Test
    public void testContainsForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final Traversable<T> traversable = supplier.newBuilder().add(a).add(b).build();
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
        })));
    }

    @Test
    public void testAnyMatchWhenEmpty() {
        final Predicate<T> func = v -> {
            fail("This method should never be called in empty collections");
            return true;
        };

        withBuilderSupplier(supplier -> assertFalse(supplier.newBuilder().build().anyMatch(func)));
    }

    @Test
    public void testAnyMatchForSingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().add(value).build();
            withFilterFunc(f -> {
                if (f.apply(value)) {
                    assertTrue(iterable.anyMatch(f));
                }
                else {
                    assertFalse(iterable.anyMatch(f));
                }
            });
        }));
    }

    @Test
    public void testAnyMatchForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().add(a).add(b).build();
            withFilterFunc(f -> {
                if (f.apply(a) || f.apply(b)) {
                    assertTrue(iterable.anyMatch(f));
                }
                else {
                    assertFalse(iterable.anyMatch(f));
                }
            });
        })));
    }

    @Test
    public void testIndexOfWhenEmpty() {
        withValue(value -> withBuilderSupplier(supplier -> {
            assertEquals(-1, supplier.newBuilder().build().indexOf(value));
        }));
    }

    @Test
    public void testIndexOfForSingleElement() {
        withValue(a -> withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> traversable = supplier.newBuilder().add(a).build();
            final int index = traversable.indexOf(value);

            if (equal(a, value)) {
                assertEquals(0, index);
            }
            else {
                assertEquals(-1, index);
            }
        })));
    }

    @Test
    public void testIndexOfForMultipleElements() {
        withValue(a -> withValue(b -> withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> traversable = supplier.newBuilder().add(a).add(b).build();

            final Iterator<T> it = traversable.iterator();
            int expectedIndex = -1;
            for (int i = 0; it.hasNext() && expectedIndex == -1; i++) {
                if (equal(it.next(), value)) {
                    expectedIndex = i;
                }
            }

            assertEquals(expectedIndex, traversable.indexOf(value));
        }))));
    }

    @Test
    public void testFindFirstWhenEmpty() {
        withFilterFunc(f -> withValue(defaultValue -> withBuilderSupplier(supplier -> {
            assertEquals(defaultValue, supplier.newBuilder().build().findFirst(f, defaultValue));
        })));
    }

    @Test
    public void testFindFirstForSingleElement() {
        withFilterFunc(f -> withValue(defaultValue -> withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> collection = supplier.newBuilder().add(value).build();
            final T first = collection.findFirst(f, defaultValue);

            if (f.apply(value)) {
                assertSame(value, first);
            }
            else {
                assertSame(defaultValue, first);
            }
        }))));
    }

    @Test
    public void testFindFirstForMultipleElements() {
        withFilterFunc(f -> withValue(defaultValue -> withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final Traversable<T> collection = supplier.newBuilder().add(a).add(b).build();

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
        })))));
    }

    private T unexpectedReduceFunction(T left, T right) {
        fail("Unexpected call to the reduce function");
        return null;
    }

    @Test
    public void testReduceForSingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().add(value).build();
            assertSame(value, iterable.reduce(this::unexpectedReduceFunction));
        }));
    }

    @Test
    public void testReduceForMultipleElements() {
        withReduceFunction(func -> withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().add(a).add(b).add(c).build();
            final Iterator<T> it = iterable.iterator();
            T expectedValue = it.next();
            while (it.hasNext()) {
                expectedValue = func.apply(expectedValue, it.next());
            }

            assertEquals(expectedValue, iterable.reduce(func));
        })))));
    }

    @Test
    public void testReduceWithValueWhenEmpty() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().build();
            assertSame(value, iterable.reduce(this::unexpectedReduceFunction, value));
        }));
    }

    @Test
    public void testReduceWithValueForSingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().add(value).build();
            assertSame(value, iterable.reduce(this::unexpectedReduceFunction, null));
        }));
    }

    @Test
    public void testReduceWithValueForMultipleElements() {
        withReduceFunction(func -> withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().add(a).add(b).add(c).build();
            final Iterator<T> it = iterable.iterator();
            T expectedValue = it.next();
            while (it.hasNext()) {
                expectedValue = func.apply(expectedValue, it.next());
            }

            assertEquals(expectedValue, iterable.reduce(func, null));
        })))));
    }
}

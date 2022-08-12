package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sword.collections.SortUtils.equal;

interface TraversableTest<T, B extends TraversableBuilder<T>> {

    void withBuilderSupplier(Procedure<BuilderSupplier<T, B>> procedure);
    void withValue(Procedure<T> procedure);
    void withFilterFunc(Procedure<Predicate<T>> procedure);
    void withReduceFunction(Procedure<ReduceFunction<T>> procedure);
    void withMapFunc(Procedure<Function<T, String>> procedure);
    void withMapToIntFunc(Procedure<IntResultFunction<T>> procedure);

    default void withArbitraryBuilder(Procedure<TraversableBuilder<T>> procedure) {
        procedure.apply(new ImmutableList.Builder<>());
        procedure.apply(new ImmutableHashSet.Builder<>());
        procedure.apply(new ImmutableHashMapTest.HashCodeKeyTraversableBuilder<>());
        procedure.apply(new ImmutableIntKeyMapTest.HashCodeKeyTraversableBuilder<>());
        procedure.apply(new MutableList.Builder<>());
        procedure.apply(new MutableHashSet.Builder<>());
        procedure.apply(new MutableHashMapTest.HashCodeKeyTraversableBuilder<>());
        procedure.apply(new MutableIntKeyMapTest.HashCodeKeyTraversableBuilder<>());
    }

    @Test
    default void testSizeForNoElements() {
        withBuilderSupplier(supplier -> assertEquals(0, supplier.newBuilder().build().size()));
    }

    @Test
    default void testSizeForOneElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().add(value).build();
            assertEquals(1, iterable.size());
        }));
    }

    @Test
    default void testIsEmptyForNoElements() {
        withBuilderSupplier(supplier -> assertTrue(supplier.newBuilder().build().isEmpty()));
    }

    @Test
    default void testIsEmptyForASingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().add(value).build().isEmpty());
        }));
    }

    @Test
    default void testIteratingForEmptyList() {
        withBuilderSupplier(supplier -> {
            assertFalse(supplier.newBuilder().build().iterator().hasNext());
        });
    }

    @Test
    default void testIteratingForASingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> list = supplier.newBuilder().add(value).build();
            final Iterator<T> iterator = list.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(value, iterator.next());
            assertFalse(iterator.hasNext());
        }));
    }

    @Test
    default void testContainsForEmptyList() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> list = supplier.newBuilder().build();
            assertFalse(list.contains(value));
        }));
    }

    @Test
    default void testContainsForASingleElement() {
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
    default void testContainsForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final Traversable<T> traversable = supplier.newBuilder().add(a).add(b).build();
            boolean aIncluded = false;
            boolean bIncluded = false;
            for (T value : traversable) {
                if (value == a) {
                    aIncluded = true;
                }

                if (value == b) {
                    bIncluded = true;
                }
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
    default void testAnyMatchWhenEmpty() {
        final Predicate<T> func = v -> {
            fail("This method should never be called in empty collections");
            return true;
        };

        withBuilderSupplier(supplier -> assertFalse(supplier.newBuilder().build().anyMatch(func)));
    }

    @Test
    default void testAnyMatchForSingleElement() {
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
    default void testAnyMatchForMultipleElements() {
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
    default void testAllMatchWhenEmpty() {
        final Predicate<T> func = v -> {
            fail("This method should never be called in empty collections");
            return true;
        };

        withBuilderSupplier(supplier -> assertTrue(supplier.newBuilder().build().allMatch(func)));
    }

    @Test
    default void testAllMatchForSingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().add(value).build();
            withFilterFunc(f -> assertEquals(f.apply(value), iterable.allMatch(f)));
        }));
    }

    @Test
    default void testAllMatchForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().add(a).add(b).build();
            withFilterFunc(f -> {
                final boolean expected = f.apply(a) && f.apply(b);
                assertEquals(expected, iterable.allMatch(f));
            });
        })));
    }

    @Test
    default void testIndexOfWhenEmpty() {
        withValue(value -> withBuilderSupplier(supplier -> {
            assertEquals(-1, supplier.newBuilder().build().indexOf(value));
        }));
    }

    @Test
    default void testIndexOfForSingleElement() {
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
    default void testIndexOfForMultipleElements() {
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
    default void testIndexWhereWhenEmpty() {
        final Predicate<T> predicate = v -> {
            throw new AssertionError("This method should not be called");
        };

        withBuilderSupplier(supplier -> assertEquals(-1, supplier.newBuilder().build().indexWhere(predicate)));
    }

    @Test
    default void testIndexWhereForSingleElement() {
        withFilterFunc(predicate -> withValue(a -> withBuilderSupplier(supplier -> {
            final Traversable<T> collection = supplier.newBuilder().add(a).build();
            final int expected = predicate.apply(a)? 0 : -1;
            assertEquals(expected, collection.indexWhere(predicate));
        })));
    }

    @Test
    default void testIndexWhereForMultipleElements() {
        withFilterFunc(predicate -> withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final Traversable<T> collection = supplier.newBuilder().add(a).add(b).build();
            int expected = -1;
            final Iterator<T> it = collection.iterator();
            for (int index = 0; it.hasNext(); index++) {
                if (predicate.apply(it.next())) {
                    expected = index;
                    break;
                }
            }

            assertEquals(expected, collection.indexWhere(predicate));
        }))));
    }

    @Test
    default void testFindFirstWhenEmpty() {
        withFilterFunc(f -> withValue(defaultValue -> withBuilderSupplier(supplier -> {
            assertEquals(defaultValue, supplier.newBuilder().build().findFirst(f, defaultValue));
        })));
    }

    @Test
    default void testFindFirstForSingleElement() {
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
    default void testFindFirstForMultipleElements() {
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

    @Test
    default void testReduceForSingleElement() {
        final ReduceFunction<T> f = (left, right) -> fail("Unexpected call to the reduce function");
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().add(value).build();
            assertSame(value, iterable.reduce(f));
        }));
    }

    @Test
    default void testReduceForMultipleElements() {
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
    default void testReduceWithValueWhenEmpty() {
        final ReduceFunction<T> f = (left, right) -> fail("Unexpected call to the reduce function");
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().build();
            assertSame(value, iterable.reduce(f, value));
        }));
    }

    @Test
    default void testReduceWithValueForSingleElement() {
        final ReduceFunction<T> f = (left, right) -> fail("Unexpected call to the reduce function");
        withValue(value -> withBuilderSupplier(supplier -> {
            final Traversable<T> iterable = supplier.newBuilder().add(value).build();
            assertSame(value, iterable.reduce(f, null));
        }));
    }

    @Test
    default void testReduceWithValueForMultipleElements() {
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

    @Test
    default void testLastWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final Traversable<T> traversable = supplier.newBuilder().build();
            try {
                traversable.last();
                fail("Exception not thrown");
            }
            catch (EmptyCollectionException e) {
                // All fine
            }
        });
    }

    @Test
    default void testLastForSingleElement() {
        withValue(a -> withBuilderSupplier(supplier ->
                assertSame(a, supplier.newBuilder().add(a).build().last())));
    }

    @Test
    default void testLastForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Traversable<T> traversable = supplier.newBuilder().add(a).add(b).add(c).build();
            T expected = null;
            for (T t : traversable) {
                expected = t;
            }
            assertSame(expected, traversable.last());
        }))));
    }

    @Test
    default void testEqualTraversableWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final Traversable<T> empty = supplier.newBuilder().build();
            assertTrue(empty.equalTraversable(empty));
            assertFalse(empty.equalTraversable(null));

            withArbitraryBuilder(thatBuilder -> assertTrue(empty.equalTraversable(thatBuilder.build())));

            withValue(a -> withArbitraryBuilder(thatBuilder -> {
                assertFalse(empty.equalTraversable(thatBuilder.add(a).build()));
            }));
        });
    }

    @Test
    default void testEqualTraversable() {
        withArbitraryBuilder(builderForEmpty -> {
            final Traversable<T> empty = builderForEmpty.build();
            withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
                final Traversable<T> traversable = supplier.newBuilder().add(a).add(b).build();
                assertTrue(traversable.equalTraversable(traversable));
                assertFalse(traversable.equalTraversable(empty));

                withArbitraryBuilder(thatBuilder -> {
                    for (T value : traversable) {
                        thatBuilder.add(value);
                    }

                    final Traversable<T> that = thatBuilder.build();
                    final int size = traversable.size();
                    boolean expectedResult = size == that.size();
                    if (expectedResult) {
                        for (int i = 0; i < size; i++) {
                            if (!equal(traversable.valueAt(i), that.valueAt(i))) {
                                expectedResult = false;
                                break;
                            }
                        }
                    }

                    assertEquals(expectedResult, traversable.equalTraversable(that));
                });
            })));
        });
    }
}

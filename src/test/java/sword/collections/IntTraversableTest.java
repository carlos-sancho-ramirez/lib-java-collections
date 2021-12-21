package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sword.collections.SortUtils.equal;

interface IntTraversableTest<B extends IntTraversableBuilder> {

    void withBuilderSupplier(Procedure<IntBuilderSupplier<B>> procedure);
    IntTraversableBuilder newIntBuilder();
    void withValue(IntProcedure procedure);

    default void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(v -> (v & 1) == 0);
    }

    default void withReduceFunction(Procedure<IntReduceFunction> procedure) {
        procedure.apply((left, right) -> left * 31 + right);
    }

    default void withArbitraryBuilder(Procedure<IntTraversableBuilder> procedure) {
        procedure.apply(new ImmutableIntList.Builder());
        procedure.apply(new ImmutableIntArraySet.Builder());
        procedure.apply(new ImmutableIntValueHashMapTest.SameKeyAndValueTraversableBuilder());
        procedure.apply(new ImmutableIntPairMapTest.SameKeyAndValueTraversableBuilder());
        procedure.apply(new MutableIntList.Builder());
        procedure.apply(new MutableIntArraySet.Builder());
        procedure.apply(new MutableIntValueHashMapTest.SameKeyAndValueTraversableBuilder());
        procedure.apply(new MutableIntPairMapTest.SameKeyAndValueTraversableBuilder());
    }

    @Test
    default void testSizeForNoElements() {
        final Sizable iterable = newIntBuilder().build();
        assertEquals(0, iterable.size());
    }

    @Test
    default void testSizeForOneElement() {
        withValue(value -> {
            final Sizable iterable = newIntBuilder().add(value).build();
            assertEquals(1, iterable.size());
        });
    }

    @Test
    default void testSizeForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withValue(d -> {
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
    default void testIsEmptyForNoElements() {
        final Sizable list = newIntBuilder().build();
        assertTrue(list.isEmpty());
    }

    @Test
    default void testIsEmptyForASingleElement() {
        withValue(value -> assertFalse(newIntBuilder().add(value).build().isEmpty()));
    }

    @Test
    default void testIteratingForEmptyTraversable() {
        assertFalse(newIntBuilder().build().iterator().hasNext());
    }

    @Test
    default void testIteratingForASingleElement() {
        withValue(value -> {
            final IntTraversable traversable = newIntBuilder().add(value).build();
            final IntTraverser traverser = traversable.iterator();
            assertTrue(traverser.hasNext());
            assertEquals(value, traverser.next().intValue());
            assertFalse(traverser.hasNext());
        });
    }

    @Test
    default void testContainsWhenEmpty() {
        withValue(value -> assertFalse(newIntBuilder().build().contains(value)));
    }

    @Test
    default void testContainsWhenContainingASingleElement() {
        withValue(valueIncluded -> {
            final IntTraversable traversable = newIntBuilder().add(valueIncluded).build();
            withValue(otherValue -> {
                assertFalse(valueIncluded == otherValue && !traversable.contains(otherValue));
                assertFalse(valueIncluded != otherValue && traversable.contains(otherValue));
            });
        });
    }

    @Test
    default void testContainsWhenContainingMultipleElements() {
        withValue(a -> withValue(b -> {
            final IntTraversable traversable = newIntBuilder().add(a).add(b).build();
            withValue(value -> {
                assertFalse((a == value || b == value) && !traversable.contains(value));
                assertFalse(a != value && b != value && traversable.contains(value));
            });
        }));
    }

    @Test
    default void testAnyMatchWhenEmpty() {
        final IntTraversable traversable = newIntBuilder().build();
        withFilterFunc(f -> assertFalse(traversable.anyMatch(f)));
    }

    @Test
    default void testAnyMatchForSingleElement() {
        withValue(value -> {
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
    default void testAnyMatchForMultipleElements() {
        withValue(a -> withValue(b -> {
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
    default void testIndexOfWhenEmpty() {
        withValue(value -> assertEquals(-1, newIntBuilder().build().indexOf(value)));
    }

    @Test
    default void testIndexOfForSingleElement() {
        withValue(a -> withValue(value -> {
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
    default void testIndexOfForMultipleElements() {
        withValue(a -> withValue(b -> withValue(value -> {
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
    default void testIndexWhereWhenEmpty() {
        final IntPredicate predicate = v -> {
            throw new AssertionError("This method should not be called");
        };

        withBuilderSupplier(supplier -> assertEquals(-1, supplier.newBuilder().build().indexWhere(predicate)));
    }

    @Test
    default void testIndexWhereForSingleElement() {
        withFilterFunc(predicate -> withValue(a -> withBuilderSupplier(supplier -> {
            final IntTraversable collection = supplier.newBuilder().add(a).build();
            final int expected = predicate.apply(a)? 0 : -1;
            assertEquals(expected, collection.indexWhere(predicate));
        })));
    }

    @Test
    default void testIndexWhereForMultipleElements() {
        withFilterFunc(predicate -> withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final IntTraversable collection = supplier.newBuilder().add(a).add(b).build();
            int expected = -1;
            final IntTraverser it = collection.iterator();
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
        withFilterFunc(f -> withValue(defaultValue -> {
            assertEquals(defaultValue, newIntBuilder().build().findFirst(f, defaultValue));
        }));
    }

    @Test
    default void testFindFirstForSingleElement() {
        withFilterFunc(f -> withValue(defaultValue -> withValue(value -> {
            final IntTraversable traversable = newIntBuilder().add(value).build();
            final int expected = f.apply(value)? value : defaultValue;
            assertEquals(expected, traversable.findFirst(f, defaultValue));
        })));
    }

    @Test
    default void testFindFirstForMultipleElements() {
        withFilterFunc(f -> withValue(defaultValue -> withValue(a -> withValue(b -> {
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

    @Test
    default void testReduceForSingleElement() {
        final IntReduceFunction f = (left, right) -> {
            fail("Unexpected call to the reduce function");
            return 0;
        };

        withValue(value -> {
            final IntTraversable traversable = newIntBuilder().add(value).build();
            assertEquals(value, traversable.reduce(f));
        });
    }

    @Test
    default void testReduceForMultipleElements() {
        withReduceFunction(func -> withValue(a -> withValue(b -> withValue(c -> {
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
    default void testReduceWithValueWhenEmpty() {
        final IntReduceFunction f = (left, right) -> {
            fail("Unexpected call to the reduce function");
            return 0;
        };

        withValue(value -> {
            final IntTraversable traversable = newIntBuilder().build();
            assertEquals(value, traversable.reduce(f, value));
        });
    }

    @Test
    default void testReduceWithValueForSingleElement() {
        final IntReduceFunction f = (left, right) -> {
            fail("Unexpected call to the reduce function");
            return 0;
        };

        withValue(value -> {
            final IntTraversable traversable = newIntBuilder().add(value).build();
            assertEquals(value, traversable.reduce(f, 0));
        });
    }

    @Test
    default void testReduceWithValueForMultipleElements() {
        withReduceFunction(func -> withValue(a -> withValue(b -> withValue(c -> {
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
    default void testMinForSingleValue() {
        withValue(a -> assertEquals(a, newIntBuilder().add(a).build().min()));
    }

    @Test
    default void testMinForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> {
            final IntTraversable traversable = newIntBuilder().add(a).add(b).add(c).build();
            final int halfMin = (a < b)? a : b;
            final int min = (halfMin < c)? halfMin : c;
            assertEquals(min, traversable.min());
        })));
    }

    @Test
    default void testMaxForSingleValue() {
        withValue(a -> assertEquals(a, newIntBuilder().add(a).build().max()));
    }

    @Test
    default void testMaxForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> {
            final IntTraversable iterable = newIntBuilder().add(a).add(b).add(c).build();
            final int halfMax = (a > b)? a : b;
            final int max = (halfMax > c)? halfMax : c;
            assertEquals(max, iterable.max());
        })));
    }

    @Test
    default void testSumWhenEmpty() {
        assertEquals(0, newIntBuilder().build().sum());
    }

    @Test
    default void testSumForSingleValue() {
        withValue(a -> assertEquals(a, newIntBuilder().add(a).build().sum()));
    }

    @Test
    default void testSumForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> {
            final IntTraversable iterable = newIntBuilder().add(a).add(b).add(c).build();
            int result = 0;
            for (int value : iterable) {
                result += value;
            }

            assertEquals(result, iterable.sum());
        })));
    }

    @Test
    default void testEqualTraversableWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntTraversable empty = supplier.newBuilder().build();
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
            final IntTraversable empty = builderForEmpty.build();
            withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
                final IntTraversable traversable = supplier.newBuilder().add(a).add(b).build();
                assertTrue(traversable.equalTraversable(traversable));
                assertFalse(traversable.equalTraversable(empty));

                withArbitraryBuilder(thatBuilder -> {
                    for (int value : traversable) {
                        thatBuilder.add(value);
                    }

                    final IntTraversable that = thatBuilder.build();
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

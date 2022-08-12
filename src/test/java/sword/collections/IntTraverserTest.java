package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;

abstract class IntTraverserTest<B extends IntTraversableBuilder> {

    abstract void withBuilder(Procedure<B> procedure);

    void withValue(IntProcedure value) {
        withInt(value);
    }

    private static boolean isEven(int value) {
        return (value & 1) == 0;
    }

    void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(IntTraverserTest::isEven);
    }

    private static int substract(int a, int b) {
        return a - b;
    }

    private void withReduceFunction(Procedure<IntReduceFunction> procedure) {
        procedure.apply(IntTraverserTest::substract);
    }

    private void withArbitraryBuilder(Procedure<IntTraversableBuilder> procedure) {
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
    void testContainsWhenEmpty() {
        withValue(value -> withBuilder(builder -> {
            if (builder.build().iterator().contains(value)) {
                fail("contains method is expected to return false always for any empty set. " +
                        "But returned true for " + value);
            }
        }));
    }

    @Test
    void testContainsWhenContainingASingleElement() {
        withValue(valueIncluded -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(valueIncluded).build();
            withValue(otherValue -> {
                final IntTraverser traverser = iterable.iterator();
                if (equal(valueIncluded, otherValue) && !traverser.contains(otherValue)) {
                    fail("contains method is expected to return true when containing the value. But failing for value " + otherValue);
                }
                else if (!equal(valueIncluded, otherValue) && traverser.contains(otherValue)) {
                    fail("contains method is expected to return false when no containing the value. But failing for value " + otherValue + " while only containing " + valueIncluded);
                }
            });
        }));
    }

    @Test
    void testContainsWhenContainingMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).add(b).build();
            final IntTraverser it = iterable.iterator();
            final int first = it.next();
            final boolean hasSecond = it.hasNext();
            final int second = hasSecond? it.next() : 0;
            assertFalse(it.hasNext());

            withValue(value -> {
                final IntTraverser traverser = iterable.iterator();
                if (equal(first, value) || hasSecond && equal(second, value)) {
                    assertTrue(traverser.contains(value));
                }
                else {
                    assertFalse(traverser.contains(value));
                }
            });
        })));
    }

    @Test
    void testAnyMatchWhenEmpty() {
        withBuilder(builder -> {
            final IntTraversable iterable = builder.build();
            withFilterFunc(f -> assertFalse(iterable.iterator().anyMatch(f)));
        });
    }

    @Test
    void testAnyMatchForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(value).build();
            withFilterFunc(f -> {
                final IntTraverser traverser = iterable.iterator();
                if (f.apply(value)) {
                    assertTrue(traverser.anyMatch(f));
                }
                else {
                    assertFalse(traverser.anyMatch(f));
                }
            });
        }));
    }

    @Test
    void testAnyMatchForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).add(b).build();
            withFilterFunc(f -> {
                final IntTraverser traverser = iterable.iterator();
                if (f.apply(a) || f.apply(b)) {
                    assertTrue(traverser.anyMatch(f));
                }
                else {
                    assertFalse(traverser.anyMatch(f));
                }
            });
        })));
    }

    @Test
    void testAllMatchWhenEmpty() {
        withBuilder(builder -> {
            final IntTraversable iterable = builder.build();
            withFilterFunc(f -> assertTrue(iterable.iterator().allMatch(f)));
        });
    }

    @Test
    void testAllMatchForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(value).build();
            withFilterFunc(f -> assertEquals(f.apply(value), iterable.iterator().allMatch(f)));
        }));
    }

    @Test
    void testAllMatchForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).add(b).build();
            withFilterFunc(f -> {
                final boolean expected = f.apply(a) && f.apply(b);
                assertEquals(expected, iterable.iterator().allMatch(f));
            });
        })));
    }

    @Test
    void testIndexOfWhenEmpty() {
        withBuilder(builder -> {
            IntTraversable iterable = builder.build();
            withValue(value -> assertEquals(-1, iterable.iterator().indexOf(value)));
        });
    }

    @Test
    void testIndexOfForSingleElement() {
        withValue(a -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).build();
            withValue(value -> {
                final int expected = equal(a, value)? 0 : -1;
                assertEquals(expected, iterable.iterator().indexOf(value));
            });
        }));
    }

    @Test
    void testIndexOfForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).add(b).build();
            withValue(value -> {
                final IntTraverser it = iterable.iterator();
                final int first = it.next();
                final boolean hasSecond = it.hasNext();
                final int second = hasSecond? it.next() : 0;
                assertFalse(it.hasNext());

                final int expected = equal(first, value)? 0 :
                        (hasSecond && equal(second, value))? 1 : -1;
                assertEquals(expected, iterable.iterator().indexOf(value));
            });
        })));
    }

    @Test
    void testIndexWhereWhenEmpty() {
        final IntPredicate predicate = v -> {
            throw new AssertionError("This method should not be called");
        };

        withBuilder(builder -> assertEquals(-1, builder.build().iterator().indexWhere(predicate)));
    }

    @Test
    void testIndexWhereForSingleElement() {
        withFilterFunc(predicate -> withValue(a -> withBuilder(builder -> {
            final IntTraversable collection = builder.add(a).build();
            final int expected = predicate.apply(a)? 0 : -1;
            assertEquals(expected, collection.iterator().indexWhere(predicate));
        })));
    }

    @Test
    void testIndexWhereForMultipleElements() {
        withFilterFunc(predicate -> withValue(a -> withValue(b -> withBuilder(builder -> {
            final IntTraversable collection = builder.add(a).add(b).build();
            int expected = -1;
            final IntTraverser it = collection.iterator();
            for (int index = 0; it.hasNext(); index++) {
                if (predicate.apply(it.next())) {
                    expected = index;
                    break;
                }
            }

            assertEquals(expected, collection.iterator().indexWhere(predicate));
        }))));
    }

    @Test
    void testValueAtForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final IntTraverser traverser = builder.add(value).build().iterator();
            assertEquals(value, traverser.valueAt(0));
        }));
    }

    @Test
    void testValueAtForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).add(b).build();
            final IntTraverser it = iterable.iterator();
            final int first = it.next();
            final boolean hasSecond = it.hasNext();
            final int second = hasSecond? it.next() : 0;
            assertFalse(it.hasNext());

            assertEquals(first, iterable.iterator().valueAt(0));
            if (hasSecond) {
                assertEquals(second, iterable.iterator().valueAt(1));
            }
        })));
    }

    @Test
    void testFindFirstWhenEmpty() {
        withBuilder(builder -> {
            final IntTraversable iterable = builder.build();
            final IntPredicate predicate = value -> {
                throw new AssertionError("This should not be called");
            };

            withValue(defaultValue -> assertEquals(defaultValue, iterable.iterator().findFirst(predicate, defaultValue)));
        });
    }

    @Test
    void testFindFirstForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(value).build();
            withValue(defaultValue -> withFilterFunc(f -> {
                final int expected = f.apply(value)? value : defaultValue;
                assertEquals(expected, iterable.iterator().findFirst(f, defaultValue));
            }));
        }));
    }

    @Test
    void testFindFirstForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).add(b).build();
            final IntTraverser it = iterable.iterator();
            final int first = it.next();
            final boolean hasSecond = it.hasNext();
            final int second = hasSecond? it.next() : 0;

            withFilterFunc(f -> withValue(defaultValue -> {
                final int expected = f.apply(first)? first :
                        (hasSecond && f.apply(second))? second : defaultValue;
                assertEquals(expected, iterable.iterator().findFirst(f, defaultValue));
            }));
        })));
    }

    private static <E> E unexpectedReduceFunction(E left, E right) {
        throw new AssertionError("This should not be called");
    }

    @Test
    void testReduceForSingleElement() {
        final IntReduceFunction func = IntTraverserTest::unexpectedReduceFunction;
        withValue(value -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(value).build();
            assertEquals(value, iterable.iterator().reduce(func));
        }));
    }

    @Test
    void testReduceForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).add(b).add(c).build();
            withReduceFunction(func -> {
                final IntTraverser it = iterable.iterator();
                int expectedValue = it.next();
                while (it.hasNext()) {
                    expectedValue = func.apply(expectedValue, it.next());
                }

                assertEquals(expectedValue, iterable.iterator().reduce(func));
            });
        }))));
    }

    @Test
    void testReduceWithValueWhenEmpty() {
        withBuilder(builder -> {
            final IntTraversable iterable = builder.build();
            final IntReduceFunction func = IntTraverserTest::unexpectedReduceFunction;
            withValue(value -> assertEquals(value, iterable.iterator().reduce(func, value)));
        });
    }

    @Test
    void testReduceWithValueForSingleElement() {
        final IntReduceFunction func = IntTraverserTest::unexpectedReduceFunction;
        withValue(value -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(value).build();
            withValue(defValue -> assertEquals(value, iterable.iterator().reduce(func, defValue)));
        }));
    }

    @Test
    void testReduceWithValueForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).add(b).add(c).build();
            withReduceFunction(func -> {
                final IntTraverser it = iterable.iterator();
                int v = it.next();
                while (it.hasNext()) {
                    v = func.apply(v, it.next());
                }

                final int expected = v;
                withValue(defValue -> assertEquals(expected, iterable.iterator().reduce(func, defValue)));
            });
        }))));
    }

    @Test
    void testMinForSingleValue() {
        withValue(a -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).build();
            assertEquals(a, iterable.iterator().min());
        }));
    }

    @Test
    void testMinForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).add(b).add(c).build();
            final int halfMin = (a < b)? a : b;
            final int min = (halfMin < c)? halfMin : c;
            assertEquals(min, iterable.iterator().min());
        }))));
    }

    @Test
    void testMaxForSingleValue() {
        withValue(a -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).build();
            assertEquals(a, iterable.iterator().max());
        }));
    }

    @Test
    void testMaxForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).add(b).add(c).build();
            final int halfMax = (a > b)? a : b;
            final int max = (halfMax > c)? halfMax : c;
            assertEquals(max, iterable.iterator().max());
        }))));
    }

    @Test
    void testSumWhenEmpty() {
        withBuilder(builder -> {
            final IntTraversable iterable = builder.build();
            assertEquals(0, iterable.iterator().sum());
        });
    }

    @Test
    void testSumForSingleValue() {
        withValue(a -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).build();
            assertEquals(a, iterable.iterator().sum());
        }));
    }

    @Test
    void testSumForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTraversable iterable = builder.add(a).add(b).add(c).build();
            int result = 0;
            for (int value : iterable) {
                result += value;
            }

            assertEquals(result, iterable.iterator().sum());
        }))));
    }

    @Test
    void testSizeForNoElements() {
        withBuilder(builder -> assertEquals(0, builder.build().iterator().size()));
    }

    @Test
    void testSizeForOneElement() {
        withValue(value -> withBuilder(builder -> {
            final IntTraversable traversable = builder.add(value).build();
            assertEquals(1, traversable.iterator().size());
        }));
    }

    @Test
    void testSizeForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTraversable traversable = builder.add(a).add(b).add(c).build();
            final IntTraverser traverser = traversable.iterator();
            int count = 0;
            while (traverser.hasNext()) {
                count++;
                traverser.next();
            }

            assertEquals(count, traversable.iterator().size());
        }))));
    }

    @Test
    void testSkipZeroForNoElements() {
        withBuilder(builder -> {
            final IntTraverser traverser = builder.build().iterator();
            assertSame(traverser, traverser.skip(0));
            assertFalse(traverser.hasNext());
        });
    }

    @Test
    void testSkipOneForNoElements() {
        withBuilder(builder -> {
            final IntTraverser traverser = builder.build().iterator();
            assertSame(traverser, traverser.skip(1));
            assertFalse(traverser.hasNext());
        });
    }

    @Test
    void testSkipZeroForOneElement() {
        withValue(value -> withBuilder(builder -> {
            final IntTraverser traverser = builder.add(value).build().iterator();
            assertSame(traverser, traverser.skip(0));
            assertTrue(traverser.hasNext());
            assertEquals(value, traverser.next());
            assertFalse(traverser.hasNext());
        }));
    }

    @Test
    void testSkipOneForOneElement() {
        withValue(value -> withBuilder(builder -> {
            final IntTraverser traverser = builder.add(value).build().iterator();
            assertSame(traverser, traverser.skip(1));
            assertFalse(traverser.hasNext());
        }));
    }

    @Test
    void testSkipTwoForOneElement() {
        withValue(value -> withBuilder(builder -> {
            final IntTraverser traverser = builder.add(value).build().iterator();
            assertSame(traverser, traverser.skip(2));
            assertFalse(traverser.hasNext());
        }));
    }

    @Test
    void testSkipZeroForTwoElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IntTraversable traversable = builder.add(a).add(b).build();
            if (traversable.size() == 2) {
                final IntTraverser traverser = traversable.iterator();
                assertSame(traverser, traverser.skip(0));
                assertTrue(traverser.hasNext());
                final int first = traverser.next();
                final boolean reversed = first == b;
                if (!reversed) {
                    assertEquals(a, first);
                }

                assertTrue(traverser.hasNext());
                final int expectedSecond = reversed? a : b;
                assertEquals(expectedSecond, traverser.next());
                assertFalse(traverser.hasNext());
            }
        })));
    }

    @Test
    void testSkipOneForTwoElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IntTraversable traversable = builder.add(a).add(b).build();
            if (traversable.size() == 2) {
                final IntTraverser traverser = traversable.iterator();
                assertSame(traverser, traverser.skip(1));
                assertTrue(traverser.hasNext());
                assertEquals(traversable.valueAt(1), traverser.next());
                assertFalse(traverser.hasNext());
            }
        })));
    }

    @Test
    void testSkipTwoForTwoElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IntTraverser traverser = builder.add(a).add(b).build().iterator();
            assertSame(traverser, traverser.skip(2));
            assertFalse(traverser.hasNext());
        })));
    }

    @Test
    void testSkipThreeForTwoElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IntTraverser traverser = builder.add(a).add(b).build().iterator();
            assertSame(traverser, traverser.skip(3));
            assertFalse(traverser.hasNext());
        })));
    }

    @Test
    void testEqualTraverserWhenEmpty() {
        withBuilder(builder -> {
            final IntTraversable empty = builder.build();
            assertTrue(empty.iterator().equalTraverser(empty.iterator()));
            assertFalse(empty.iterator().equalTraverser(null));

            withArbitraryBuilder(thatBuilder -> assertTrue(empty.iterator().equalTraverser(thatBuilder.build().iterator())));

            withValue(a -> withArbitraryBuilder(thatBuilder -> {
                assertFalse(empty.iterator().equalTraverser(thatBuilder.add(a).build().iterator()));
            }));
        });
    }

    @Test
    void testEqualTraverser() {
        withArbitraryBuilder(builderForEmpty -> {
            final IntTraversable empty = builderForEmpty.build();
            withValue(a -> withValue(b -> withBuilder(builder -> {
                final IntTraversable traversable = builder.add(a).add(b).build();
                assertTrue(traversable.iterator().equalTraverser(traversable.iterator()));
                assertFalse(traversable.iterator().equalTraverser(empty.iterator()));

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

                    assertEquals(expectedResult, traversable.iterator().equalTraverser(that.iterator()));
                });
            })));
        });
    }

    @Test
    void testLastWhenEmpty() {
        withBuilder(builder -> {
            final IntTraverser traverser = builder.build().iterator();
            try {
                traverser.last();
                fail("Exception not thrown");
            }
            catch (EmptyCollectionException e) {
                // All fine
            }
        });
    }

    @Test
    void testLastForSingleElement() {
        withValue(a -> withBuilder(builder ->
                assertEquals(a, builder.add(a).build().iterator().last())));
    }

    @Test
    void testLastForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTraversable traversable = builder.add(a).add(b).add(c).build();
            int expected = 0;
            for (int t : traversable) {
                expected = t;
            }
            assertEquals(expected, traversable.iterator().last());
        }))));
    }
}

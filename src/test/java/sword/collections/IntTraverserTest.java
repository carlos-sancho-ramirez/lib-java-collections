package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}

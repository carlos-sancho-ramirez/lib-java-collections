package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sword.collections.SortUtils.equal;

abstract class TraverserTest<T, B extends TraversableBuilder<T>> {

    abstract void withBuilder(Procedure<B> procedure);
    abstract void withValue(Procedure<T> value);
    abstract void withFilterFunc(Procedure<Predicate<T>> procedure);
    abstract void withReduceFunction(Procedure<ReduceFunction<T>> procedure);

    private void withArbitraryBuilder(Procedure<TraversableBuilder<T>> procedure) {
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
    void testSizeForNoElements() {
        withBuilder(builder -> assertEquals(0, builder.build().iterator().size()));
    }

    @Test
    void testSizeForOneElement() {
        withValue(value -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(value).build();
            assertEquals(1, iterable.iterator().size());
        }));
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
            final Traversable<T> iterable = builder.add(valueIncluded).build();
            withValue(otherValue -> {
                final Traverser<T> traverser = iterable.iterator();
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
            final Traversable<T> iterable = builder.add(a).add(b).build();
            final Traverser<T> it = iterable.iterator();
            final T first = it.next();
            final boolean hasSecond = it.hasNext();
            final T second = hasSecond? it.next() : null;
            assertFalse(it.hasNext());

            withValue(value -> {
                final Traverser<T> traverser = iterable.iterator();
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
            final Traversable<T> iterable = builder.build();
            withFilterFunc(f -> assertFalse(iterable.iterator().anyMatch(f)));
        });
    }

    @Test
    void testAnyMatchForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(value).build();
            withFilterFunc(f -> {
                final Traverser<T> traverser = iterable.iterator();
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
            final Traversable<T> iterable = builder.add(a).add(b).build();
            withFilterFunc(f -> {
                final Traverser<T> traverser = iterable.iterator();
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
            final Traversable<T> iterable = builder.build();
            withFilterFunc(f -> assertTrue(iterable.iterator().allMatch(f)));
        });
    }

    @Test
    void testAllMatchForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(value).build();
            withFilterFunc(f -> assertEquals(f.apply(value), iterable.iterator().allMatch(f)));
        }));
    }

    @Test
    void testAllMatchForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(a).add(b).build();
            withFilterFunc(f -> {
                final boolean expected = f.apply(a) && f.apply(b);
                assertEquals(expected, iterable.iterator().allMatch(f));
            });
        })));
    }

    @Test
    void testIndexOfWhenEmpty() {
        withBuilder(builder -> {
            Traversable<T> iterable = builder.build();
            withValue(value -> assertEquals(-1, iterable.iterator().indexOf(value)));
        });
    }

    @Test
    void testIndexOfForSingleElement() {
        withValue(a -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(a).build();
            withValue(value -> {
                final int expected = equal(a, value)? 0 : -1;
                assertEquals(expected, iterable.iterator().indexOf(value));
            });
        }));
    }

    @Test
    void testIndexOfForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(a).add(b).build();
            withValue(value -> {
                final Traverser<T> it = iterable.iterator();
                final T first = it.next();
                final boolean hasSecond = it.hasNext();
                final T second = hasSecond? it.next() : null;
                assertFalse(it.hasNext());

                final int expected = equal(first, value)? 0 :
                        (hasSecond && equal(second, value))? 1 : -1;
                assertEquals(expected, iterable.iterator().indexOf(value));
            });
        })));
    }

    @Test
    void testIndexWhereWhenEmpty() {
        final Predicate<T> predicate = v -> {
            throw new AssertionError("This method should not be called");
        };

        withBuilder(builder -> assertEquals(-1, builder.build().iterator().indexWhere(predicate)));
    }

    @Test
    void testIndexWhereForSingleElement() {
        withFilterFunc(predicate -> withValue(a -> withBuilder(builder -> {
            final Traversable<T> collection = builder.add(a).build();
            final int expected = predicate.apply(a)? 0 : -1;
            assertEquals(expected, collection.iterator().indexWhere(predicate));
        })));
    }

    @Test
    void testIndexWhereForMultipleElements() {
        withFilterFunc(predicate -> withValue(a -> withValue(b -> withBuilder(builder -> {
            final Traversable<T> collection = builder.add(a).add(b).build();
            int expected = -1;
            final Iterator<T> it = collection.iterator();
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
            final Traverser<T> traverser = builder.add(value).build().iterator();
            assertEquals(value, traverser.valueAt(0));
        }));
    }

    @Test
    void testValueAtForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(a).add(b).build();
            final Traverser<T> it = iterable.iterator();
            final T first = it.next();
            final boolean hasSecond = it.hasNext();
            final T second = hasSecond? it.next() : null;
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
            final Traversable<T> iterable = builder.build();
            final Predicate<T> predicate = value -> {
                throw new AssertionError("This should not be called");
            };

            withValue(defaultValue -> assertEquals(defaultValue, iterable.iterator().findFirst(predicate, defaultValue)));
        });
    }

    @Test
    void testFindFirstForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(value).build();
            withValue(defaultValue -> withFilterFunc(f -> {
                final T expected = f.apply(value)? value : defaultValue;
                assertSame(expected, iterable.iterator().findFirst(f, defaultValue));
            }));
        }));
    }

    @Test
    void testFindFirstForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(a).add(b).build();
            final Traverser<T> it = iterable.iterator();
            final T first = it.next();
            final boolean hasSecond = it.hasNext();
            final T second = hasSecond? it.next() : null;

            withFilterFunc(f -> withValue(defaultValue -> {
                final T expected = f.apply(first)? first :
                        (hasSecond && f.apply(second))? second : defaultValue;
                assertSame(expected, iterable.iterator().findFirst(f, defaultValue));
            }));
        })));
    }

    private static <E> E unexpectedReduceFunction(E left, E right) {
        throw new AssertionError("This should not be called");
    }

    @Test
    void testReduceForSingleElement() {
        final ReduceFunction<T> func = TraverserTest::unexpectedReduceFunction;
        withValue(value -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(value).build();
            assertSame(value, iterable.iterator().reduce(func));
        }));
    }

    @Test
    void testReduceForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(a).add(b).add(c).build();
            withReduceFunction(func -> {
                final Iterator<T> it = iterable.iterator();
                T expectedValue = it.next();
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
            final Traversable<T> iterable = builder.build();
            final ReduceFunction<T> func = TraverserTest::unexpectedReduceFunction;
            withValue(value -> assertSame(value, iterable.iterator().reduce(func, value)));
        });
    }

    @Test
    void testReduceWithValueForSingleElement() {
        final ReduceFunction<T> func = TraverserTest::unexpectedReduceFunction;
        withValue(value -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(value).build();
            withValue(defValue -> assertSame(value, iterable.iterator().reduce(func, defValue)));
        }));
    }

    @Test
    void testReduceWithValueForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final Traversable<T> iterable = builder.add(a).add(b).add(c).build();
            withReduceFunction(func -> {
                final Iterator<T> it = iterable.iterator();
                T v = it.next();
                while (it.hasNext()) {
                    v = func.apply(v, it.next());
                }

                final T expected = v;
                withValue(defValue -> assertEquals(expected, iterable.iterator().reduce(func, defValue)));
            });
        }))));
    }

    @Test
    void testSkipZeroForNoElements() {
        withBuilder(builder -> {
            final Traverser<T> traverser = builder.build().iterator();
            assertSame(traverser, traverser.skip(0));
            assertFalse(traverser.hasNext());
        });
    }

    @Test
    void testSkipOneForNoElements() {
        withBuilder(builder -> {
            final Traverser<T> traverser = builder.build().iterator();
            assertSame(traverser, traverser.skip(1));
            assertFalse(traverser.hasNext());
        });
    }

    @Test
    void testSkipZeroForOneElement() {
        withValue(value -> withBuilder(builder -> {
            final Traverser<T> traverser = builder.add(value).build().iterator();
            assertSame(traverser, traverser.skip(0));
            assertTrue(traverser.hasNext());
            assertSame(value, traverser.next());
            assertFalse(traverser.hasNext());
        }));
    }

    @Test
    void testSkipOneForOneElement() {
        withValue(value -> withBuilder(builder -> {
            final Traverser<T> traverser = builder.add(value).build().iterator();
            assertSame(traverser, traverser.skip(1));
            assertFalse(traverser.hasNext());
        }));
    }

    @Test
    void testSkipTwoForOneElement() {
        withValue(value -> withBuilder(builder -> {
            final Traverser<T> traverser = builder.add(value).build().iterator();
            assertSame(traverser, traverser.skip(2));
            assertFalse(traverser.hasNext());
        }));
    }

    @Test
    void testSkipZeroForTwoElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final Traversable<T> traversable = builder.add(a).add(b).build();
            if (traversable.size() == 2) {
                final Traverser<T> traverser = traversable.iterator();
                assertSame(traverser, traverser.skip(0));
                assertTrue(traverser.hasNext());
                final T first = traverser.next();
                final boolean reversed = first == b;
                if (!reversed) {
                    assertSame(a, first);
                }

                assertTrue(traverser.hasNext());
                final T expectedSecond = reversed? a : b;
                assertSame(expectedSecond, traverser.next());
                assertFalse(traverser.hasNext());
            }
        })));
    }

    @Test
    void testSkipOneForTwoElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final Traversable<T> traversable = builder.add(a).add(b).build();
            if (traversable.size() == 2) {
                final Traverser<T> traverser = traversable.iterator();
                assertSame(traverser, traverser.skip(1));
                assertTrue(traverser.hasNext());
                assertSame(traversable.valueAt(1), traverser.next());
                assertFalse(traverser.hasNext());
            }
        })));
    }

    @Test
    void testSkipTwoForTwoElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final Traverser<T> traverser = builder.add(a).add(b).build().iterator();
            assertSame(traverser, traverser.skip(2));
            assertFalse(traverser.hasNext());
        })));
    }

    @Test
    void testSkipThreeForTwoElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final Traverser<T> traverser = builder.add(a).add(b).build().iterator();
            assertSame(traverser, traverser.skip(3));
            assertFalse(traverser.hasNext());
        })));
    }

    @Test
    void testEqualTraverserWhenEmpty() {
        withBuilder(builder -> {
            final Traversable<T> empty = builder.build();
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
            final Traversable<T> empty = builderForEmpty.build();
            withValue(a -> withValue(b -> withBuilder(builder -> {
                final Traversable<T> traversable = builder.add(a).add(b).build();
                assertTrue(traversable.iterator().equalTraverser(traversable.iterator()));
                assertFalse(traversable.iterator().equalTraverser(empty.iterator()));

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

                    assertEquals(expectedResult, traversable.iterator().equalTraverser(that.iterator()));
                });
            })));
        });
    }
}

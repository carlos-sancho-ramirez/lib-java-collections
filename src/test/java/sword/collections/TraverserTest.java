package sword.collections;

import junit.framework.TestCase;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class TraverserTest<T> extends TestCase {

    abstract void withBuilder(Procedure<CollectionBuilder<T>> procedure);
    abstract void withValue(Procedure<T> value);
    abstract void withFilterFunc(Procedure<Predicate<T>> procedure);
    abstract void withReduceFunction(Procedure<ReduceFunction<T>> procedure);

    public void testContainsWhenEmpty() {
        withValue(value -> withBuilder(builder -> {
            if (builder.build().iterator().contains(value)) {
                fail("contains method is expected to return false always for any empty set. " +
                        "But returned true for " + value);
            }
        }));
    }

    public void testContainsWhenContainingASingleElement() {
        withValue(valueIncluded -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(valueIncluded).build();
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

    public void testContainsWhenContainingMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(a).add(b).build();
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

    public void testAnyMatchWhenEmpty() {
        withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.build();
            withFilterFunc(f -> assertFalse(iterable.iterator().anyMatch(f)));
        });
    }

    public void testAnyMatchForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(value).build();
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

    public void testAnyMatchForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(a).add(b).build();
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

    public void testIndexOfWhenEmpty() {
        withBuilder(builder -> {
            IterableCollection<T> iterable = builder.build();
            withValue(value -> assertEquals(-1, iterable.iterator().indexOf(value)));
        });
    }

    public void testIndexOfForSingleElement() {
        withValue(a -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(a).build();
            withValue(value -> {
                final int expected = equal(a, value)? 0 : -1;
                assertEquals(expected, iterable.iterator().indexOf(value));
            });
        }));
    }

    public void testIndexOfForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(a).add(b).build();
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

    public void testValueAtForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final Traverser<T> traverser = builder.add(value).build().iterator();
            assertEquals(value, traverser.valueAt(0));
        }));
    }

    public void testValueAtForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(a).add(b).build();
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

    public void testFindFirstWhenEmpty() {
        withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.build();
            final Predicate<T> predicate = value -> {
                throw new AssertionError("This should not be called");
            };

            withValue(defaultValue -> {
                assertEquals(defaultValue, iterable.iterator().findFirst(predicate, defaultValue));
            });
        });
    }

    public void testFindFirstForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(value).build();
            withValue(defaultValue -> withFilterFunc(f -> {
                final T expected = f.apply(value)? value : defaultValue;
                assertSame(expected, iterable.iterator().findFirst(f, defaultValue));
            }));
        }));
    }

    public void testFindFirstForMultipleElements() {
        withValue(a -> withValue(b -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(a).add(b).build();
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

    public void testReduceForSingleElement() {
        final ReduceFunction<T> func = TraverserTest::unexpectedReduceFunction;
        withValue(value -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(value).build();
            assertSame(value, iterable.iterator().reduce(func));
        }));
    }

    public void testReduceForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(a).add(b).add(c).build();
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

    public void testReduceWithValueWhenEmpty() {
        withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.build();
            final ReduceFunction<T> func = TraverserTest::unexpectedReduceFunction;
            withValue(value -> assertSame(value, iterable.iterator().reduce(func, value)));
        });
    }

    public void testReduceWithValueForSingleElement() {
        final ReduceFunction<T> func = TraverserTest::unexpectedReduceFunction;
        withValue(value -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(value).build();
            withValue(defValue -> assertSame(value, iterable.iterator().reduce(func, defValue)));
        }));
    }

    public void testReduceWithValueForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IterableCollection<T> iterable = builder.add(a).add(b).add(c).build();
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
}

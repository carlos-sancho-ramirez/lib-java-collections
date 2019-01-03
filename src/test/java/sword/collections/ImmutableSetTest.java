package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class ImmutableSetTest<T> extends AbstractIterableImmutableTest<T> {

    abstract boolean lessThan(T a, T b);

    abstract ImmutableSet.Builder<T> newBuilder();

    @Override
    abstract ImmutableSet.Builder<T> newIterableBuilder();

    abstract void withSortFunc(Procedure<SortFunction<T>> procedure);

    public void testSizeForTwoElements() {
        withValue(a -> withValue(b -> {
            final ImmutableSet<T> list = newBuilder().add(a).add(b).build();
            final int size = list.size();
            if (equal(a, b)) {
                assertEquals(1, size);
            }
            else {
                assertEquals(2, size);
            }
        }));
    }

    public void testIteratingForMultipleElements() {
        withValue(a -> withValue(b -> {
            final ImmutableSet<T> set = newBuilder().add(a).add(b).build();
            final Iterator<T> iterator = set.iterator();

            assertTrue(iterator.hasNext());
            final T first = iterator.next();

            if (lessThan(b, a)) {
                assertEquals(b, first);
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.next());
            }
            else {
                assertEquals(a, first);
                if (!equal(a, b)) {
                    assertTrue(iterator.hasNext());
                    assertEquals(b, iterator.next());
                }
            }

            assertFalse(iterator.hasNext());
        }));
    }

    public void testToImmutableForEmpty() {
        final ImmutableSet set = newBuilder().build();
        assertSame(set, set.toImmutable());
    }

    public void testMutateForEmpty() {
        final ImmutableSet<T> set1 = newBuilder().build();
        withValue(value -> {
            final MutableSet<T> set2 = set1.mutate();
            assertTrue(set2.isEmpty());

            set2.add(value);
            assertFalse(set1.contains(value));
        });
    }

    public void testToImmutable() {
        withValue(a -> withValue(b -> {
            final ImmutableSet<T> set = newBuilder().add(a).add(b).build();
            assertSame(set, set.toImmutable());
        }));
    }

    public void testMutate() {
        withValue(a -> withValue(b -> {
            final ImmutableSet<T> set1 = newBuilder().add(a).add(b).build();
            final MutableSet<T> set2 = set1.mutate();

            final Iterator<T> it1 = set1.iterator();
            final Iterator<T> it2 = set2.iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                assertEquals(it1.next(), it2.next());
            }
            assertFalse(it2.hasNext());

            set2.remove(b);
            assertTrue(set1.contains(b));
            assertFalse(set2.contains(b));
        }));
    }

    @Override
    public void testIndexOfForMultipleElements() {
        withValue(a -> withValue(b -> withValue(value -> {
            final IterableCollection<T> set = newIterableBuilder().add(a).add(b).build();
            final int index = set.indexOf(value);

            final int expectedIndex;
            if (lessThan(b, a)) {
                expectedIndex = equal(value, b)? 0 : equal(value, a)? 1 : -1;
            }
            else {
                expectedIndex = equal(value, a)? 0 : equal(value, b)? 1 : -1;
            }
            assertEquals(expectedIndex, index);
        })));
    }

    @Override
    public void testFindFirstForMultipleElements() {
        withFilterFunc(f -> withValue(defaultValue -> withValue(a -> withValue(b -> {
            final IterableCollection<T> collection = newIterableBuilder().add(a).add(b).build();

            final T expected;
            if (lessThan(b, a)) {
                expected = f.apply(b)? b : f.apply(a)? a : defaultValue;
            }
            else {
                expected = f.apply(a)? a : f.apply(b)? b : defaultValue;
            }
            assertSame(expected, collection.findFirst(f, defaultValue));
        }))));
    }

    public void testToListWhenEmpty() {
        final ImmutableSet<T> set = newBuilder().build();
        assertTrue(set.isEmpty());
        assertTrue(set.toList().isEmpty());
    }

    public void testToList() {
        withValue(a -> withValue(b -> {
            final ImmutableSet<T> set = newBuilder().add(a).add(b).build();
            final ImmutableList<T> list = set.toList();

            if (equal(a, b)) {
                assertEquals(1, list.size());
                assertEquals(a, list.get(0));
            }
            else {
                assertEquals(2, list.size());

                if (lessThan(b, a)) {
                    assertEquals(b, list.get(0));
                    assertEquals(a, list.get(1));
                }
                else {
                    assertEquals(a, list.get(0));
                    assertEquals(b, list.get(1));
                }
            }
        }));
    }

    public void testSort() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableSet<T> set = newBuilder().add(a).add(b).add(c).build();
            final int setLength = set.size();
            withSortFunc(f -> {
                final ImmutableSet<T> sortedSet = set.sort(f);
                assertEquals(setLength, sortedSet.size());

                boolean firstElement = true;
                T previousElement = null;

                for (T v : sortedSet) {
                    assertTrue(set.contains(v));
                    if (!firstElement) {
                        assertFalse(f.lessThan(v, previousElement));
                    }
                    firstElement = false;
                }
            });
        })));
    }

    public void testAdd() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableSet<T> set = newBuilder().add(a).add(b).build();
            final ImmutableSet<T> expectedSet = newBuilder().add(a).add(b).add(c).build();
            final ImmutableSet<T> unionSet = set.add(c);
            assertEquals(expectedSet, unionSet);

            if (expectedSet.equals(set)) {
                assertSame(unionSet, set);
            }
        })));
    }

    public void testAddAll() {
        withValue(a -> withValue(b -> withValue(c -> withValue(d -> {
            final ImmutableSet<T> set1 = newBuilder().add(a).add(b).build();
            final ImmutableSet<T> set2 = newBuilder().add(c).add(d).build();
            final ImmutableSet<T> expectedSet = newBuilder().add(a).add(b).add(c).add(d).build();
            final ImmutableSet<T> unionSet = set1.addAll(set2);
            assertEquals(expectedSet, unionSet);

            if (expectedSet.equals(set1)) {
                assertSame(unionSet, set1);
            }
        }))));
    }
}

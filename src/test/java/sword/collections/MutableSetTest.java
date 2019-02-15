package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class MutableSetTest<T> extends AbstractTransformableTest<T> {

    abstract boolean lessThan(T a, T b);
    abstract MutableSet.Builder<T> newBuilder();
    abstract MutableSet.Builder<T> newIterableBuilder();
    abstract void withSortFunc(Procedure<SortFunction<T>> procedure);

    @Override
    void assertEmptyCollection(Transformable<T> collection) {
        assertFalse(collection.iterator().hasNext());
    }

    @Override
    void assertNotChanged(Object expected, Object given) {
        assertEquals(expected, given);
    }

    public void testSizeForTwoElements() {
        withValue(a -> withValue(b -> {
            final MutableSet<T> list = newBuilder().add(a).add(b).build();
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
            final MutableSet<T> set = newBuilder().add(a).add(b).build();
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
        assertTrue(newBuilder().build().toImmutable().isEmpty());
    }

    public void testMutateForEmpty() {
        final MutableSet<T> set1 = newBuilder().build();
        withValue(value -> {
            final MutableSet<T> set2 = set1.mutate();

            assertEquals(set1, set2);
            assertNotSame(set1, set2);

            set1.add(value);
            assertFalse(set2.contains(value));
        });
    }

    public void testToImmutable() {
        withValue(a -> withValue(b -> {
            final MutableSet<T> set = newBuilder().add(a).add(b).build();
            final ImmutableSet<T> set2 = set.toImmutable();

            final Iterator<T> it1 = set.iterator();
            final Iterator<T> it2 = set2.iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                assertEquals(it1.next(), it2.next());
            }
            assertFalse(it2.hasNext());
        }));
    }

    public void testMutate() {
        withValue(a -> withValue(b -> {
            final MutableSet<T> set1 = newBuilder().add(a).add(b).build();
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
            final Traversable<T> set = newIterableBuilder().add(a).add(b).build();
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
            final Traversable<T> collection = newIterableBuilder().add(a).add(b).build();

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

    public void testMapWhenEmpty() {
        withMapFunc(f -> {
            final MutableSet<T> set = newBuilder().build();
            final List<String> mapped = set.map(f);
            assertTrue(mapped.isEmpty());

            withValue(value -> {
                set.clear();
                set.add(value);

                assertEquals(1, mapped.size());
                assertEquals(f.apply(value), mapped.valueAt(0));
            });

            set.clear();
            assertTrue(mapped.isEmpty());
        });
    }

    public void testMapForSingleElement() {
        withMapFunc(f -> withValue(value -> {
            final MutableSet<T> set = newBuilder().add(value).build();
            final List<String> mapped = set.map(f);
            final Iterator<String> iterator = mapped.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next());
            assertFalse(iterator.hasNext());

            set.removeAt(0);
            assertTrue(mapped.isEmpty());
        }));
    }

    public void testMapForMultipleElements() {
        withMapFunc(f -> withValue(a -> withValue(b -> {
            final MutableSet<T> set = newIterableBuilder().add(a).add(b).build();
            final List<String> mapped = set.map(f);

            final Iterator<T> setIterator = set.iterator();
            final Iterator<String> mappedIterator = mapped.iterator();
            while (setIterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(setIterator.next()), mappedIterator.next());
            }

            assertFalse(mappedIterator.hasNext());
        })));
    }

    public void testToListWhenEmpty() {
        final Set<T> set = newBuilder().build();
        assertTrue(set.isEmpty());
        assertTrue(set.toList().isEmpty());
    }

    public void testToList() {
        withValue(a -> withValue(b -> {
            final Set<T> set = newBuilder().add(a).add(b).build();
            final List<T> list = set.toList();

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
            final MutableSet<T> set = newBuilder().add(a).add(b).add(c).build();
            final int setLength = set.size();
            withSortFunc(f -> {
                final Set<T> sortedSet = set.sort(f);
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

    public void testClearWhenEmpty() {
        final MutableSet<T> collection = newBuilder().build();
        assertFalse(collection.clear());
        assertTrue(collection.isEmpty());
    }

    public void testClearForSingleItem() {
        withValue(value -> {
            final MutableSet<T> collection = newBuilder().add(value).build();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        });
    }

    public void testClearForMultipleItems() {
        withValue(a -> withValue(b -> {
            final MutableSet<T> collection = newBuilder().add(a).add(b).build();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        }));
    }
}

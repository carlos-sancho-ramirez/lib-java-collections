package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;

abstract class MutableSetTest<T, B extends MutableSet.Builder<T>> extends SetTest<T, B> implements MutableTraversableTest<T, B> {

    abstract boolean lessThan(T a, T b);
    abstract MutableSet.Builder<T> newBuilder();
    abstract MutableSet.Builder<T> newIterableBuilder();
    abstract void withSortFunc(Procedure<SortFunction<T>> procedure);

    @Test
    void testSizeForTwoElements() {
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

    @Test
    void testIteratingForMultipleElements() {
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

    @Test
    void testToImmutableForEmpty() {
        assertTrue(newBuilder().build().toImmutable().isEmpty());
    }

    @Test
    void testMutateForEmpty() {
        final MutableSet<T> set1 = newBuilder().build();
        withValue(value -> {
            final MutableSet<T> set2 = set1.mutate();

            assertEquals(set1, set2);
            assertNotSame(set1, set2);

            set1.add(value);
            assertFalse(set2.contains(value));
        });
    }

    @Test
    void testToImmutable() {
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

    @Test
    void testMutate() {
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

    @Test
    void testMapWhenEmpty() {
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

    @Test
    void testMapForSingleElement() {
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

    @Test
    void testMapForMultipleElements() {
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

    @Test
    void testSort() {
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

    @Test
    void testEqualsInItems() {
        withValue(a -> withValue(b -> withValue(c -> {
            final Set<T> set = newBuilder().add(a).add(b).add(c).build();
            assertTrue(set.equalSet(set));
            withSortFunc(sortFunction -> {
                final Set<T> sortedSet = set.sort(sortFunction);
                assertTrue(set.equalSet(sortedSet));
                assertTrue(sortedSet.equalSet(set));
            });

            final MutableSet.Builder<T> setBuilder = newBuilder();
            final Iterator<T> it = set.iterator();
            it.next();
            while (it.hasNext()) {
                setBuilder.add(it.next());
            }
            final Set<T> set2 = setBuilder.build();

            assertFalse(set.equalSet(set2));
            assertFalse(set2.equalSet(set));

            withSortFunc(sortFunction -> {
                final Set<T> sortedSet = set.sort(sortFunction);
                assertTrue(set.equalSet(sortedSet));
                assertTrue(sortedSet.equalSet(set));
                assertFalse(set2.equalSet(sortedSet));
                assertFalse(sortedSet.equalSet(set2));
            });

            withSortFunc(sortFunction -> {
                final Set<T> sortedSet = set2.sort(sortFunction);
                assertTrue(set2.equalSet(sortedSet));
                assertTrue(sortedSet.equalSet(set2));
                assertFalse(set.equalSet(sortedSet));
                assertFalse(sortedSet.equalSet(set));
            });
        })));
    }

    @Test
    void testAddAllWhenEmptyAndGivenIsEmpty() {
        final MutableSet<T> set = newBuilder().build();
        assertFalse(set.addAll(ImmutableList.empty()));
        assertTrue(set.isEmpty());
    }

    @Test
    void testAddAllWhenEmptyAndGivenHasOneElement() {
        withValue(a -> {
            final MutableList<T> list = MutableList.empty();
            list.append(a);

            final MutableSet<T> set = newBuilder().build();

            assertTrue(set.addAll(list));
            assertEquals(1, set.size());
            assertSame(a, set.valueAt(0));

            assertFalse(set.addAll(set));
            assertEquals(1, set.size());
            assertSame(a, set.valueAt(0));
        });
    }

    @Test
    void testAddAllWhenEmptyAndGivenHasMultipleElements() {
        withValue(a -> withValue(b -> {
            final int expectedSize = equal(a, b)? 1 : 2;

            final MutableList<T> list = MutableList.empty();
            list.append(a);
            list.append(b);

            final MutableSet<T> set = newBuilder().build();

            assertTrue(set.addAll(list));
            assertEquals(expectedSize, set.size());
            final T first = set.valueAt(0);
            if (expectedSize == 1) {
                assertSame(a, first);
            }
            else if (first == a) {
                assertSame(b, set.valueAt(1));
            }
            else {
                assertSame(b, first);
                assertSame(a, set.valueAt(1));
            }

            assertFalse(set.addAll(set));
        }));
    }

    @Test
    void testAddAllWhenHasOneElementAndGivenIsEmpty() {
        withValue(a -> {
            final MutableSet<T> set = newBuilder().add(a).build();

            assertFalse(set.addAll(ImmutableList.empty()));
            assertEquals(1, set.size());
            assertSame(a, set.valueAt(0));
        });
    }

    @Test
    void testAddAllWhenHasOneElementAndGivenHasOneElement() {
        withValue(a -> withValue(b -> {
            final MutableSet<T> set = newBuilder().add(a).build();
            final ImmutableList<T> list = ImmutableList.<T>empty().append(b);

            final boolean included = !equal(a, b);
            assertEquals(included, set.addAll(list));

            final T first = set.valueAt(0);
            if (included) {
                assertEquals(2, set.size());
                final T second = set.valueAt(1);
                if (first == a) {
                    assertSame(b, second);
                }
                else {
                    assertSame(b, first);
                    assertSame(a, second);
                }
            }
            else {
                assertEquals(1, set.size());
                assertSame(a, set.valueAt(0));
            }
        }));
    }

    private void checkContainsOnlyAfterAddAll(T a, T b, T c, MutableSet<T> set) {
        final T first = set.valueAt(0);
        if (equal(a, b)) {
            if (equal(a, c)) {
                assertEquals(1, set.size());
                assertSame(a, first);
            }
            else {
                assertEquals(2, set.size());
                if (a == first) {
                    assertSame(c, set.valueAt(1));
                }
                else {
                    assertSame(c, first);
                    assertSame(a, set.valueAt(1));
                }
            }
        }
        else if (equal(a, c) || equal(b, c)) {
            assertEquals(2, set.size());
            if (a == first) {
                assertSame(b, set.valueAt(1));
            }
            else {
                assertSame(b, first);
                assertSame(a, set.valueAt(1));
            }
        }
        else {
            assertEquals(3, set.size());
            final T second = set.valueAt(1);
            final T third = set.valueAt(2);

            if (a == first) {
                if (b == second) {
                    assertSame(c, third);
                }
                else {
                    assertSame(c, second);
                    assertSame(b, third);
                }
            }
            else if (b == first) {
                if (a == second) {
                    assertSame(c, third);
                }
                else {
                    assertSame(c, second);
                    assertSame(a, third);
                }
            }
            else {
                assertSame(c, first);
                if (a == second) {
                    assertSame(b, third);
                }
                else {
                    assertSame(b, second);
                    assertSame(a, third);
                }
            }
        }
    }

    @Test
    void testAddAllWhenHasOneElementAndGivenHasMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> {
            final MutableSet<T> set = newBuilder().add(a).build();
            final ImmutableList<T> list = ImmutableList.<T>empty().append(b).append(c);

            final boolean setChanged = !equal(a, b) || !equal(a, c);
            assertEquals(setChanged, set.addAll(list));
            checkContainsOnlyAfterAddAll(a, b, c, set);
        })));
    }

    @Test
    void testAddAllWhenHasMultipleElementsAndGivenHasOneElement() {
        withValue(a -> withValue(b -> withValue(c -> {
            final MutableSet<T> set = newBuilder().add(a).add(b).build();
            final ImmutableList<T> list = ImmutableList.<T>empty().append(c);

            final boolean setChanged = !equal(a, c) && !equal(b, c);
            assertEquals(setChanged, set.addAll(list));
            checkContainsOnlyAfterAddAll(a, b, c, set);
        })));
    }
}

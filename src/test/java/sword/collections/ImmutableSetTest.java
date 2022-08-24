package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;

abstract class ImmutableSetTest<T, B extends ImmutableSet.Builder<T>> extends SetTest<T, B> implements ImmutableTransformableTest<T, B> {

    abstract boolean lessThan(T a, T b);

    abstract void withSortFunc(Procedure<SortFunction<T>> procedure);

    @Test
    void testSizeForTwoElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().add(a).add(b).build();
            final int size = set.size();
            if (equal(a, b)) {
                assertEquals(1, size);
            }
            else {
                assertEquals(2, size);
            }
        })));
    }

    @Test
    void testIteratingForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().add(a).add(b).build();
            final Iterator<T> iterator = set.iterator();

            assertTrue(iterator.hasNext());
            final T first = iterator.next();

            if (!equal(a, b)) {
                if (equal(b, first)) {
                    assertTrue(iterator.hasNext());
                    assertEquals(a, iterator.next());
                }
                else {
                    assertEquals(a, first);
                    assertTrue(iterator.hasNext());
                    assertEquals(b, iterator.next());
                }
            }
            else {
                assertEquals(a, first);
            }

            assertFalse(iterator.hasNext());
        })));
    }

    @Test
    void testToImmutableForEmpty() {
        withBuilderSupplier(supplier -> {
            final ImmutableSet set = supplier.newBuilder().build();
            assertSame(set, set.toImmutable());
        });
    }

    @Test
    void testMutateForEmpty() {
        withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set1 = supplier.newBuilder().build();
            withValue(value -> {
                final MutableSet<T> set2 = set1.mutate();
                assertTrue(set2.isEmpty());

                set2.add(value);
                assertFalse(set1.contains(value));
            });
        });
    }

    @Test
    void testToImmutable() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().add(a).add(b).build();
            assertSame(set, set.toImmutable());
        })));
    }

    @Test
    void testMutate() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set1 = supplier.newBuilder().add(a).add(b).build();
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
        })));
    }

    @Test
    void testSort() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
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
        }))));
    }

    @Test
    void testAdd() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().add(a).add(b).build();
            final ImmutableSet<T> expectedSet = supplier.newBuilder().add(a).add(b).add(c).build();
            final ImmutableSet<T> unionSet = set.add(c);
            assertEquals(expectedSet, unionSet);

            if (expectedSet.equals(set)) {
                assertSame(unionSet, set);
            }
        }))));
    }

    @Test
    void testAddAll() {
        withValue(a -> withValue(b -> withValue(c -> withValue(d -> withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set1 = supplier.newBuilder().add(a).add(b).build();
            final ImmutableSet<T> set2 = supplier.newBuilder().add(c).add(d).build();
            final ImmutableSet<T> expectedSet = supplier.newBuilder().add(a).add(b).add(c).add(d).build();
            final ImmutableSet<T> unionSet = set1.addAll(set2);
            assertEquals(expectedSet, unionSet);

            if (expectedSet.equals(set1)) {
                assertSame(unionSet, set1);
            }
        })))));
    }

    void withTraversableBuilderSupplier(Procedure<BuilderSupplier<T, TraversableBuilder<T>>> procedure) {
        procedure.apply(ImmutableHashSet.Builder::new);
        procedure.apply(MutableHashSet.Builder::new);
        withSortFunc(sortFunc -> {
            procedure.apply(() -> new ImmutableSortedSet.Builder<>(sortFunc));
            procedure.apply(() -> new MutableSortedSet.Builder<>(sortFunc));
        });
        procedure.apply(ImmutableList.Builder::new);
        procedure.apply(MutableList.Builder::new);
        // TODO: Include maps
    }

    @Test
    void testEqualSet() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final Set<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            assertTrue(set.equalSet(set));
            withSortFunc(sortFunction -> {
                final Set<T> sortedSet = set.sort(sortFunction);
                assertTrue(set.equalSet(sortedSet));
                assertTrue(sortedSet.equalSet(set));
            });

            final ImmutableSet.Builder<T> setBuilder = supplier.newBuilder();
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
        }))));
    }

    @Test
    void testRemoveForEmptySet() {
        withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().build();
            withValue(value -> assertSame(set, set.remove(value), "Removing on an empty set should always return the same set"));
        });
    }

    @Test
    void testRemoveForASingleElement() {
        withValue(included -> withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().add(included).build();
            withValue(value -> {
                if (equal(included, value)) {
                    final ImmutableSet<T> emptySet = set.remove(value);
                    final String msg = "Removing value " + value + " from set containing only that value should return an empty set";
                    assertNotSame(set, emptySet, msg);
                    assertTrue(emptySet.isEmpty(), msg);
                }
                else {
                    assertSame(set, set.remove(value), "Removing an element that is not included in the set should always return the same set");
                }
            });
        }));
    }

    @Test
    @Override
    public void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final T first = set.valueAt(0);
            final T second = (size >= 2)? set.valueAt(1) : null;
            final T third = (size >= 3)? set.valueAt(2) : null;

            final ImmutableSet<T> sliceA = set.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertSame(first, sliceA.valueAt(0));

            final ImmutableSet<T> sliceB = set.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertSame(second, sliceB.valueAt(0));
            }
            else {
                assertEquals(0, sliceB.size());
            }

            final ImmutableSet<T> sliceC = set.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertSame(third, sliceC.valueAt(0));
            }
            else {
                assertEquals(0, sliceC.size());
            }

            final ImmutableSet<T> sliceAB = set.slice(new ImmutableIntRange(0, 1));
            if (size >= 2) {
                assertEquals(2, sliceAB.size());
                assertSame(second, sliceAB.valueAt(1));
            }
            else {
                assertEquals(1, sliceAB.size());
            }
            assertEquals(first, sliceAB.valueAt(0));

            final ImmutableSet<T> sliceBC = set.slice(new ImmutableIntRange(1, 2));
            if (size == 1) {
                assertEquals(0, sliceBC.size());
            }
            else if (size == 2) {
                assertEquals(1, sliceBC.size());
                assertSame(second, sliceBC.valueAt(0));
            }
            else {
                assertEquals(2, sliceBC.size());
                assertSame(second, sliceBC.valueAt(0));
                assertSame(third, sliceBC.valueAt(1));
            }

            final ImmutableSet<T> sliceABC = set.slice(new ImmutableIntRange(0, 2));
            assertEquals(size, sliceABC.size());
            assertSame(first, sliceABC.valueAt(0));
            if (size >= 2) {
                assertSame(second, sliceABC.valueAt(1));
                if (size >= 3) {
                    assertSame(third, sliceABC.valueAt(2));
                }
            }

            final ImmutableSet<T> sliceABCD = set.slice(new ImmutableIntRange(0, 3));
            assertEquals(size, sliceABCD.size());
            assertSame(first, sliceABCD.valueAt(0));
            if (size >= 2) {
                assertSame(second, sliceABCD.valueAt(1));
                if (size >= 3) {
                    assertSame(third, sliceABCD.valueAt(2));
                }
            }
        }))));
    }

    @Test
    @Override
    public void testSkip() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final T second = (size >= 2)? set.valueAt(1) : null;
            final T third = (size == 3)? set.valueAt(2) : null;

            assertSame(set, set.skip(0));

            final ImmutableSet<T> skip1 = set.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertSame(second, skip1.valueAt(0));
                if (size == 3) {
                    assertSame(third, skip1.valueAt(1));
                }
            }

            final ImmutableSet<T> empty = ImmutableHashSet.empty();
            final ImmutableSet<T> skip2 = set.skip(2);
            if (size == 3) {
                assertSame(third, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertSame(empty, skip2);
            }

            assertSame(empty, set.skip(3));
            assertSame(empty, set.skip(4));
            assertSame(empty, set.skip(24));
        }))));
    }

    @Test
    @Override
    public void testTakeWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().build();
            assertSame(set, set.take(0));
            assertSame(set, set.take(1));
            assertSame(set, set.take(2));
            assertSame(set, set.take(24));
        });
    }

    @Test
    @Override
    public void testTake() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final T first = set.valueAt(0);

            assertTrue(set.take(0).isEmpty());

            final ImmutableSet<T> take1 = set.take(1);
            if (size > 1) {
                assertEquals(1, take1.size());
                assertSame(first, take1.valueAt(0));
            }
            else {
                assertSame(set, take1);
            }

            final ImmutableSet<T> take2 = set.take(2);
            if (size > 2) {
                assertEquals(2, take2.size());
                assertSame(first, take2.valueAt(0));
                assertSame(set.valueAt(1), take2.valueAt(1));
            }
            else {
                assertSame(set, take2);
            }

            assertSame(set, set.take(3));
            assertSame(set, set.take(4));
            assertSame(set, set.take(24));
        }))));
    }

    @Test
    @Override
    public void testSkipLastWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().build();
            assertSame(set, set.skipLast(0));
            assertSame(set, set.skipLast(1));
            assertSame(set, set.skipLast(2));
            assertSame(set, set.skipLast(24));
        });
    }

    @Test
    @Override
    public void testSkipLast() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableSet<T> set = supplier.newBuilder().add(a).add(b).add(c).build();
            assertSame(set, set.skipLast(0));

            final int size = set.size();
            final T first = set.valueAt(0);
            final T second = (size >= 2)? set.valueAt(1) : null;

            final ImmutableSet<T> set1 = set.skipLast(1);
            assertEquals(size - 1, set1.size());
            if (size >= 2) {
                assertSame(first, set1.valueAt(0));
                if (size == 3) {
                    assertSame(second, set1.valueAt(1));
                }
            }

            final ImmutableSet<T> set2 = set.skipLast(2);
            if (size < 3) {
                assertTrue(set2.isEmpty());
            }
            else {
                assertEquals(1, set2.size());
                assertSame(first, set2.valueAt(0));
            }

            assertTrue(set.skipLast(3).isEmpty());
            assertTrue(set.skipLast(4).isEmpty());
            assertTrue(set.skipLast(24).isEmpty());
        }))));
    }
}

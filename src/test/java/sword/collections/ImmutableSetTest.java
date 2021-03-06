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

    private void withTraversableBuilderSupplier(Procedure<BuilderSupplier<T, TraversableBuilder<T>>> procedure) {
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
    void testEquals() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(setSupplier -> withTraversableBuilderSupplier(trSupplier -> {
            final ImmutableSet<T> set = setSupplier.newBuilder().add(a).add(b).add(c).build();
            final TraversableBuilder<T> builder = trSupplier.newBuilder();
            for (T item : set) {
                builder.add(item);
            }
            final Traversable<T> traversable = builder.build();
            final Traverser<T> traverser = traversable.iterator();
            boolean sameOrderAndSize = true;
            for (T item : set) {
                if (!traverser.hasNext() || !equal(item, traverser.next())) {
                    sameOrderAndSize = false;
                    break;
                }
            }

            if (traverser.hasNext()) {
                sameOrderAndSize = false;
            }

            assertEquals(sameOrderAndSize, set.equals(traversable));
        })))));
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
}

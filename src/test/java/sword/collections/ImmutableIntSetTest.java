package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class ImmutableIntSetTest extends AbstractImmutableIntIterableTest {

    abstract ImmutableIntSet.Builder newIntBuilder();

    @Override
    AbstractImmutableIntSet emptyCollection() {
        return ImmutableIntSetImpl.empty();
    }

    private boolean isPositiveValue(int value) {
        return value >= 0;
    }

    @Override
    void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::isPositiveValue);
    }

    @Override
    void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    @Override
    ImmutableSet<String> mapTargetEmptyCollection() {
        return ImmutableSet.empty();
    }

    public void testSizeForMultipleElements() {
        withItem(a -> withItem(b -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).build();
            if (a == b) {
                assertEquals("Expected size 1 after building it adding twice value " + a, 1, set.size());
            }
            else {
                assertEquals("Expected size 2 after building it adding two different values " + a + " and " + b, 2, set.size());
            }
        }));
    }

    public void testIteratingForMultipleElements() {
        withItem(a -> withItem(b -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> iterator = set.iterator();

            assertTrue(iterator.hasNext());
            final int first = iterator.next();

            if (a == b) {
                assertFalse(iterator.hasNext());
                assertEquals(a, first);
            }
            else {
                assertTrue(iterator.hasNext());
                final int second = iterator.next();

                assertFalse(iterator.hasNext());
                if (a < b) {
                    assertEquals(a, first);
                    assertEquals(b, second);
                }
                else {
                    assertEquals(a, second);
                    assertEquals(b, first);
                }
            }
        }));
    }

    public void testMapForMultipleElements() {
        withMapFunc(f -> withItem(a -> withItem(b -> {
            final ImmutableIntSet collection = newIntBuilder().add(a).add(b).build();
            final ImmutableSet<String> mapped = collection.map(f);
            final Iterator<String> iterator = mapped.iterator();

            final String mappedA = f.apply(a);
            final String mappedB = f.apply(b);

            assertTrue(iterator.hasNext());
            final boolean sameMappedValue = equal(mappedA, mappedB);
            final String first = iterator.next();

            if (sameMappedValue) {
                assertEquals(mappedA, first);
            }
            else if (equal(mappedA, first)) {
                assertTrue(iterator.hasNext());
                assertEquals(mappedB, iterator.next());
            }
            else if (equal(mappedB, first)) {
                assertTrue(iterator.hasNext());
                assertEquals(mappedA, iterator.next());
            }
            else {
                fail("Expected either " + mappedA + " or " + mappedB + " but found " + first);
            }

            assertFalse(iterator.hasNext());
        })));
    }

    public void testAdd() {
        withItem(a -> withItem(b -> {
            ImmutableIntSet set = newIntBuilder().build();
            set = set.add(a);
            assertFalse(set.isEmpty());

            if (a == b) {
                assertSame(set, set.add(b));
                assertTrue(set.contains(b));
            }
            else {
                set = set.add(b);
                assertEquals(2, set.size());
                assertTrue(set.contains(a));
                assertTrue(set.contains(b));
            }
        }));
    }

    public void testRemoveForEmptySet() {
        final ImmutableIntSet set = newIntBuilder().build();
        withItem(value -> {
            assertSame("Removing on an empty set should always return the same set", set, set.remove(value));
        });
    }

    public void testRemoveForASingleElement() {
        withItem(included -> {
            final ImmutableIntSet set = newIntBuilder().add(included).build();
            withItem(value -> {
                if (included == value) {
                    final ImmutableIntSet emptySet = set.remove(value);
                    final String msg = "Removing value " + value + " from set containing only that value should return an empty set";
                    assertNotSame(msg, set, emptySet);
                    assertTrue(msg, emptySet.isEmpty());
                }
                else {
                    assertSame("Removing an element that is not included in the set should always return the same set",
                            set, set.remove(value));
                }
            });
        });
    }

    public void testMin() {
        withItem(a -> withItem(b -> withItem(c -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).add(c).build();
            final int min = Math.min(Math.min(a, b), c);
            assertEquals(min, set.min());
        })));
    }

    public void testMax() {
        withItem(a -> withItem(b -> withItem(c -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).add(c).build();
            final int max = Math.max(Math.max(a, b), c);
            assertEquals(max, set.max());
        })));
    }

    public void testToImmutableMethodReturnSameInstance() {
        withItem(a -> withItem(b -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).build();
            assertSame(set, set.toImmutable());
        }));
    }

    public void testMutate() {
        withItem(a -> withItem(b -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).build();
            final MutableIntSet set2 = set.mutate();

            assertEquals(set.size(), set2.size());
            for (int value : set) {
                assertTrue(set2.contains(value));
            }
        }));
    }
}

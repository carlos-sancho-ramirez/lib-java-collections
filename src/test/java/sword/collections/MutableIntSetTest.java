package sword.collections;

import java.util.Iterator;

public class MutableIntSetTest extends AbstractIntIterableTest {

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE, -500, -2, -1, 0, 1, 3, 127, 128, Integer.MAX_VALUE
    };

    @Override
    MutableIntSet.Builder newIntBuilder() {
        return new MutableIntSet.Builder();
    }

    @Override
    void withItem(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    @Override
    MutableIntSet emptyCollection() {
        return new MutableIntSet();
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
            final MutableIntSet set = newIntBuilder().add(a).add(b).build();
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
            final MutableIntSet set = newIntBuilder().add(a).add(b).build();
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

    public void testAdd() {
        withItem(a -> withItem(b -> {
            final MutableIntSet set = new MutableIntSet();
            assertTrue(set.add(a));
            assertFalse(set.isEmpty());

            if (a == b) {
                assertFalse(set.add(b));
                assertEquals(1, set.size());
                assertTrue(set.contains(b));
            }
            else {
                assertTrue(set.add(b));
                assertEquals(2, set.size());
                assertTrue(set.contains(a));
                assertTrue(set.contains(b));
            }
        }));
    }

    public void testAddAll() {
        withItem(a -> withItem(b -> {
            final ImmutableIntSet values = new ImmutableIntSetBuilder().add(a).add(b).build();
            withItem(c -> {
                final MutableIntSet set = new MutableIntSet();
                set.add(c);

                if (c == a && c == b) {
                    assertFalse(set.addAll(values));
                    assertEquals(1, set.size());
                    assertTrue(set.contains(c));
                }
                else {
                    assertTrue(set.addAll(values));
                    assertTrue(set.contains(a));
                    assertTrue(set.contains(b));
                    assertTrue(set.contains(c));
                    if (a == b || a == c || b == c) {
                        assertEquals(2, set.size());
                    }
                    else {
                        assertEquals(3, set.size());
                    }
                }
            });
        }));
    }

    public void testRemoveForEmptySet() {
        final MutableIntSet set = newIntBuilder().build();
        withItem(value -> {
            assertFalse(set.remove(value));
            assertTrue(set.isEmpty());
        });
    }

    public void testRemoveForASingleElement() {
        withItem(included -> {
            withItem(value -> {
                final MutableIntSet set = newIntBuilder().add(included).build();
                if (included == value) {
                    assertTrue(set.remove(value));
                    assertTrue(set.isEmpty());
                }
                else {
                    assertFalse(set.remove(value));
                    assertFalse(set.isEmpty());
                }
            });
        });
    }

    public void testMin() {
        withItem(a -> withItem(b -> withItem(c -> {
            final MutableIntSet set = new MutableIntSet.Builder().add(a).add(b).add(c).build();
            final int min = Math.min(Math.min(a, b), c);
            assertEquals(min, set.min());
        })));
    }

    public void testMax() {
        withItem(a -> withItem(b -> withItem(c -> {
            final MutableIntSet set = new MutableIntSet.Builder().add(a).add(b).add(c).build();
            final int max = Math.max(Math.max(a, b), c);
            assertEquals(max, set.max());
        })));
    }

    public void testToImmutableMethodReturnSameInstance() {
        withItem(a -> withItem(b -> {
            final MutableIntSet set = new MutableIntSet.Builder().add(a).add(b).build();
            final ImmutableIntSet set2 = set.toImmutable();
            assertEquals(set.size(), set2.size());
            for (int value : set) {
                assertTrue(set2.contains(value));
            }
        }));
    }

    public void testMutate() {
        withItem(a -> withItem(b -> {
            final MutableIntSet set = new MutableIntSet.Builder().add(a).add(b).build();
            withItem(c -> {
                final MutableIntSet set2 = set.mutate();
                assertNotSame(set, set2);
                set2.add(c);
                if (a == c || b == c) {
                    assertEquals(set.size(), set2.size());
                }
                else {
                    assertEquals(set.size() + 1, set2.size());
                    assertTrue(set2.contains(c));
                    assertFalse(set.contains(c));
                }

                for (int value : set) {
                    assertTrue(set2.contains(value));
                }
            });
        }));
    }
}

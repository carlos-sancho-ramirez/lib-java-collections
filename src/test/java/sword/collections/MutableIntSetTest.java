package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public final class MutableIntSetTest extends IntTransformableTest<MutableIntSet.Builder> implements MutableIntTraversableTest<MutableIntSet.Builder> {

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE, -500, -2, -1, 0, 1, 3, 127, 128, Integer.MAX_VALUE
    };

    @Override
    MutableIntSet.Builder newIntBuilder() {
        return new MutableIntSet.Builder();
    }

    @Override
    public void withValue(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
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
    void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v * v);
        procedure.apply(v -> v + 1);
    }

    @Test
    public void testIteratingForMultipleElements() {
        withValue(a -> withValue(b -> {
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

    @Test
    public void testAdd() {
        withValue(a -> withValue(b -> {
            final MutableIntSet set = MutableIntSet.empty();
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

    @Test
    public void testAddAll() {
        withValue(a -> withValue(b -> {
            final ImmutableIntSet values = new ImmutableIntSetCreator().add(a).add(b).build();
            withValue(c -> {
                final MutableIntSet set = MutableIntSet.empty();
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

    @Test
    public void testRemoveForEmptySet() {
        final MutableIntSet set = newIntBuilder().build();
        withValue(value -> {
            assertFalse(set.remove(value));
            assertTrue(set.isEmpty());
        });
    }

    @Test
    public void testRemoveForASingleElement() {
        withValue(included -> {
            withValue(value -> {
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

    @Test
    public void testValueAt() {
        withValue(a -> withValue(b -> withValue(c -> {
            final MutableIntSet set = newIntBuilder().add(a).add(b).add(c).build();
            final Iterator<Integer> it = set.iterator();
            int index = 0;
            while (it.hasNext()) {
                assertEquals(set.valueAt(index++), it.next().intValue());
            }
        })));
    }

    @Test
    public void testToImmutableMethodReturnSameInstance() {
        withValue(a -> withValue(b -> {
            final MutableIntSet set = new MutableIntSet.Builder().add(a).add(b).build();
            final ImmutableIntSet set2 = set.toImmutable();
            assertEquals(set.size(), set2.size());
            for (int value : set) {
                assertTrue(set2.contains(value));
            }
        }));
    }

    @Test
    public void testMutate() {
        withValue(a -> withValue(b -> {
            final MutableIntSet set = new MutableIntSet.Builder().add(a).add(b).build();
            withValue(c -> {
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

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<MutableIntSet.Builder>> procedure) {
        procedure.apply(MutableIntSet.Builder::new);
    }

    @Test
    public void testEqualSet() {
        withValue(a -> withValue(b -> withValue(c -> {
            final IntSet set = newIntBuilder().add(a).add(b).add(c).build();
            assertTrue(set.equalSet(set));

            final MutableIntSet reducedSet = set.mutate();
            reducedSet.removeAt(0);

            assertFalse(set.equalSet(reducedSet));
            assertFalse(reducedSet.equalSet(set));

            assertTrue(set.equalSet(set.toImmutable()));
        })));
    }
}

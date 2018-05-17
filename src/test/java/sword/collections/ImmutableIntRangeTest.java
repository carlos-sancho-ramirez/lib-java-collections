package sword.collections;

import junit.framework.TestCase;

import java.util.Iterator;

public class ImmutableIntRangeTest extends TestCase {

    // This is used in some tests that iterates in a range instance as the maximum allowed size.
    // If the size is exceeded, then that range instance is not tested.
    // This is mainly to avoid spending too much time executing the tests
    private static final int RANGE_SIZE_THRESHOLD = 10;

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE,
            Integer.MIN_VALUE + RANGE_SIZE_THRESHOLD - 3,
            -RANGE_SIZE_THRESHOLD,
            -2, 0, 1, 3,
            RANGE_SIZE_THRESHOLD,
            Integer.MAX_VALUE - RANGE_SIZE_THRESHOLD + 3,
            Integer.MAX_VALUE
    };

    private void withValue(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    private void withRange(Procedure<ImmutableIntRange> procedure) {
        for (int min : INT_VALUES) {
            for (int max : INT_VALUES) {
                if (min <= max) {
                    procedure.apply(new ImmutableIntRange(min, max));
                }
            }
        }
    }

    private void withSmallRange(Procedure<ImmutableIntRange> procedure) {
        for (int min : INT_VALUES) {
            for (int max : INT_VALUES) {
                if (min <= max && max <= min + RANGE_SIZE_THRESHOLD - 1) {
                    procedure.apply(new ImmutableIntRange(min, max));
                }
            }
        }
    }

    public void testMinMaxAndSizeConsistency() {
        withRange(range -> {
            assertEquals(range.size(), range.max() - range.min() + 1);
        });
    }

    public void testContains() {
        withRange(range -> withValue(value -> {
            if (value >= range.min() && value <= range.max()) {
                assertTrue(range.contains(value));
            }
            else {
                assertFalse(range.contains(value));
            }
        }));
    }

    public void testIteration() {
        withSmallRange(range -> {
            final int max = range.max();
            final Iterator<Integer> it = range.iterator();
            for (int value = range.min(); value <= max; value++) {
                assertTrue(it.hasNext());
                assertEquals(value, it.next().intValue());
            }

            assertFalse(it.hasNext());
        });
    }

    public void testToImmutable() {
        withRange(range -> {
            assertSame(range, range.toImmutable());
        });
    }

    public void testMutable() {
        withSmallRange(range -> {
            final MutableIntSet set = range.mutate();
            assertEquals(range.size(), set.size());
            assertEquals(range.min(), set.min());
            assertEquals(range.max(), set.max());
        });
    }

    public void testAdd() {
        withSmallRange(range -> withValue(value -> {
            final ImmutableIntSet set = range.add(value);

            if (range.contains(value)) {
                assertSame(range, set);
            }
            else {
                assertEquals(range.size() + 1, set.size());
                for (int v : range) {
                    assertTrue(set.contains(v));
                }
                assertTrue(set.contains(value));
            }
        }));
    }

    public void testRemove() {
        withSmallRange(range -> withValue(value -> {
            final ImmutableIntSet set = range.remove(value);

            if (!range.contains(value)) {
                assertSame(range, set);
            }
            else {
                assertEquals(range.size() - 1, set.size());
                for (int v : range) {
                    if (value != v) {
                        assertTrue(set.contains(v));
                    }
                    else {
                        assertFalse(set.contains(value));
                    }
                }
            }
        }));
    }

    public void testToList() {
        for (int min : INT_VALUES) {
            for (int i = 0; i < 3; i++) {
                final int max = min + i;
                if (max > min) {
                    final ImmutableIntRange set = new ImmutableIntRange(min, max);
                    final Iterator<Integer> listIt = set.toList().iterator();

                    for (int value : set) {
                        assertTrue(listIt.hasNext());
                        assertEquals(value, (int) listIt.next());
                    }
                    assertFalse(listIt.hasNext());
                }
            }
        }
    }
}

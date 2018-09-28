package sword.collections;

import junit.framework.TestCase;

public final class MutableIntTreeSetTest extends TestCase {

    private final int[] intValues = {
            Integer.MIN_VALUE, -100, -2, -1, 0, 1, 2, 5, Integer.MAX_VALUE
    };

    MutableIntTreeSet.Builder newBuilder() {
        return new MutableIntTreeSet.Builder();
    }

    public void withValue(IntProcedure procedure) {
        for (int value : intValues) {
            procedure.apply(value);
        }
    }

    public void testContainsForEmpty() {
        final MutableIntTreeSet set = newBuilder().build();
        withValue(value -> assertFalse(set.contains(value)));
    }

    public void testContainsForSingleValue() {
        withValue(a -> {
            final MutableIntTreeSet set = newBuilder().add(a).build();
            withValue(value -> {
                if (a == value) {
                    assertTrue(set.contains(value));
                }
                else {
                    assertFalse(set.contains(value));
                }
            });
        });
    }

    public void testContainsForMultipleValues() {
        withValue(a -> withValue(b-> {
            final MutableIntTreeSet set = newBuilder().add(a).add(b).build();
            withValue(value -> {
                if (a == value || b == value) {
                    assertTrue(set.contains(value));
                }
                else {
                    assertFalse(set.contains(value));
                }
            });
        }));
    }
}

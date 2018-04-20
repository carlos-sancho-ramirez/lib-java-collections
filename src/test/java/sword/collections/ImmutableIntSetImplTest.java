package sword.collections;

import static sword.collections.SortUtils.equal;

public class ImmutableIntSetImplTest extends ImmutableIntSetTest {

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE, -500, -2, -1, 0, 1, 3, 127, 128, Integer.MAX_VALUE
    };

    @Override
    ImmutableIntSetImpl.Builder newIntBuilder() {
        return new ImmutableIntSetImpl.Builder();
    }

    @Override
    void withItem(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    @Override
    public void testIndexOfForMultipleElements() {
        withItem(a -> withItem(b -> withItem(value -> {
            final IterableIntCollection list = newIntBuilder().add(a).add(b).build();
            final int index = list.indexOf(value);

            if (a <= b && equal(a, value) || b < a && equal(b, value)) {
                assertEquals(0, index);
            }
            else if (a <= b && equal(b, value) || b < a && equal(a, value)) {
                assertEquals(1, index);
            }
            else {
                assertEquals(-1, index);
            }
        })));
    }
}

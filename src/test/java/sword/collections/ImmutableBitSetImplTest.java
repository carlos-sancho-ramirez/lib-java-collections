package sword.collections;

import static sword.collections.SortUtils.equal;

public class ImmutableBitSetImplTest extends ImmutableIntSetTest {

    private static final int[] INT_VALUES = {
            0, 1, 2, 3, 31, 32, 33, 127, 128
    };

    @Override
    void withItem(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    @Override
    ImmutableBitSetImpl.Builder newIntBuilder() {
        return new ImmutableBitSetImpl.Builder();
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

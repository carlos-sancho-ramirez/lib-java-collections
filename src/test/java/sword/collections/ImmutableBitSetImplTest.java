package sword.collections;

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
}

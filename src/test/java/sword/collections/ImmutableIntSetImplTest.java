package sword.collections;

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
}

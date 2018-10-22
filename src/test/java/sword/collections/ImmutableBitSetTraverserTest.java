package sword.collections;

public final class ImmutableBitSetTraverserTest extends IntTransformerTest<ImmutableIntSet, ImmutableBitSetImpl.Builder> {

    private static final int[] INT_VALUES = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 13, 17, 22, 46, 124, 1023
    };

    @Override
    void withBuilder(Procedure<ImmutableBitSetImpl.Builder> procedure) {
        procedure.apply(new ImmutableBitSetImpl.Builder());
    }

    @Override
    void withValue(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }
}

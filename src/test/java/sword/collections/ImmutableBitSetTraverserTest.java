package sword.collections;

public final class ImmutableBitSetTraverserTest extends IntTraverserTest<ImmutableIntSet> {

    private static final int[] INT_VALUES = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 13, 17, 22, 46, 124, 1023
    };

    @Override
    void withBuilder(Procedure<IntCollectionBuilder<ImmutableIntSet>> procedure) {
        procedure.apply(new ImmutableBitSetImpl.Builder());
    }

    @Override
    void withValue(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }
}

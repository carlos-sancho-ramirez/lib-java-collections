package sword.collections;

public final class ImmutableIntArraySetTest extends ImmutableIntSetTest {

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE, -500, -2, -1, 0, 1, 3, 127, 128, Integer.MAX_VALUE
    };

    @Override
    ImmutableIntArraySet.Builder newIntBuilder() {
        return new ImmutableIntArraySet.Builder();
    }

    @Override
    public void withValue(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntSet.Builder>> procedure) {
        procedure.apply(ImmutableIntArraySet.Builder::new);
    }
}

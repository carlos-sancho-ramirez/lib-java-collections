package sword.collections;

import static org.junit.jupiter.api.Assertions.assertFalse;

public final class ImmutableBitSetImplTest extends ImmutableIntSetTest {

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
    void assertEmptyCollection(IntTransformable transformable) {
        assertFalse(transformable.iterator().hasNext());
    }

    @Override
    public void withTransformableBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntSet, ImmutableIntTransformableBuilder<ImmutableIntSet>>> procedure) {
        procedure.apply(ImmutableBitSetImpl.Builder::new);
    }
}

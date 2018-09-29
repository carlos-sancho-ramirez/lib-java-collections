package sword.collections;

public final class MutableIntTreeSetTest extends AbstractIntIterableTest {

    private final int[] intValues = {
            Integer.MIN_VALUE, -100, -2, -1, 0, 1, 2, 5, Integer.MAX_VALUE
    };

    @Override
    AbstractIntIterable emptyCollection() {
        return ImmutableIntSetImpl.empty();
    }

    @Override
    IntCollectionBuilder newIntBuilder() {
        return new MutableIntTreeSet.Builder();
    }

    @Override
    void withItem(IntProcedure procedure) {
        for (int value : intValues) {
            procedure.apply(value);
        }
    }

    private static boolean evenIntFilter(int value) {
        return (value & 1) != 0;
    }

    @Override
    void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(MutableIntTreeSetTest::evenIntFilter);
    }
}

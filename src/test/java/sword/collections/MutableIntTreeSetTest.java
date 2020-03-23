package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MutableIntTreeSetTest extends IntSetTest<MutableIntTreeSet.Builder> implements MutableIntSetTest<MutableIntTreeSet.Builder> {

    private final int[] intValues = {
            Integer.MIN_VALUE, -100, -2, -1, 0, 1, 2, 5, Integer.MAX_VALUE
    };

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<MutableIntTreeSet.Builder>> procedure) {
        procedure.apply(MutableIntTreeSet.Builder::new);
    }

    @Override
    IntTransformableBuilder newIntBuilder() {
        return new MutableIntTreeSet.Builder();
    }

    @Override
    public void withValue(IntProcedure procedure) {
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

    @Override
    public void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    @Override
    public void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v * v);
        procedure.apply(v -> v + 1);
    }

    @Test
    void testIteratorOrderForAscending() {
        final int amount = 10;
        final MutableIntTreeSet set = new MutableIntTreeSet();
        for (int i = 0; i < amount; i++) {
            set.add(i);
        }

        assertEquals(amount, set.size());
        final IntTraverser traverser = set.iterator();
        for (int i = 0; i < amount; i++) {
            assertTrue(traverser.hasNext());
            assertEquals(i, traverser.next().intValue());
        }
        assertFalse(traverser.hasNext());
    }

    @Test
    void testIteratorOrderForDescending() {
        final int amount = 10;
        final MutableIntTreeSet set = new MutableIntTreeSet();
        for (int i = amount - 1; i >= 0; i--) {
            set.add(i);
        }

        assertEquals(amount, set.size());
        final IntTraverser traverser = set.iterator();
        for (int i = 0; i < amount; i++) {
            assertTrue(traverser.hasNext());
            assertEquals(i, traverser.next().intValue());
        }
        assertFalse(traverser.hasNext());
    }
}

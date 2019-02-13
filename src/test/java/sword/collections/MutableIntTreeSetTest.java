package sword.collections;

public final class MutableIntTreeSetTest extends AbstractIntTraversableTest {

    private final int[] intValues = {
            Integer.MIN_VALUE, -100, -2, -1, 0, 1, 2, 5, Integer.MAX_VALUE
    };

    @Override
    AbstractIntTraversable emptyCollection() {
        return ImmutableIntSetImpl.empty();
    }

    @Override
    IntTraversableBuilder newIntBuilder() {
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

    public void testIteratorOrderForAscending() {
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

    public void testIteratorOrderForDescending() {
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

package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public final class MutableIntArraySetTest extends IntSetTest<MutableIntArraySet.Builder> implements MutableIntSetTest<MutableIntArraySet.Builder> {

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE, -500, -2, -1, 0, 1, 3, 127, 128, Integer.MAX_VALUE
    };

    @Override
    MutableIntArraySet.Builder newIntBuilder() {
        return new MutableIntArraySet.Builder();
    }

    @Override
    public void withValue(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    private boolean isPositiveValue(int value) {
        return value >= 0;
    }

    @Override
    void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::isPositiveValue);
    }

    @Override
    void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    @Override
    void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v * v);
        procedure.apply(v -> v + 1);
    }

    @Test
    public void testIteratingForMultipleElements() {
        withValue(a -> withValue(b -> {
            final MutableIntArraySet set = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> iterator = set.iterator();

            assertTrue(iterator.hasNext());
            final int first = iterator.next();

            if (a == b) {
                assertFalse(iterator.hasNext());
                assertEquals(a, first);
            }
            else {
                assertTrue(iterator.hasNext());
                final int second = iterator.next();

                assertFalse(iterator.hasNext());
                if (a < b) {
                    assertEquals(a, first);
                    assertEquals(b, second);
                }
                else {
                    assertEquals(a, second);
                    assertEquals(b, first);
                }
            }
        }));
    }

    @Test
    public void testValueAt() {
        withValue(a -> withValue(b -> withValue(c -> {
            final MutableIntArraySet set = newIntBuilder().add(a).add(b).add(c).build();
            final Iterator<Integer> it = set.iterator();
            int index = 0;
            while (it.hasNext()) {
                assertEquals(set.valueAt(index++), it.next().intValue());
            }
        })));
    }

    @Test
    public void testToImmutableMethodReturnSameInstance() {
        withValue(a -> withValue(b -> {
            final MutableIntArraySet set = new MutableIntArraySet.Builder().add(a).add(b).build();
            final ImmutableIntSet set2 = set.toImmutable();
            assertEquals(set.size(), set2.size());
            for (int value : set) {
                assertTrue(set2.contains(value));
            }
        }));
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<MutableIntArraySet.Builder>> procedure) {
        procedure.apply(MutableIntArraySet.Builder::new);
    }
}

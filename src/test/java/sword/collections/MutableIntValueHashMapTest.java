package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static sword.collections.TestUtils.withInt;

public final class MutableIntValueHashMapTest extends MutableIntValueMapTest<String, MutableIntValueHashMap<String>> {

    @Override
    MutableIntValueHashMap.Builder<String> newBuilder() {
        return new MutableIntValueHashMap.Builder<>();
    }

    @Override
    void withKey(Procedure<String> procedure) {
        final String[] values = {null, "", " ", "abcd", "0"};
        for (String value : values) {
            procedure.apply(value);
        }
    }

    @Override
    void withSortFunc(Procedure<SortFunction<String>> procedure) {
        procedure.apply(SortUtils::compareCharSequenceByUnicode);
        procedure.apply(SortUtils::compareByHashCode);
    }

    @Override
    String keyFromInt(int value) {
        return Integer.toString(value);
    }

    @Override
    IntTraversableBuilder newIntBuilder() {
        return new SameKeyAndValueTraversableBuilder();
    }

    @Override
    void withItem(IntProcedure procedure) {
        withInt(procedure);
    }

    @Override
    void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(v -> (v & 1) == 0);
    }

    @Override
    public void withIntTraversableBuilderSupplier(Procedure<IntBuilderSupplier<MutableIntValueHashMap<String>, MutableIntTraversableBuilder<MutableIntValueHashMap<String>>>> procedure) {
        procedure.apply(SameKeyAndValueTraversableBuilder::new);
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withInt(procedure);
    }

    @Test
    public void testHashCode() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntValueMap<String> mutable = newBuilder()
                    .put(Integer.toString(a), b)
                    .put(Integer.toString(b), c)
                    .put(Integer.toString(c), a)
                    .build();
            final IntValueMap<String> immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable.hashCode(), immutable.hashCode());
        })));
    }

    @Test
    public void testEquals() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntValueMap<String> mutable = newBuilder()
                    .put(Integer.toString(a), b)
                    .put(Integer.toString(b), c)
                    .put(Integer.toString(c), a)
                    .build();
            final IntValueMap<String> immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable, immutable);
            assertEquals(immutable, mutable);
        })));
    }

    private static final class SameKeyAndValueTraversableBuilder implements MutableIntTraversableBuilder<MutableIntValueHashMap<String>> {
        private final MutableIntValueHashMap<String> map = MutableIntValueHashMap.empty();

        @Override
        public SameKeyAndValueTraversableBuilder add(int value) {
            map.put(Integer.toString(value), value);
            return this;
        }

        @Override
        public MutableIntValueHashMap<String> build() {
            return map;
        }
    }
}

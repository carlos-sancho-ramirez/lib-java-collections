package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static sword.collections.TestUtils.withInt;

public final class MutableIntValueHashMapTest implements MutableIntValueMapTest<String, MutableIntTransformableBuilder, MutableIntValueHashMap.Builder<String>> {

    @Override
    public MutableIntValueHashMap.Builder<String> newBuilder() {
        return new MutableIntValueHashMap.Builder<>();
    }

    @Override
    public void withKey(Procedure<String> procedure) {
        final String[] values = {null, "", " ", "abcd", "0"};
        for (String value : values) {
            procedure.apply(value);
        }
    }

    @Override
    public void withSortFunc(Procedure<SortFunction<String>> procedure) {
        procedure.apply(SortUtils::compareCharSequenceByUnicode);
        procedure.apply(SortUtils::compareByHashCode);
    }

    @Override
    public String keyFromInt(int value) {
        return Integer.toString(value);
    }

    @Override
    public IntTransformableBuilder newIntBuilder() {
        return new SameKeyAndValueTraversableBuilder();
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withInt(procedure);
    }

    @Override
    public void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(v -> (v & 1) == 0);
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<MutableIntTransformableBuilder>> procedure) {
        procedure.apply(SameKeyAndValueTraversableBuilder::new);
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<String, MutableIntValueHashMap.Builder<String>>> procedure) {
        procedure.apply(MutableIntValueHashMap.Builder::new);
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

    static final class SameKeyAndValueTraversableBuilder implements MutableIntTransformableBuilder {
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

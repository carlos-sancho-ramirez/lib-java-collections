package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static sword.collections.TestUtils.withInt;

public final class MutableIntValueSortedMapTest implements MutableIntValueMapTest<String, MutableIntTransformableBuilder, MutableIntValueSortedMap.Builder<String>> {

    @Override
    public MutableIntValueSortedMap.Builder<String> newBuilder() {
        return new MutableIntValueSortedMap.Builder<>(SortUtils::compareCharSequenceByUnicode);
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
        return new SameKeyAndValueTraversableBuilder(SortUtils::compareByHashCode);
    }

    @Override
    public void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(v -> (v & 1) == 0);
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<MutableIntTransformableBuilder>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new SameKeyAndValueTraversableBuilder(sortFunc)));
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<String, MutableIntValueSortedMap.Builder<String>>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new MutableIntValueSortedMap.Builder<>(sortFunc)));
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

    private static final class SameKeyAndValueTraversableBuilder implements MutableIntTransformableBuilder {
        private final MutableIntValueSortedMap<String> map;

        SameKeyAndValueTraversableBuilder(SortFunction<String> sortFunc) {
            map = new MutableIntValueSortedMap.Builder<>(sortFunc).build();
        }

        @Override
        public SameKeyAndValueTraversableBuilder add(int value) {
            map.put(Integer.toString(value), value);
            return this;
        }

        @Override
        public MutableIntValueSortedMap<String> build() {
            return map;
        }
    }
}

package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static sword.collections.TestUtils.withInt;

public final class ImmutableIntValueSortedMapTest implements ImmutableIntValueMapTest<String, ImmutableIntTransformableBuilder, ImmutableIntValueSortedMap.Builder<String>> {

    @Override
    public ImmutableIntValueSortedMap.Builder<String> newBuilder() {
        return new ImmutableIntValueSortedMap.Builder<>(SortUtils::compareCharSequenceByUnicode);
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
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntTransformableBuilder>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new SameKeyAndValueTraversableBuilder(sortFunc)));
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<String, ImmutableIntValueSortedMap.Builder<String>>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new ImmutableIntValueSortedMap.Builder<>(sortFunc)));
    }

    @Override
    public IntTransformableBuilder newIntBuilder() {
        return new SameKeyAndValueTraversableBuilder(SortUtils::compareByHashCode);
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withInt(procedure);
    }

    @Test
    void testPutAllMustReturnAnImmutableHashMap() {
        final ImmutableIntValueSortedMap<String> map = newBuilder().build();
        final ImmutableIntValueSortedMap<String> result = map.putAll(map);
        assertSame(result, map);
    }

    private static final class SameKeyAndValueTraversableBuilder implements ImmutableIntTransformableBuilder {
        private final ImmutableIntValueSortedMap.Builder<String> builder;

        SameKeyAndValueTraversableBuilder(SortFunction<String> sortFunction) {
            builder = new ImmutableIntValueSortedMap.Builder<>(sortFunction);
        }

        @Override
        public SameKeyAndValueTraversableBuilder add(int value) {
            builder.put(Integer.toString(value), value);
            return this;
        }

        @Override
        public ImmutableIntTransformable build() {
            return builder.build();
        }
    }
}

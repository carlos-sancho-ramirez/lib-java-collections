package sword.collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withInt;

public final class ImmutableIntValueSortedMapTest extends ImmutableIntValueMapTest<String, ImmutableIntTransformableBuilder> {

    @Override
    ImmutableIntValueSortedMap.Builder<String> newBuilder() {
        return new ImmutableIntValueSortedMap.Builder<>(SortUtils::compareCharSequenceByUnicode);
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
    void assertEmpty(ImmutableIntValueMap<String> map) {
        assertTrue(map.isEmpty());
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntTransformableBuilder>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new SameKeyAndValueTraversableBuilder(sortFunc)));
    }

    @Override
    void withMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<String, IntValueMap.Builder<String>>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new ImmutableIntValueSortedMap.Builder<>(sortFunc)));
    }

    @Override
    IntTransformableBuilder newIntBuilder() {
        return new SameKeyAndValueTraversableBuilder(SortUtils::compareByHashCode);
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withInt(procedure);
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

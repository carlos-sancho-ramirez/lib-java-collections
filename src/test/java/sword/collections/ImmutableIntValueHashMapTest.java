package sword.collections;

import static org.junit.jupiter.api.Assertions.assertSame;
import static sword.collections.TestUtils.withInt;

public final class ImmutableIntValueHashMapTest extends ImmutableIntValueMapTest<String> {

    @Override
    ImmutableIntValueHashMap.Builder<String> newBuilder() {
        return new ImmutableIntValueHashMap.Builder<>();
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
        assertSame(newBuilder().build(), map);
    }

    @Override
    IntTraversableBuilder newIntBuilder() {
        return new SameKeyAndValueTraversableBuilder();
    }

    @Override
    void withItem(IntProcedure procedure) {
        withInt(procedure);
    }

    private static final class SameKeyAndValueTraversableBuilder implements ImmutableIntTransformableBuilder<ImmutableIntValueHashMap<String>> {
        private final ImmutableIntValueHashMap.Builder<String> builder = new ImmutableIntValueHashMap.Builder<>();

        @Override
        public SameKeyAndValueTraversableBuilder add(int value) {
            builder.put(Integer.toString(value), value);
            return this;
        }

        @Override
        public ImmutableIntValueHashMap<String> build() {
            return builder.build();
        }
    }
}

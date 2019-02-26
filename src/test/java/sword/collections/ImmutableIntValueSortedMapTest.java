package sword.collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ImmutableIntValueSortedMapTest extends ImmutableIntValueMapTest<String> {

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
}

package sword.collections;

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
    String keyFromInt(int value) {
        return Integer.toString(value);
    }

    @Override
    void assertEmpty(ImmutableIntValueMap<String> map) {
        assertSame(newBuilder().build(), map);
    }
}

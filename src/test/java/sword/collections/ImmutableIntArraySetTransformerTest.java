package sword.collections;

final class ImmutableIntArraySetTransformerTest extends IntTransformerTest<ImmutableIntArraySet.Builder> {

    @Override
    void withBuilder(Procedure<ImmutableIntArraySet.Builder> procedure) {
        procedure.apply(new ImmutableIntArraySet.Builder());
    }

    @Override
    void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v);
        procedure.apply(v -> v * v);
        procedure.apply(v -> -v - 1);
    }

    @Override
    void withMapFunc(Procedure<IntFunction<Object>> procedure) {
        procedure.apply(Integer::toString);
    }
}

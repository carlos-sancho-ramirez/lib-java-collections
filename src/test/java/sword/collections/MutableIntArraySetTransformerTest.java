package sword.collections;

final class MutableIntArraySetTransformerTest extends IntTransformerTest<MutableIntArraySet.Builder> {

    @Override
    void withBuilder(Procedure<MutableIntArraySet.Builder> procedure) {
        procedure.apply(new MutableIntArraySet.Builder());
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

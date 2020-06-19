package sword.collections;

final class MutableIntListTransformerTest extends IntTransformerTest<MutableIntList.Builder> {

    @Override
    void withBuilder(Procedure<MutableIntList.Builder> procedure) {
        procedure.apply(new MutableIntList.Builder());
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

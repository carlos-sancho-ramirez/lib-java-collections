package sword.collections;

public final class ImmutableIntListTransformerTest extends IntTransformerTest<ImmutableIntList, ImmutableIntList.Builder> {

    @Override
    void withBuilder(Procedure<ImmutableIntList.Builder> procedure) {
        procedure.apply(new ImmutableIntList.Builder());
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

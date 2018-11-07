package sword.collections;

public final class ImmutableIntSetImplTransformerTest extends IntTransformerTest<ImmutableIntSet, ImmutableIntSetImpl.Builder> {

    @Override
    void withBuilder(Procedure<ImmutableIntSetImpl.Builder> procedure) {
        procedure.apply(new ImmutableIntSetImpl.Builder());
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

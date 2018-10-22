package sword.collections;

public final class ImmutableIntSetImplTraverserTest extends IntTransformerTest<ImmutableIntSet, ImmutableIntSetImpl.Builder> {

    @Override
    void withBuilder(Procedure<ImmutableIntSetImpl.Builder> procedure) {
        procedure.apply(new ImmutableIntSetImpl.Builder());
    }
}

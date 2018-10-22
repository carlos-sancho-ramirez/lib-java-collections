package sword.collections;

public final class ImmutableIntListTraverserTest extends IntTransformerTest<ImmutableIntList, ImmutableIntList.Builder> {

    @Override
    void withBuilder(Procedure<ImmutableIntList.Builder> procedure) {
        procedure.apply(new ImmutableIntList.Builder());
    }
}

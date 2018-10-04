package sword.collections;

public final class ImmutableIntListTraverserTest extends IntTraverserTest<ImmutableIntList> {

    @Override
    void withBuilder(Procedure<IntCollectionBuilder<ImmutableIntList>> procedure) {
        procedure.apply(new ImmutableIntList.Builder());
    }
}

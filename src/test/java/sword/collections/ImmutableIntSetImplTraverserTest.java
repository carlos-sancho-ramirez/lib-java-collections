package sword.collections;

public final class ImmutableIntSetImplTraverserTest extends IntTraverserTest<ImmutableIntSet> {

    @Override
    void withBuilder(Procedure<IntCollectionBuilder<ImmutableIntSet>> procedure) {
        procedure.apply(new ImmutableIntSetImpl.Builder());
    }
}

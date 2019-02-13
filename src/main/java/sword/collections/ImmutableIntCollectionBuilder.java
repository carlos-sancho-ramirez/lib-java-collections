package sword.collections;

public interface ImmutableIntCollectionBuilder<T extends ImmutableIntTraversable> extends IntTransformableBuilder<T> {
    @Override
    ImmutableIntCollectionBuilder<T> add(int value);

    @Override
    T build();
}

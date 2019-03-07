package sword.collections;

public interface ImmutableIntCollectionBuilder<T extends ImmutableIntTransformable> extends IntTransformableBuilder<T> {
    @Override
    ImmutableIntCollectionBuilder<T> add(int value);

    @Override
    T build();
}

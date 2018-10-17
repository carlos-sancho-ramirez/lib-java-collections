package sword.collections;

public interface ImmutableCollectionBuilder<T> extends TransformableBuilder<T> {
    ImmutableCollectionBuilder<T> add(T element);
    IterableImmutableCollection<T> build();
}

package sword.collections;

public interface ImmutableCollectionBuilder<T> extends CollectionBuilder<T> {
    ImmutableCollectionBuilder<T> add(T element);
    IterableImmutableCollection<T> build();
}

package sword.collections;

public interface CollectionBuilder<T> {
    CollectionBuilder<T> add(T element);
    IterableCollection<T> build();
}

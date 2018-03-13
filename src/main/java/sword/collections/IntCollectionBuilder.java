package sword.collections;

public interface IntCollectionBuilder<T extends IterableIntCollection> {
    IntCollectionBuilder<T> add(int value);
    T build();
}

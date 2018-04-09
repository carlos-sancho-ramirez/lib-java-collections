package sword.collections;

public interface IntKeyMapBuilder<T> extends CollectionBuilder<T> {
    IntKeyMapBuilder<T> add(T element);
    IntKeyMapBuilder<T> put(int key, T value);
    IntKeyMap<T> build();
}

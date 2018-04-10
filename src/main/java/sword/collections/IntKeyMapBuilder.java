package sword.collections;

public interface IntKeyMapBuilder<T> {
    IntKeyMapBuilder<T> put(int key, T value);
    IntKeyMap<T> build();
}

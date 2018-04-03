package sword.collections;

public interface IntValueMapBuilder<T> {
    IntValueMapBuilder<T> put(T key, int value);
    IntValueMap<T> build();
}

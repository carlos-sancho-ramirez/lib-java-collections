package sword.collections;

public interface IntTraversableBuilder<T extends IntTraversable> {
    IntTraversableBuilder<T> add(int value);
    T build();
}

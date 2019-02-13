package sword.collections;

public interface TraversableBuilder<T> {
    TraversableBuilder<T> add(T element);
    Traversable<T> build();
}

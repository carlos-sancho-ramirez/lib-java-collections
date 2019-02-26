package sword.collections;

public interface MutableTraversableBuilder<T> extends TraversableBuilder<T> {
    MutableTraversableBuilder<T> add(T element);
    MutableTraversable<T> build();
}

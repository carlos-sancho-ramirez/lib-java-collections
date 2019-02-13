package sword.collections;

public interface TransformableBuilder<T> extends TraversableBuilder<T> {
    TransformableBuilder<T> add(T element);
    Transformable<T> build();
}

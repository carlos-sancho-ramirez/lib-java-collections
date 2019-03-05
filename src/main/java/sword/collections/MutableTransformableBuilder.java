package sword.collections;

public interface MutableTransformableBuilder<T> extends TransformableBuilder<T>, MutableTraversableBuilder<T> {
    MutableTransformableBuilder<T> add(T element);
    MutableTransformable<T> build();
}

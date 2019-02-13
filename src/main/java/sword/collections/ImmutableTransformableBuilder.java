package sword.collections;

public interface ImmutableTransformableBuilder<T> extends TransformableBuilder<T> {
    ImmutableTransformableBuilder<T> add(T element);
    ImmutableTransformable<T> build();
}

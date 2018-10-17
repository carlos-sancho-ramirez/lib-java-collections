package sword.collections;

public interface TransformableBuilder<T> extends CollectionBuilder<T> {
    TransformableBuilder<T> add(T element);
    Transformable<T> build();
}

package sword.collections;

public interface IntTransformableBuilder<T extends IntTransformable> extends IntTraversableBuilder<T> {
    @Override
    IntTransformableBuilder<T> add(int value);

    @Override
    T build();
}

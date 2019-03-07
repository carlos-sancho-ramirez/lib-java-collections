package sword.collections;

public interface IntTransformableBuilder<C extends IntTransformable> extends IntTraversableBuilder<C> {
    @Override
    IntTransformableBuilder<C> add(int value);

    @Override
    C build();
}

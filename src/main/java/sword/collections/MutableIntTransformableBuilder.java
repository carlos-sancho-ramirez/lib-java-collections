package sword.collections;

public interface MutableIntTransformableBuilder<C extends MutableIntTransformable> extends IntTransformableBuilder<C>, MutableIntTraversableBuilder<C> {
    @Override
    MutableIntTransformableBuilder<C> add(int value);

    @Override
    C build();
}

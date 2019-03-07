package sword.collections;

public interface ImmutableIntTransformableBuilder<C extends ImmutableIntTransformable> extends IntTransformableBuilder<C> {
    ImmutableIntTransformableBuilder<C> add(int value);
    C build();
}

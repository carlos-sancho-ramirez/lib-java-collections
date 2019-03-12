package sword.collections;

public interface ImmutableIntTransformableBuilder extends IntTransformableBuilder {
    @Override
    ImmutableIntTransformableBuilder add(int value);

    @Override
    ImmutableIntTransformable build();
}

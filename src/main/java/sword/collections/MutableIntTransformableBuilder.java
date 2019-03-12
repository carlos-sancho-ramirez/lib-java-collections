package sword.collections;

public interface MutableIntTransformableBuilder extends IntTransformableBuilder, MutableIntTraversableBuilder {
    @Override
    MutableIntTransformableBuilder add(int value);

    @Override
    MutableIntTransformable build();
}

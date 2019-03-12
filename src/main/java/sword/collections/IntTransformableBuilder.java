package sword.collections;

public interface IntTransformableBuilder extends IntTraversableBuilder {
    @Override
    IntTransformableBuilder add(int value);

    @Override
    IntTransformable build();
}

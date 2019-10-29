package sword.collections;

public interface IntListBuilder extends IntTransformableBuilder {
    @Override
    IntListBuilder add(int value);

    @Override
    IntList build();
}

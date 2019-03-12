package sword.collections;

public interface MutableIntTraversableBuilder extends IntTraversableBuilder {
    @Override
    MutableIntTraversableBuilder add(int value);

    @Override
    MutableIntTraversable build();
}

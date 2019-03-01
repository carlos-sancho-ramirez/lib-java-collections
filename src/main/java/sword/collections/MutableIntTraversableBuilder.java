package sword.collections;

public interface MutableIntTraversableBuilder<C extends MutableIntTraversable> extends IntTraversableBuilder<C> {
    MutableIntTraversableBuilder<C> add(int value);
    C build();
}

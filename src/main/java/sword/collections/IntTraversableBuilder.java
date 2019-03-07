package sword.collections;

public interface IntTraversableBuilder<C extends IntTraversable> {
    IntTraversableBuilder<C> add(int value);
    C build();
}

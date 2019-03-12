package sword.collections;

public interface IntTraversableBuilder {
    IntTraversableBuilder add(int value);
    IntTraversable build();
}

package sword.collections;

public interface IntPairMapBuilder {
    IntPairMapBuilder put(int key, int value);
    IntPairMap build();
}

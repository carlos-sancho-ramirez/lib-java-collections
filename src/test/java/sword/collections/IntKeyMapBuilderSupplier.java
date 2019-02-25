package sword.collections;

public interface IntKeyMapBuilderSupplier<T, B extends IntKeyMapBuilder<T>> {
    B newBuilder();
}

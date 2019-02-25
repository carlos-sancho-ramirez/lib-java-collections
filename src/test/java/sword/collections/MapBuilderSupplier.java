package sword.collections;

public interface MapBuilderSupplier<K, V, B extends MapBuilder<K, V>> {
    B newBuilder();
}

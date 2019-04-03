package sword.collections;

public interface IntValueMapBuilderSupplier<K, B extends IntValueMap.Builder<K>> {
    B newBuilder();
}

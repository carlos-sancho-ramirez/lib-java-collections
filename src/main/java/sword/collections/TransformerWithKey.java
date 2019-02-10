package sword.collections;

public interface TransformerWithKey<K, V> extends Transformer<V> {

    /**
     * Key attached to the last value returned in {@link #next()}
     */
    K key();

    /**
     * Build a new map containing all elements given on traversing this collection
     */
    Map<K, V> toMap();
}

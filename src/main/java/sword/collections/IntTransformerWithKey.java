package sword.collections;

public interface IntTransformerWithKey<K> extends IntTransformer {

    /**
     * Key attached to the last value returned in {@link #next()}
     */
    K key();

    /**
     * Build a new map containing all elements given on traversing this collection
     */
    IntValueMap<K> toMap();
}

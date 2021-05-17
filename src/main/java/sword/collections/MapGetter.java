package sword.collections;

public interface MapGetter<K, V> {

    /**
     * Return the value assigned to the given key.
     * @throws UnmappedKeyException if the given key is not found within the map.
     */
    V get(K key) throws UnmappedKeyException;
}

package sword.collections;

public interface IntValueMapGetter<T> {

    /**
     * Return the value assigned to the given key.
     * @throws UnmappedKeyException if the given key is not found within the map.
     */
    int get(T key) throws UnmappedKeyException;
}

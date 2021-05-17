package sword.collections;

public interface IntKeyMapGetter<T> {

    /**
     * Return the value assigned to the given key.
     * @throws UnmappedKeyException if the given key is not found within the map.
     */
    T get(int key) throws UnmappedKeyException;
}

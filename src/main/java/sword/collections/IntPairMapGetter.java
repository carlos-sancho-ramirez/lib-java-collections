package sword.collections;

public interface IntPairMapGetter {

    /**
     * Return the value assigned to the given key.
     * @throws UnmappedKeyException if the given key is not found within the map.
     */
    int get(int key) throws UnmappedKeyException;
}

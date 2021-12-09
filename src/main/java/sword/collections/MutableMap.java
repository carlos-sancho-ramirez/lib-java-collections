package sword.collections;

/**
 * Mutable version of a Map.
 *
 * This Map is mutable, that means that, in contrast with the immutable version,
 * algorithms to insert, modify and remove are required. This may affect its
 * internal memory footprint to ensure easy modification.
 *
 * Implementation of this interfaces will assume that keys inserted are immutable.
 * Mutation of the contained keys may result in duplicates within
 * the keys or wrong sorting of keys.
 * It is not guaranteed to work if keys are mutable.
 * This does no apply to values of the map, that can mutate without risk.
 *
 * @param <K> Type for the key elements within the Map
 * @param <V> Type for the value elements within the Map
 */
public interface MutableMap<K, V> extends Map<K, V>, MutableTransformable<V> {

    /**
     * Inserts the given key-value pair into this map.
     *
     * @param key Key to be compared with any existing one.
     * @param value Value to be placed within this map.
     * @return Whether this operation has increased by one the size of this map.
     */
    boolean put(K key, V value);
    boolean remove(K key);

    /**
     * Inserts into this map all the data found in the given map.
     *
     * As this is a map, duplicated keys will not be allowed.
     * Than means that elements within the given map will replace any value in
     * this map if there is an equivalent key already included in this map.
     *
     * @param other Map from where new items will be added.
     * @return Whether this operation has increased the size of this map or not.
     */
    default boolean putAll(Map<K, ? extends V> other) {
        boolean changed = false;
        final TransformerWithKey<K, ? extends V> transformer = other.iterator();
        while (transformer.hasNext()) {
            final V value = transformer.next();
            changed |= put(transformer.key(), value);
        }

        return changed;
    }

    /**
     * Create a new mutable map instance and copy from this collection the actual data references. After it, this collection gets cleared.
     * <p>
     * This is a more efficient alternative to the following code:
     * <code>
     * <br>MutableMap newMap = map.mutate();
     * <br>map.clear();
     * </code>
     *
     * @return A new mutable map that contains the actual data of this map.
     */
    default MutableMap<K, V> donate() {
        final MutableMap<K, V> mutated = mutate();
        clear();
        return mutated;
    }

    /**
     * Removes from this map the entry whose key matches the given key, returning its matching value.
     *
     * This method will shrink the size of this map in one, except if the given key is not present.
     * In case the given key is not present, an {@link UnmappedKeyException} will be thrown.
     *
     * This method is exactly the same as executing the following snippet:
     * <pre>
     *     V value = map.get(key);
     *     map.remove(key);
     *     return value;
     * </pre>
     *
     * @param key Key expected to be found within this map.
     * @return The value associated with the given key.
     * @throws UnmappedKeyException if the given key is not present in this map.
     */
    default V pick(K key) throws UnmappedKeyException {
        final int index = indexOfKey(key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        final V value = valueAt(index);
        removeAt(index);
        return value;
    }

    interface Builder<K, V> extends MapBuilder<K, V> {
        @Override
        Builder<K, V> put(K key, V value);

        @Override
        MutableMap<K, V> build();
    }
}

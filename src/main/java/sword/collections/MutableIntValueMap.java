package sword.collections;

/**
 * Mutable version of a Map with integer values.
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
 * @param <T> Type for the key elements within the Map
 */
public interface MutableIntValueMap<T> extends IntValueMap<T>, MutableIntTransformable {

    boolean put(T key, int value);

    /**
     * Create a new mutable map instance and copy from this collection the actual data references. After it, this collection gets cleared.
     * <p>
     * This is a more efficient alternative to the following code:
     * <code>
     * <br>MutableIntValueMap newMap = map.mutate();
     * <br>map.clear();
     * </code>
     *
     * @return A new mutable map that contains the actual data of this map.
     */
    default MutableIntValueMap<T> donate() {
        final MutableIntValueMap<T> mutated = mutate();
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
     *     int value = map.get(key);
     *     map.remove(key);
     *     return value;
     * </pre>
     *
     * @param key Key expected to be found within this map.
     * @return The value associated with the given key.
     * @throws UnmappedKeyException if the given key is not present in this map.
     */
    default int pick(T key) throws UnmappedKeyException {
        final int index = indexOfKey(key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        final int value = valueAt(index);
        removeAt(index);
        return value;
    }

    interface Builder<E> extends IntValueMap.Builder<E> {

        @Override
        Builder<E> put(E key, int value);

        @Override
        MutableIntValueMap<E> build();
    }
}

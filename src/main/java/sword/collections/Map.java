package sword.collections;

/**
 * Root for both variants of Map, immutable and mutable.
 * @param <K> Type of key items within the map.
 * @param <V> Type of value items within the map.
 */
public interface Map<K, V> extends IterableCollection<Map.Entry<K, V>>, Sizable {

    /**
     * Check whether the given key is contained in the map
     * @param key Key to be found.
     */
    boolean containsKey(K key);

    /**
     * Return the value assigned to the given key.
     * @throws UnmappedKeyException if the given key is not found within the map.
     */
    V get(K key);

    /**
     * Return the value assigned to the given key.
     * Or the given defaultValue if that key is not in the map.
     */
    V get(K key, V defaultValue);

    /**
     * Key in the given index position.
     *
     * @param index Index within the array of keys, valid indexes goes from 0 to {@link #size()} - 1
     * @return The key in the given position.
     */
    K keyAt(int index);

    /**
     * Value in the given index position.
     *
     * @param index Index within the array of values, valid indexes goes from 0 to {@link #size()} - 1
     * @return The value in the given position.
     */
    V valueAt(int index);

    /**
     * Returns the index for which {@link #keyAt(int)} would return the specified key,
     * or -1 if the specified key is not mapped.
     */
    int indexOfKey(K key);

    /**
     * Return the set of all keys
     */
    Set<K> keySet();

    /**
     * Return an immutable map from the values contained in this map.
     * The same instance will be returned in case of being already immutable.
     */
    ImmutableMap<K, V> toImmutable();

    /**
     * Return a new mutable map.
     * This method will always generate a new instance in order to avoid affecting the state of its original map.
     */
    MutableMap<K, V> mutate();

    final class Entry<A, B> {
        private final A mKey;
        private final B mValue;
        private final int mIndex;

        public Entry(int index, A key, B value) {
            mIndex = index;
            mKey = key;
            mValue = value;
        }

        public int getIndex() {
            return mIndex;
        }

        public A getKey() {
            return mKey;
        }

        public B getValue() {
            return mValue;
        }
    }
}

package sword.collections;

import static sword.collections.SortUtils.equal;

/**
 * Root for both variants of Map, immutable and mutable.
 * @param <K> Type of key items within the map.
 * @param <V> Type of value items within the map.
 */
public interface Map<K, V> extends IterableCollection<V>, Sizable {

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
     * Returns the index for which {@link #keyAt(int)} would return the specified key,
     * or -1 if the specified key is not mapped.
     */
    int indexOfKey(K key);

    /**
     * Return the set of all keys
     */
    Set<K> keySet();

    /**
     * Return the list of all values.
     * It is guaranteed that the traverse order within the new list is exactly the same that
     * traversing this map.
     */
    List<V> valueList();

    /**
     * Compose a set of key-value entries from this map.
     * Resulting set is guaranteed to keep the same item order when it is iterated.
     */
    Set<Entry<K, V>> entries();

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
        private final A _key;
        private final B _value;
        private final int _index;

        public Entry(int index, A key, B value) {
            _index = index;
            _key = key;
            _value = value;
        }

        public int index() {
            return _index;
        }

        public A key() {
            return _key;
        }

        public B value() {
            return _value;
        }

        @Override
        public String toString() {
            return String.valueOf(_key) + " -> " + _value;
        }

        @Override
        public int hashCode() {
            return (_key != null)? _key.hashCode() : 0;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof Entry)) {
                return false;
            }

            final Entry that = (Entry) other;
            return (equal(_key, that._key) && equal(_value, that._value));
        }
    }
}

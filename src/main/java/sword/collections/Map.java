package sword.collections;

import static sword.collections.SortUtils.equal;

/**
 * Root for both variants of Map, immutable and mutable.
 * @param <K> Type of key items within the map.
 * @param <V> Type of value items within the map.
 */
public interface Map<K, V> extends Transformable<V> {

    TransformerWithKey<K, V> iterator();

    /**
     * Check whether the given key is contained in the map
     * @param key Key to be found.
     */
    default boolean containsKey(K key) {
        return indexOfKey(key) >= 0;
    }

    /**
     * Return the value assigned to the given key.
     * @throws UnmappedKeyException if the given key is not found within the map.
     */
    default V get(K key) throws UnmappedKeyException {
        final int index = indexOfKey(key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        return valueAt(index);
    }

    /**
     * Return the value assigned to the given key.
     * Or the given defaultValue if that key is not in the map.
     */
    default V get(K key, V defaultValue) {
        final int index = indexOfKey(key);
        return (index >= 0)? valueAt(index) : defaultValue;
    }

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
    default int indexOfKey(K key) {
        return keySet().indexOf(key);
    }

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

    @Override
    Map<K, V> filter(Predicate<V> predicate);

    @Override
    Map<K, V> filterNot(Predicate<V> predicate);

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

    /**
     * Creates a new map where all current elements and future elements will be
     * sorted following the given function.
     *
     * @param function Function to sort the keys within this map.
     * @return A new map where all current elements and future newly added will
     * be sorted following the given function.
     */
    Map<K, V> sort(SortFunction<K> function);

    /**
     * Return true if this map, and the given one, have equivalent keys, and equivalent values assigned.
     *
     * Note that the order of the key-value pair within the map and the collection mutability is irrelevant.
     *
     * @param that Map to be contrasted to.
     */
    default boolean equalMap(Map that) {
        if (that == null) {
            return false;
        }

        final Set<K> keySet = keySet();
        if (!keySet.equalSet(that.keySet())) {
            return false;
        }

        for (K key : keySet) {
            if (!equal(get(key), that.get(key))) {
                return false;
            }
        }

        return true;
    }

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
            if (!(other instanceof Entry)) {
                return false;
            }

            final Entry that = (Entry) other;
            return (equal(_key, that._key) && equal(_value, that._value));
        }
    }
}

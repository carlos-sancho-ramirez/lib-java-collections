package sword.collections;

import static sword.collections.SortUtils.equal;

/**
 * Root for both variants of Map where values are integer values, immutable and mutable.
 * @param <T> Type of key items within the map.
 */
public interface IntValueMap<T> extends IterableIntCollection, Sizable {

    /**
     * Check whether the given key is contained in the map
     * @param key Key to be found.
     */
    default boolean containsKey(T key) {
        return indexOfKey(key) >= 0;
    }

    /**
     * Return the value assigned to the given key.
     * @throws UnmappedKeyException if the given key is not found within the map.
     */
    default int get(T key) throws UnmappedKeyException {
        final int index = indexOfKey(key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        return valueAt(index);
    }

    /**
     * Return the value assigned to the given key, or the given <pre>defaultValue</pre> the given key is not mapped.
     */
    default int get(T key, int defaultValue) {
        final int index = indexOfKey(key);
        return (index >= 0)? valueAt(index) : defaultValue;
    }

    /**
     * Key in the given index position.
     * As internally all keys are ensured to be sorted. Greater indexes provide greater keys.
     *
     * @param index Index within the array of keys, valid indexes goes from 0 to {@link #size()} - 1
     * @return The key in the given position.
     */
    T keyAt(int index);

    /**
     * Value in the given index position.
     *
     * @param index Index within the array of values, valid indexes goes from 0 to {@link #size()} - 1
     * @return The value in the given position.
     */
    int valueAt(int index);

    /**
     * Returns the index for which {@link #keyAt(int)} would return the specified key,
     * or -1 if the specified key is not mapped.
     */
    int indexOfKey(T key);

    /**
     * Return the set of all keys
     */
    Set<T> keySet();

    /**
     * Return the list of all values.
     * It is guaranteed that the traverse order within the new list is exactly the same that
     * traversing this map.
     */
    IntList valueList();

    /**
     * Compose a set of key-value entries from this map.
     * Resulting set is guaranteed to keep the same item order when it is iterated.
     */
    Set<Entry<T>> entries();

    /**
     * Return an immutable map from the values contained in this map.
     * The same instance will be returned in case of being already immutable.
     */
    ImmutableIntValueMap<T> toImmutable();

    /**
     * Return a new mutable map.
     * This method will always generate a new instance in order to avoid affecting the state of its original map.
     */
    MutableIntValueMap<T> mutate();

    final class Entry<E> {
        private final int _index;
        private final E _key;
        private final int _value;

        public Entry(int index, E key, int value) {
            _index = index;
            _key = key;
            _value = value;
        }

        public int index() {
            return _index;
        }

        @SuppressWarnings("unchecked")
        public E key() {
            return _key;
        }

        public int value() {
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
            return _value == that._value && equal(_key, that._key);
        }
    }

    interface Builder<T> {
        Builder<T> put(T key, int value);
        IntValueMap<T> build();
    }
}

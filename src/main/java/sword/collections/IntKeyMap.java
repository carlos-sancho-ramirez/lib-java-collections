package sword.collections;

import static sword.collections.SortUtils.equal;

public interface IntKeyMap<T> extends Transformable<T> {

    @Override
    IntKeyMap<T> filter(Predicate<T> predicate);

    @Override
    IntKeyMap<T> filterNot(Predicate<T> predicate);

    /**
     * Return the value assigned to the given key.
     * @throws UnmappedKeyException if the given key is not found within the map.
     */
    T get(int key);

    /**
     * Return the value assigned to the given key.
     * Or the given defaultValue if that key is not in the map.
     */
    T get(int key, T defaultValue);

    /**
     * Key in the given index position.
     * As internally all keys are ensured to be sorted. Greater indexes provide greater keys.
     *
     * @param index Index within the array of keys, valid indexes goes from 0 to {@link #size()} - 1
     * @return The key in the given position.
     */
    int keyAt(int index);

    /**
     * Returns the index for which {@link #keyAt(int)} would return the specified key,
     * or -1 if the specified key is not mapped.
     */
    int indexOfKey(int key);

    /**
     * Return the set of all keys
     */
    IntSet keySet();

    /**
     * Return the list of all values.
     * It is guaranteed that the traverse order within the new list is exactly the same that
     * traversing this map.
     */
    List<T> valueList();

    /**
     * Compose a set of key-value entries from this map.
     * Resulting set is guaranteed to keep the same item order when it is iterated.
     */
    Set<Entry<T>> entries();

    /**
     * Return an immutable map from the values contained in this map.
     * The same instance will be returned in case of being already immutable.
     */
    ImmutableIntKeyMap<T> toImmutable();

    /**
     * Return a new mutable map.
     * This method will always generate a new instance in order to avoid affecting the state of its original map.
     */
    MutableIntKeyMap<T> mutate();

    /**
     * Return true if this map, and the given one, have equivalent keys, and equivalent values assigned.
     *
     * Note that the order of the key-value pair within the map and the collection mutability is irrelevant.
     *
     * @param that Map to be contrasted to.
     */
    default boolean equalMap(IntKeyMap that) {
        if (that == null) {
            return false;
        }

        final IntSet keySet = keySet();
        if (!keySet.equalSet(that.keySet())) {
            return false;
        }

        for (int key : keySet) {
            if (!equal(get(key), that.get(key))) {
                return false;
            }
        }

        return true;
    }

    final class Entry<E> {
        private final int _key;
        private final E _value;
        private final int _index;

        public Entry(int index, int key, E value) {
            _index = index;
            _key = key;
            _value = value;
        }

        public int index() {
            return _index;
        }

        public int key() {
            return _key;
        }

        public E value() {
            return _value;
        }

        @Override
        public String toString() {
            return Integer.toString(_key) + " -> " + _value;
        }

        @Override
        public int hashCode() {
            return _key;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Entry)) {
                return false;
            }

            final Entry that = (Entry) other;
            return (_key == that._key && equal(_value, that._value));
        }
    }
}

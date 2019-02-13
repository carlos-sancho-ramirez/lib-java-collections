package sword.collections;

import static sword.collections.SortUtils.equal;

public interface IntPairMap extends IntTraversable {

    /**
     * Return the value assigned to the given key.
     * @throws UnmappedKeyException if the given key is not found within the map.
     */
    int get(int key);

    /**
     * Return the value assigned to the given key. Or <pre>defaultValue</pre> if that key is not in the map.
     */
    int get(int key, int defaultValue);

    /**
     * Key in the given index position.
     * As internally all keys are ensured to be sorted. Greater indexes provide greater keys.
     *
     * @param index Index within the array of keys, valid indexes goes from 0 to size() - 1
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
    IntList valueList();

    /**
     * Compose a set of key-value entries from this map.
     * Resulting set is guaranteed to keep the same item order when it is iterated.
     */
    Set<Entry> entries();

    /**
     * Return an immutable map from the values contained in this map.
     * The same instance will be returned in case of being already immutable.
     */
    ImmutableIntPairMap toImmutable();

    /**
     * Return a new mutable map.
     * This method will always generate a new instance in order to avoid affecting the state of its original map.
     */
    MutableIntPairMap mutate();

    final class Entry {
        private final int _key;
        private final int _value;
        private final int _index;

        Entry(int index, int key, int value) {
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

        public int value() {
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
            if (other == null || !(other instanceof Entry)) {
                return false;
            }

            final Entry that = (Entry) other;
            return (_key == that._key && equal(_value, that._value));
        }
    }
}

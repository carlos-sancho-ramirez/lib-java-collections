package sword.collections;

public interface IntKeyMap<T> extends Iterable<IntKeyMap.Entry<T>>, Sizable {

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
     * Value in the given index position.
     *
     * @param index Index within the array of values, valid indexes goes from 0 to {@link #size()} - 1
     * @return The value in the given position.
     */
    T valueAt(int index);

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
     * Return an immutable map from the values contained in this map.
     * The same instance will be returned in case of being already immutable.
     */
    ImmutableIntKeyMap<T> toImmutable();

    /**
     * Return a new mutable map.
     * This method will always generate a new instance in order to avoid affecting the state of its original map.
     */
    MutableIntKeyMap<T> mutate();

    final class Entry<E> {
        private final int mKey;
        private final E mValue;
        private final int mIndex;

        public Entry(int index, int key, E value) {
            mIndex = index;
            mKey = key;
            mValue = value;
        }

        public int getIndex() {
            return mIndex;
        }

        public int getKey() {
            return mKey;
        }

        public E getValue() {
            return mValue;
        }
    }
}

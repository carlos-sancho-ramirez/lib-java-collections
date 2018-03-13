package sword.collections;

public interface IntPairMap extends Iterable<IntPairMap.Entry>, Sizable {

    /**
     * Return the value assigned to the given key. Or <pre>defaultValue</pre> if that key is not in the map.
     */
    int get(int key, int defaultValue);

    /**
     * Return the value assigned to the given key. Or 0 if that key is not in the map.
     */
    int get(int key);

    /**
     * Key in the given index position.
     * As internally all keys are ensured to be sorted. Greater indexes provide greater keys.
     *
     * @param index Index within the array of keys, valid indexes goes from 0 to size() - 1
     * @return The key in the given position.
     */
    int keyAt(int index);

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
    int indexOfKey(int key);

    /**
     * Return the set of all keys
     */
    IntSet keySet();

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
        private final int mKey;
        private final int mValue;
        private final int mIndex;

        Entry(int index, int key, int value) {
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

        public int getValue() {
            return mValue;
        }
    }
}

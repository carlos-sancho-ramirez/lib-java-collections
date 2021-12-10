package sword.collections;

import sword.annotations.ToBeSubtyped;

import static sword.collections.SortUtils.equal;

/**
 * Root for both variants of Map where values are integer values, immutable and mutable.
 * @param <T> Type of key items within the map.
 */
public interface IntValueMap<T> extends IntTransformable, IntValueMapGetter<T> {

    @Override
    @ToBeSubtyped(IntTransformerWithKey.class)
    IntTransformer iterator();

    /**
     * Check whether the given key is contained in the map
     * @param key Key to be found.
     */
    default boolean containsKey(T key) {
        return indexOfKey(key) >= 0;
    }

    @Override
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

    @Override
    IntValueMap<T> filter(IntPredicate predicate);

    @Override
    IntValueMap<T> filterNot(IntPredicate predicate);

    @Override
    <U> Map<T, U> map(IntFunction<? extends U> func);

    @Override
    IntValueMap<T> mapToInt(IntToIntFunction func);

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

    /**
     * Return a new mutable map with the given {@link ArrayLengthFunction}.
     * This method will always generate a new instance in order to avoid affecting the state of its original map.
     */
    MutableIntValueMap<T> mutate(ArrayLengthFunction arrayLengthFunction);

    /**
     * Creates a new map where all current elements and future elements will be
     * sorted following the given function.
     *
     * @param function Function to sort the keys within this map.
     * @return A new map where all current elements and future newly added will
     * be sorted following the given function.
     */
    IntValueMap<T> sort(SortFunction<? super T> function);

    /**
     * Return true if this map, and the given one, have equivalent keys, and equivalent values assigned.
     *
     * Note that the order of the key-value pair within the map and the collection mutability is irrelevant.
     *
     * @param that Map to be contrasted to.
     */
    default boolean equalMap(IntValueMap that) {
        if (that == null) {
            return false;
        }

        final Set<T> keySet = keySet();
        if (!keySet.equalSet(that.keySet())) {
            return false;
        }

        for (T key : keySet) {
            if (!equal(get(key), that.get(key))) {
                return false;
            }
        }

        return true;
    }

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
            if (!(other instanceof Entry)) {
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

package sword.collections;

import sword.annotations.ToBeAbstract;

import static sword.collections.SortUtils.equal;

public interface IntPairMap extends IntTransformable, IntPairMapGetter {

    @Override
    int get(int key) throws UnmappedKeyException;

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
     * Check whether the given key is contained in the map.
     * @param key Key to be found.
     */
    default boolean containsKey(int key) {
        return indexOfKey(key) >= 0;
    }

    /**
     * Return the set of all keys
     */
    IntSet keySet();

    /**
     * Compose a set of key-value entries from this map.
     * Resulting set is guaranteed to keep the same item order when it is iterated.
     */
    Set<Entry> entries();

    @Override
    IntPairMap filter(IntPredicate predicate);

    @Override
    default IntPairMap filterNot(IntPredicate predicate) {
        return filter(v -> !predicate.apply(v));
    }

    /**
     * Composes a new Map containing all the key-value pairs from this map
     * where the given predicate returns true.
     *
     * @param predicate Only key returning true for the given predicate
     *                  will be present in the resulting Map.
     */
    default IntPairMap filterByKey(IntPredicate predicate) {
        final IntPairMapBuilder builder = new ImmutableIntPairMap.Builder();
        final Transformer<IntPairMap.Entry> transformer = entries().iterator();
        while (transformer.hasNext()) {
            final IntPairMap.Entry entry = transformer.next();
            final int key = entry.key();
            if (predicate.apply(key)) {
                builder.put(key, entry.value());
            }
        }

        return builder.build();
    }

    /**
     * Composes a new Map containing all the key-value pairs from this map where the given predicate returns true.
     * @param predicate Condition to be evaluated for each key-value pair.
     *                  Only the key-value pairs where this condition returned
     *                  true will be present in the resulting map.
     *                  For performance reasons, this predicate may recycle the
     *                  same entry instance for each call to the predicate, it
     *                  is important that the predicate does not store the
     *                  given entry instance anywhere as it is not guaranteed
     *                  to be immutable.
     */
    default IntPairMap filterByEntry(Predicate<IntPairMapEntry> predicate) {
        final IntPairMapBuilder builder = new ImmutableIntPairMap.Builder();
        final Transformer<IntPairMap.Entry> transformer = entries().iterator();
        while (transformer.hasNext()) {
            final IntPairMap.Entry entry = transformer.next();
            if (predicate.apply(entry)) {
                builder.put(entry.key(), entry.value());
            }
        }

        return builder.build();
    }

    @Override
    <U> IntKeyMap<U> map(IntFunction<? extends U> func);

    @Override
    IntPairMap mapToInt(IntToIntFunction func);

    @Override
    default IntPairMap slice(ImmutableIntRange range) {
        final int size = size();
        if (size == 0) {
            return this;
        }

        final int min = range.min();
        final int max = range.max();
        if (min >= size || max < 0) {
            return ImmutableIntPairMap.empty();
        }

        if (min <= 0 && max >= size - 1) {
            return this;
        }

        final ImmutableIntPairMap.Builder builder = new ImmutableIntPairMap.Builder();
        final int maxPosition = Math.min(max, size - 1);
        for (int position = min; position <= maxPosition; position++) {
            builder.put(keyAt(position), valueAt(position));
        }

        return builder.build();
    }

    @Override
    default IntPairMap skip(int length) {
        return slice(new ImmutableIntRange(length, Integer.MAX_VALUE));
    }

    /**
     * Returns a new IntPairMap where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of this map in iteration order.
     * @return A new IntPairMap instance just including the first elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length is equal or greater than the
     *         actual size of this collection.
     */
    default IntPairMap take(int length) {
        return (length == 0)? ImmutableIntPairMap.empty() : slice(new ImmutableIntRange(0, length - 1));
    }

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

    /**
     * Return a new mutable map with the given {@link ArrayLengthFunction}.
     * This method will always generate a new instance in order to avoid affecting the state of its original map.
     */
    MutableIntPairMap mutate(ArrayLengthFunction arrayLengthFunction);

    /**
     * Return true if this map, and the given one, have equivalent keys, and equivalent values assigned.
     *
     * Note that the order of the key-value pair within the map and the collection mutability is irrelevant.
     *
     * @param that Map to be contrasted to.
     */
    default boolean equalMap(IntPairMap that) {
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

    @ToBeAbstract("This should be an interface")
    final class Entry implements IntPairMapEntry {
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

        @Override
        public int key() {
            return _key;
        }

        @Override
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
            if (!(other instanceof Entry)) {
                return false;
            }

            final Entry that = (Entry) other;
            return _key == that._key && equal(_value, that._value);
        }
    }
}

package sword.collections;

import sword.annotations.ToBeAbstract;
import sword.annotations.ToBeSubtyped;

import static sword.collections.SortUtils.equal;

public interface IntKeyMap<T> extends Transformable<T>, IntKeyMapGetter<T> {

    @Override
    @ToBeSubtyped(TransformerWithIntKey.class)
    Transformer<T> iterator();

    @Override
    IntKeyMap<T> filter(Predicate<? super T> predicate);

    @Override
    default IntKeyMap<T> filterNot(Predicate<? super T> predicate) {
        return filter(v -> !predicate.apply(v));
    }

    /**
     * Composes a new Map containing all the key-value pairs from this map where the given predicate returns true.
     * @param predicate Only key returning true for the given predicate will be present
     *                  in the resulting Map.
     */
    default IntKeyMap<T> filterByKey(IntPredicate predicate) {
        final IntKeyMapBuilder<T> builder = new ImmutableIntKeyMap.Builder<>();
        final Transformer<Entry<T>> transformer = entries().iterator();
        while (transformer.hasNext()) {
            final Entry<T> entry = transformer.next();
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
    @ToBeAbstract("This implementation is unable to provide the proper map type. For example, sorted maps will always receive a hash map as response, which is not suitable")
    default IntKeyMap<T> filterByEntry(Predicate<IntKeyMapEntry<T>> predicate) {
        final IntKeyMapBuilder<T> builder = new ImmutableIntKeyMap.Builder<>();
        final Transformer<Entry<T>> transformer = entries().iterator();
        while (transformer.hasNext()) {
            final Entry<T> entry = transformer.next();
            if (predicate.apply(entry)) {
                builder.put(entry.key(), entry.value());
            }
        }

        return builder.build();
    }

    @Override
    <E> IntKeyMap<E> map(Function<? super T, ? extends E> func);

    @Override
    IntPairMap mapToInt(IntResultFunction<? super T> func);

    @Override
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
     * Return a new mutable map with the given {@link ArrayLengthFunction}.
     * This method will always generate a new instance in order to avoid affecting the state of its original map.
     */
    MutableIntKeyMap<T> mutate(ArrayLengthFunction arrayLengthFunction);

    @Override
    default IntKeyMap<T> slice(ImmutableIntRange range) {
        final int size = size();
        final int min = range.min();
        final int max = range.max();
        if (min <= 0 && max >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableIntKeyMap.empty();
        }

        final ImmutableIntKeyMap.Builder<T> builder = new ImmutableIntKeyMap.Builder<>();
        final int maxPosition = Math.min(max, size - 1);
        for (int position = min; position <= maxPosition; position++) {
            builder.put(keyAt(position), valueAt(position));
        }

        return builder.build();
    }

    @Override
    default IntKeyMap<T> skip(int length) {
        return slice(new ImmutableIntRange(length, Integer.MAX_VALUE));
    }

    /**
     * Returns a new IntKeyMap where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of this map in iteration order.
     * @return A new IntKeyMap instance just including the first elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length is equal or greater than the
     *         actual size of this collection.
     */
    @Override
    default IntKeyMap<T> take(int length) {
        return (length == 0)? ImmutableIntKeyMap.empty() : slice(new ImmutableIntRange(0, length - 1));
    }

    /**
     * Returns a new IntKeyMap where the <code>length</code> amount of last elements
     * has been removed.
     * <p>
     * This will return an empty map if the given parameter matches
     * or exceeds the length of this collection.
     *
     * @param length the amount of elements to be removed from the end of the map.
     * @return A new IntKeyMap instance without the last elements,
     *         the same instance in case the given length is 0,
     *         or an empty instance if the given length is equal or greater
     *         than the actual length of the map.
     */
    default IntKeyMap<T> skipLast(int length) {
        if (length == 0) {
            return this;
        }

        final int size = size();
        final int max = size - length - 1;
        return (max < 0)? ImmutableIntKeyMap.empty() : slice(new ImmutableIntRange(0, max));
    }

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

    @ToBeAbstract("This should be an interface")
    final class Entry<E> implements IntKeyMapEntry<E> {
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

        @Override
        public int key() {
            return _key;
        }

        @Override
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
            return _key == that._key && equal(_value, that._value);
        }
    }
}

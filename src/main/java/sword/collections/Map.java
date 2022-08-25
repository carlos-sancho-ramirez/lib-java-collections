package sword.collections;

import sword.annotations.ToBeAbstract;

import static sword.collections.SortUtils.equal;

/**
 * Root for both variants of Map, immutable and mutable.
 * @param <K> Type of key items within the map.
 * @param <V> Type of value items within the map.
 */
public interface Map<K, V> extends Transformable<V>, MapGetter<K, V> {

    @Override
    TransformerWithKey<K, V> iterator();

    /**
     * Check whether the given key is contained in the map
     * @param key Key to be found.
     */
    default boolean containsKey(K key) {
        return indexOfKey(key) >= 0;
    }

    @Override
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
     * Compose a set of key-value entries from this map.
     * Resulting set is guaranteed to keep the same item order when it is iterated.
     */
    Set<Entry<K, V>> entries();

    @Override
    Map<K, V> filter(Predicate<? super V> predicate);

    @Override
    default Map<K, V> filterNot(Predicate<? super V> predicate) {
        return filter(v -> !predicate.apply(v));
    }

    /**
     * Composes a new Map containing all the key-value pairs from this map where the given predicate returns true.
     * @param predicate Only key returning true for the given predicate will be present
     *                  in the resulting Map.
     */
    @ToBeAbstract("This implementation is unable to provide the proper map type. For example, sorted maps will always receive a hash map as response, which is not suitable")
    default Map<K, V> filterByKey(Predicate<? super K> predicate) {
        final MapBuilder<K, V> builder = new ImmutableHashMap.Builder<>();
        final TransformerWithKey<K, V> transformer = iterator();
        while (transformer.hasNext()) {
            final V value = transformer.next();
            final K key = transformer.key();
            if (predicate.apply(key)) {
                builder.put(key, value);
            }
        }
        return builder.build();
    }

    /**
     * Composes a new Map containing all the key-value pairs from this map where the given predicate returns false.
     * @param predicate Only key returning false for the given predicate will be present
     *                  in the resulting Map.
     */
    default Map<K, V> filterByKeyNot(Predicate<? super K> predicate) {
        return filterByKey(k -> !predicate.apply(k));
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
    default Map<K, V> filterByEntry(Predicate<MapEntry<K, V>> predicate) {
        final ReusableMapEntry<K, V> entry = new ReusableMapEntry<>();
        final MapBuilder<K, V> builder = new ImmutableHashMap.Builder<>();
        final TransformerWithKey<K, V> transformer = iterator();
        while (transformer.hasNext()) {
            final V value = transformer.next();
            final K key = transformer.key();
            entry.set(key, value);
            if (predicate.apply(entry)) {
                builder.put(key, value);
            }
        }
        return builder.build();
    }

    @Override
    <E> Map<K, E> map(Function<? super V, ? extends E> func);

    @Override
    IntValueMap<K> mapToInt(IntResultFunction<? super V> func);

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
     * Return a new mutable map with the given {@link ArrayLengthFunction}.
     * This method will always generate a new instance in order to avoid affecting the state of its original map.
     */
    MutableMap<K, V> mutate(ArrayLengthFunction arrayLengthFunction);

    /**
     * Creates a new map where all current elements and future elements will be
     * sorted following the given function.
     *
     * @param function Function to sort the keys within this map.
     * @return A new map where all current elements and future newly added will
     * be sorted following the given function.
     */
    Map<K, V> sort(SortFunction<? super K> function);

    @Override
    @ToBeAbstract("This implementation is unable to provide the proper map type and iteration order for all its subtypes. Sorted maps will become hash maps, and that will modify the order of iteration")
    default Map<K, V> slice(ImmutableIntRange range) {
        final int size = size();
        final int min = range.min();
        final int max = range.max();
        if (range.min() <= 0 && range.max() >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableHashMap.empty();
        }

        final ImmutableMap.Builder<K, V> builder = new ImmutableHashMap.Builder<>();
        final int maxPosition = Math.min(max, size - 1);
        for (int position = min; position <= maxPosition; position++) {
            builder.put(keyAt(position), valueAt(position));
        }

        return builder.build();
    }

    @Override
    default Map<K, V> skip(int length) {
        return slice(new ImmutableIntRange(length, Integer.MAX_VALUE));
    }

    /**
     * Returns a new Map where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of this map in iteration order.
     * @return A new Map instance just including the first elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length is equal or greater than the
     *         actual size of this collection.
     */
    @Override
    @ToBeAbstract("Returned type is wrong for sorted maps")
    default Map<K, V> take(int length) {
        return (length == 0)? ImmutableHashMap.empty() : slice(new ImmutableIntRange(0, length - 1));
    }

    /**
     * Returns a new Map where the <code>length</code> amount of last elements
     * has been removed.
     * <p>
     * This will return an empty map if the given parameter matches
     * or exceeds the length of this collection.
     *
     * @param length the amount of elements to be removed from the end of the map.
     * @return A new Map instance without the last elements,
     *         the same instance in case the given length is 0,
     *         or an empty instance if the given length is equal or greater
     *         than the actual length of the map.
     */
    @Override
    @ToBeAbstract("This implementation is unable to provide the proper map type in case of sorted map. So the iteration order gets broken")
    default Map<K, V> skipLast(int length) {
        if (length == 0) {
            return this;
        }

        final int size = size();
        final int max = size - length - 1;
        return (max < 0)? ImmutableHashMap.empty() : slice(new ImmutableIntRange(0, max));
    }

    /**
     * Returns a new Map where only the <code>length</code> amount of
     * last elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the end of this map.
     * @return A new Map instance just including the last elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length equals or greater than the
     *         actual size of this collection.
     */
    @ToBeAbstract("Unable to provide the proper type. If it was a sorted map, it sortFunction is lost")
    default Map<K, V> takeLast(int length) {
        final int size = size();
        return (size == 0)? this : (length == 0)? ImmutableHashMap.empty() : slice(new ImmutableIntRange(size - length, size - 1));
    }

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

    @ToBeAbstract("This should be an interface")
    final class Entry<A, B> implements MapEntry<A, B> {
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

        @Override
        public A key() {
            return _key;
        }

        @Override
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
            return equal(_key, that._key) && equal(_value, that._value);
        }
    }
}

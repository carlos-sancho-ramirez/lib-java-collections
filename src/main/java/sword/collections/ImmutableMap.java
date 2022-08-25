package sword.collections;

import sword.annotations.ToBeAbstract;

/**
 * Immutable version of a Map.
 *
 * This Map is immutable, that means that its content cannot be modified once
 * it is created. This also means that, as no algorithms to insert, modify and
 * remove are required, its memory layout can be simplified and its footprint
 * can be reduced in a more optimal way.
 *
 * @param <K> Type for the key elements within the Map
 * @param <V> Type for the value elements within the Map
 */
public interface ImmutableMap<K, V> extends Map<K, V>, ImmutableTransformable<V> {

    @Override
    ImmutableSet<K> keySet();

    @Override
    ImmutableSet<Entry<K, V>> entries();

    ImmutableMap<K, V> put(K key, V value);

    /**
     * Creates a new map containing all the current elements and the ones given in the map.
     *
     * As this is a map, duplicated keys will not be allowed.
     * Than means that elements within the given map will replace any value in this map if
     * there is an equivalent key already included in this map.
     *
     * @param other Map from where new items will be added.
     */
    default ImmutableMap<K, V> putAll(Map<? extends K, ? extends V> other) {
        ImmutableMap<K, V> result = this;
        for (Map.Entry<? extends K, ? extends V> entry : other.entries()) {
            result = result.put(entry.key(), entry.value());
        }

        return result;
    }

    @Override
    ImmutableMap<K, V> filter(Predicate<? super V> predicate);

    @Override
    default ImmutableMap<K, V> filterNot(Predicate<? super V> predicate) {
        return filter(v -> !predicate.apply(v));
    }

    @Override
    @ToBeAbstract("This implementation is unable to provide the proper map type. For example, sorted maps will always receive a hash map as response, which is not suitable")
    default ImmutableMap<K, V> filterByKey(Predicate<? super K> predicate) {
        return (ImmutableMap<K, V>) Map.super.filterByKey(predicate);
    }

    @Override
    @ToBeAbstract("This implementation is unable to provide the proper map type. For example, sorted maps will always receive a hash map as response, which is not suitable")
    default ImmutableMap<K, V> filterByKeyNot(Predicate<? super K> predicate) {
        return (ImmutableMap<K, V>) Map.super.filterByKeyNot(predicate);
    }

    @Override
    @ToBeAbstract("This implementation is unable to provide the proper map type. For example, sorted maps will always receive a hash map as response, which is not suitable")
    default ImmutableMap<K, V> filterByEntry(Predicate<MapEntry<K, V>> predicate) {
        final ReusableMapEntry<K, V> entry = new ReusableMapEntry<>();
        final ImmutableMap.Builder<K, V> builder = new ImmutableHashMap.Builder<>();
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
    ImmutableIntValueMap<K> mapToInt(IntResultFunction<? super V> mapFunc);

    @Override
    <U> ImmutableMap<K, U> map(Function<? super V, ? extends U> mapFunc);

    @Override
    ImmutableMap<K, V> sort(SortFunction<? super K> function);

    @Override
    ImmutableMap<K, V> removeAt(int index);

    @ToBeAbstract("This implementation is unable to provide the proper map type and iteration order for all its subtypes. Sorted maps will become hash maps, and that will modify the order of iteration")
    default ImmutableMap<K, V> slice(ImmutableIntRange range) {
        final int size = size();
        if (size == 0) {
            return this;
        }

        final int min = range.min();
        final int max = range.max();
        if (min >= size || max < 0) {
            return ImmutableHashMap.empty();
        }

        if (range.min() <= 0 && range.max() >= size - 1) {
            return this;
        }

        final ImmutableMap.Builder<K, V> builder = new ImmutableHashMap.Builder<>();
        final int maxPosition = Math.min(max, size - 1);
        for (int position = min; position <= maxPosition; position++) {
            builder.put(keyAt(position), valueAt(position));
        }

        return builder.build();
    }

    @Override
    default ImmutableMap<K, V> skip(int length) {
        return slice(new ImmutableIntRange(length, Integer.MAX_VALUE));
    }

    /**
     * Returns a new ImmutableMap where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of this map in iteration order.
     * @return A new ImmutableMap instance just including the first elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length is equal or greater than the
     *         actual size of this collection.
     */
    @ToBeAbstract("Returned type is wrong for sorted maps")
    default ImmutableMap<K, V> take(int length) {
        return (length != 0)? slice(new ImmutableIntRange(0, length - 1)) :
                isEmpty()? this : ImmutableHashMap.empty();
    }

    /**
     * Returns a new ImmutableMap where the <code>length</code> amount of last
     * elements has been removed.
     * <p>
     * This will return an empty map if the given parameter matches or exceeds
     * the length of this collection.
     *
     * @param length the amount of elements to be removed from the end of the map.
     * @return A new ImmutableMap instance without the last elements,
     *         the same instance in case the given length is 0 or this map is already empty,
     *         or the empty instance if the given length is equal or greater
     *         than the actual length of the set.
     */
    @Override
    @ToBeAbstract("Unable to provide the proper type. If it was a sorted map, it sortFunction is lost")
    default ImmutableMap<K, V> skipLast(int length) {
        final int size = size();
        if (size == 0) {
            return this;
        }

        final int max = size - length - 1;
        return (max < 0)? ImmutableHashMap.empty() : slice(new ImmutableIntRange(0, max));
    }

    /**
     * Returns a new ImmutableMap where only the <code>length</code> amount of
     * last elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the end of this map.
     * @return A new ImmutableMap instance just including the last elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length equals or greater than the
     *         actual size of this collection.
     */
    @ToBeAbstract("Unable to provide the proper type. If it was a sorted map, it sortFunction is lost")
    default ImmutableMap<K, V> takeLast(int length) {
        final int size = size();
        return (size == 0)? this : (length == 0)? ImmutableHashMap.empty() : slice(new ImmutableIntRange(size - length, size - 1));
    }

    interface Builder<K, V> extends MapBuilder<K, V> {
        @Override
        Builder<K, V> put(K key, V value);

        @Override
        ImmutableMap<K, V> build();
    }
}

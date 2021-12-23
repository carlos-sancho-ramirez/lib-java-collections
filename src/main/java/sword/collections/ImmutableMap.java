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

    interface Builder<K, V> extends MapBuilder<K, V> {
        @Override
        Builder<K, V> put(K key, V value);

        @Override
        ImmutableMap<K, V> build();
    }
}

package sword.collections;

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
public interface ImmutableMap<K, V> extends Map<K, V>, IterableImmutableCollection<V> {

    @Override
    ImmutableSet<K> keySet();

    @Override
    ImmutableList<V> valueList();

    @Override
    ImmutableSet<Entry<K, V>> entries();

    ImmutableMap<K, V> put(K key, V value);

    @Override
    ImmutableMap<K, V> filter(Predicate<V> predicate);

    @Override
    ImmutableMap<K, V> filterNot(Predicate<V> predicate);

    @Override
    ImmutableIntValueMap<K> map(IntResultFunction<V> mapFunc);

    @Override
    <U> ImmutableMap<K, U> map(Function<V, U> mapFunc);

    @Override
    ImmutableMap<K, V> sort(SortFunction<K> function);

    interface Builder<K, V> extends MapBuilder<K, V> {
        @Override
        Builder<K, V> put(K key, V value);

        @Override
        ImmutableMap<K, V> build();
    }
}

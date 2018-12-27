package sword.collections;

/**
 * Mutable version of a Map.
 *
 * This Map is mutable, that means that, in contrast with the immutable version,
 * algorithms to insert, modify and remove are required. This may affect its
 * internal memory footprint to ensure easy modification.
 *
 * Implementation of this interfaces will assume that keys inserted are immutable.
 * Mutation of the contained keys may result in duplicates within
 * the keys or wrong sorting of keys.
 * It is not guaranteed to work if keys are mutable.
 * This does no apply to values of the map, that can mutate without risk.
 *
 * @param <K> Type for the key elements within the Map
 * @param <V> Type for the value elements within the Map
 */
public interface MutableMap<K, V> extends Map<K, V>, MutableIterableCollection<V> {

    boolean put(K key, V value);
    boolean remove(K key);

    interface Builder<K, V> extends MapBuilder<K, V> {
        @Override
        Builder<K, V> put(K key, V value);

        @Override
        MutableMap<K, V> build();
    }
}

package sword.collections;

public interface MapBuilder<K, V> {
    MapBuilder<K, V> put(K key, V value);
    Map<K, V> build();
}

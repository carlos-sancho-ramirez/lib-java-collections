package sword.collections;

final class ReusableMapEntry<K, V> implements MapEntry<K, V> {

    private K _key;
    private V _value;

    @Override
    public K key() {
        return _key;
    }

    @Override
    public V value() {
        return _value;
    }

    void set(K key, V value) {
        _key = key;
        _value = value;
    }
}

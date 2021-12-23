package sword.collections;

final class ReusableIntKeyMapEntry<V> implements IntKeyMapEntry<V> {

    private int _key;
    private V _value;

    @Override
    public int key() {
        return _key;
    }

    @Override
    public V value() {
        return _value;
    }

    void set(int key, V value) {
        _key = key;
        _value = value;
    }
}

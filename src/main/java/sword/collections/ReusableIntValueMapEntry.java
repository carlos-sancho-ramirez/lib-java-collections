package sword.collections;

final class ReusableIntValueMapEntry<K> implements IntValueMapEntry<K> {

    private K _key;
    private int _value;

    @Override
    public K key() {
        return _key;
    }

    @Override
    public int value() {
        return _value;
    }

    void set(K key, int value) {
        _key = key;
        _value = value;
    }
}

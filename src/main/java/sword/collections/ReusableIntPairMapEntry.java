package sword.collections;

final class ReusableIntPairMapEntry implements IntPairMapEntry {

    private int _key;
    private int _value;

    @Override
    public int key() {
        return _key;
    }

    @Override
    public int value() {
        return _value;
    }

    void set(int key, int value) {
        _key = key;
        _value = value;
    }
}

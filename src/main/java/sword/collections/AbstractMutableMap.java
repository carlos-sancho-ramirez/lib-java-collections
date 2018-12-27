package sword.collections;

import static sword.collections.SortUtils.*;

abstract class AbstractMutableMap<K, V> extends AbstractMap<K, V> implements MutableMap<K, V> {

    static final int GRANULARITY = DEFAULT_GRANULARITY;

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    Object[] _keys;
    Object[] _values;
    int _size;

    AbstractMutableMap(Object[] keys, Object[] values, int size) {
        _keys = keys;
        _size = size;
        _values = values;
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    @SuppressWarnings("unchecked")
    public K keyAt(int index) {
        return (K) _keys[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public V valueAt(int index) {
        return (V) _values[index];
    }

    @Override
    public List<V> valueList() {
        final int length = _size;
        final Object[] newValues = new Object[length];
        System.arraycopy(_values, 0, newValues, 0, length);
        return new ImmutableList<>(newValues);
    }

    abstract boolean entryLessThan(Entry<K, V> a, Entry<K, V> b);

    @Override
    public Set<Entry<K, V>> entries() {
        final int length = _size;
        final Entry[] entries = new Entry[length];

        for (int index = 0; index < length; index++) {
            entries[index] = new Entry<>(index, _keys[index], _values[index]);
        }

        return new ImmutableSortedSet<>(this::entryLessThan, entries);
    }

    private class Iterator implements Traverser<V> {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V next() {
            return (V) _values[_index++];
        }

        @Override
        public void remove() {
            removeAt(--_index);
        }
    }

    @Override
    public Traverser<V> iterator() {
        return new Iterator();
    }

    @Override
    public boolean remove(K key) {
        int index = indexOfKey(key);
        if (index >= 0) {
            removeAt(index);
            return true;
        }

        return false;
    }
}

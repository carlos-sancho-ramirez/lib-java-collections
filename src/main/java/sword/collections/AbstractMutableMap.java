package sword.collections;

abstract class AbstractMutableMap<K, V> extends AbstractMap<K, V> implements MutableMap<K, V> {

    ArrayLengthFunction _arrayLengthFunction;
    Object[] _keys;
    Object[] _values;
    int _size;

    AbstractMutableMap(ArrayLengthFunction arrayLengthFunction, Object[] keys, Object[] values, int size) {
        _arrayLengthFunction = arrayLengthFunction;
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
        if (index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        return (V) _values[index];
    }

    @Override
    public V last() throws EmptyCollectionException {
        if (_size == 0) {
            throw new EmptyCollectionException();
        }

        return valueAt(_size - 1);
    }

    @Override
    public List<V> toList() {
        final int length = _size;
        final Object[] newValues = new Object[length];
        System.arraycopy(_values, 0, newValues, 0, length);
        return new ImmutableList<>(newValues);
    }

    @Override
    public Map<K, V> sort(SortFunction<? super K> function) {
        final ImmutableSortedMap.Builder<K, V> builder = new ImmutableSortedMap.Builder<>(function);
        for (int i = 0; i < _size; i++) {
            builder.put(keyAt(i), valueAt(i));
        }

        return builder.build();
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

    private class Iterator extends AbstractTransformerWithKey<K, V> {

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

        @Override
        @SuppressWarnings("unchecked")
        public K key() {
            return (K) _keys[_index - 1];
        }
    }

    @Override
    public TransformerWithKey<K, V> iterator() {
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

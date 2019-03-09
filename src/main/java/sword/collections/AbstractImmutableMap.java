package sword.collections;

abstract class AbstractImmutableMap<K, V> extends AbstractMap<K, V> implements ImmutableMap<K, V> {

    final Object[] _keys;
    final Object[] _values;

    AbstractImmutableMap(Object[] keys, Object[] values) {
        _keys = keys;
        _values = values;
    }

    @Override
    public int size() {
        return _keys.length;
    }

    @SuppressWarnings("unchecked")
    public K keyAt(int index) {
        return (K) _keys[index];
    }

    @SuppressWarnings("unchecked")
    public V valueAt(int index) {
        return (V) _values[index];
    }

    @Override
    public ImmutableList<V> toList() {
        return new ImmutableList<>(_values);
    }

    abstract boolean entryLessThan(Entry<K, V> a, Entry<K, V> b);

    @Override
    public ImmutableSortedMap<K, V> sort(SortFunction<K> function) {
        final ImmutableSortedMap.Builder<K, V> builder = new ImmutableSortedMap.Builder<>(function);
        final int size = _keys.length;
        for (int i = 0; i < size; i++) {
            builder.put(keyAt(i), valueAt(i));
        }

        return builder.build();
    }

    @Override
    public ImmutableSet<Entry<K, V>> entries() {
        final int length = _keys.length;
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
            return _index < _keys.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V next() {
            return (V) _values[_index++];
        }

        @Override
        @SuppressWarnings("unchecked")
        public K key() {
            return (K) _keys[_index - 1];
        }
    }

    @Override
    public Iterator iterator() {
        return new Iterator();
    }
}

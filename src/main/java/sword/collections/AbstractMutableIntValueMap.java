package sword.collections;

import static sword.collections.SortUtils.*;

abstract class AbstractMutableIntValueMap<T> extends AbstractIntValueMap<T> implements MutableIntValueMap<T> {

    static final int GRANULARITY = DEFAULT_GRANULARITY;

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    Object[] _keys;
    int[] _values;
    int _size;

    AbstractMutableIntValueMap(Object[] keys, int[] values, int size) {
        _keys = keys;
        _values = values;
        _size = size;
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T keyAt(int index) {
        return (T) _keys[index];
    }

    @Override
    public int valueAt(int index) {
        return _values[index];
    }

    @Override
    public IntList toList() {
        final int length = _size;
        final int[] newValues = new int[length];
        System.arraycopy(_values, 0, newValues, 0, length);
        return new ImmutableIntList(newValues);
    }

    @Override
    public IntValueMap<T> sort(SortFunction<T> function) {
        final ImmutableIntValueSortedMap.Builder<T> builder = new ImmutableIntValueSortedMap.Builder<>(function);
        for (int i = 0; i < _size; i++) {
            builder.put(keyAt(i), valueAt(i));
        }

        return builder.build();
    }

    abstract boolean entryLessThan(Entry<T> a, Entry<T> b);

    @Override
    public Set<Entry<T>> entries() {
        final int length = _size;
        final Entry[] entries = new Entry[length];

        for (int index = 0; index < length; index++) {
            entries[index] = new Entry<>(index, _keys[index], _values[index]);
        }

        return new ImmutableSortedSet<>(this::entryLessThan, entries);
    }

    private class Iterator extends AbstractIntTransformer {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _size;
        }

        @Override
        public Integer next() {
            return _values[_index++];
        }

        @Override
        public void remove() {
            removeAt(--_index);
        }
    }

    @Override
    public IntTransformer iterator() {
        return new Iterator();
    }
}

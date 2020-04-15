package sword.collections;

import sword.collections.SortUtils.SwapMethod;

import static sword.collections.SortUtils.quickSort;

abstract class AbstractImmutableIntValueMap<T> extends AbstractIntValueMap<T> implements ImmutableIntValueMap<T> {

    final Object[] _keys;
    final int[] _values;

    AbstractImmutableIntValueMap(Object[] keys, int[] values) {
        _keys = keys;
        _values = values;
    }

    @Override
    public int size() {
        return _keys.length;
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
    public ImmutableIntList toList() {
        return new ImmutableIntList(_values);
    }

    @Override
    public ImmutableIntValueMap<T> sort(SortFunction<? super T> function) {
        final ImmutableIntValueSortedMap.Builder<T> builder = new ImmutableIntValueSortedMap.Builder<>(function);
        final int size = _keys.length;
        for (int i = 0; i < size; i++) {
            builder.put(keyAt(i), valueAt(i));
        }

        return builder.build();
    }

    abstract boolean entryLessThan(Entry<T> a, Entry<T> b);

    @Override
    public ImmutableSortedSet<Entry<T>> entries() {
        final int length = _keys.length;
        final Entry[] entries = new Entry[length];

        for (int index = 0; index < length; index++) {
            entries[index] = new Entry<>(index, _keys[index], _values[index]);
        }

        return new ImmutableSortedSet<>(this::entryLessThan, entries);
    }

    @Override
    public ImmutableIntPairMap count() {
        MutableIntPairMap result = MutableIntPairMap.empty();
        for (int value : this) {
            final int amount = result.get(value, 0);
            result.put(value, amount + 1);
        }

        return result.toImmutable();
    }

    @Override
    public ImmutableIntKeyMap<T> invert() {
        // TODO: Ensure that no repeated keys are going inside the inverted version
        final int length = _values.length;
        if (length > 1) {
            final int[] values = new int[length];
            final Object[] keys = new Object[length];

            for (int i = 0; i < length; i++) {
                values[i] = _values[i];
                keys[i] = _keys[i];
            }

            quickSort(values, 0, length - 1, new SwapMethod() {
                @Override
                public void apply(int index1, int index2) {
                    int temp = values[index1];
                    values[index1] = values[index2];
                    values[index2] = temp;

                    Object aux = keys[index1];
                    keys[index1] = keys[index2];
                    keys[index2] = aux;
                }
            });
            return new ImmutableIntKeyMap<>(values, keys);
        }
        else {
            return new ImmutableIntKeyMap<>(_values, _keys);
        }
    }

    private class Iterator extends AbstractIntTransformerWithKey<T> {
        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _keys.length;
        }

        @Override
        public Integer next() {
            return _values[_index++];
        }

        @Override
        public T key() {
            return keyAt(_index - 1);
        }
    }

    @Override
    public IntTransformerWithKey<T> iterator() {
        return new Iterator();
    }
}

package sword.collections;

import static sword.collections.SortUtils.DEFAULT_GRANULARITY;

public final class MutableIntList extends AbstractIntIterable implements IntList {

    private static final int GRANULARITY = DEFAULT_GRANULARITY;

    private int[] _values;
    private int _size;

    public static MutableIntList empty() {
        return new MutableIntList(new int[GRANULARITY], 0);
    }

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    MutableIntList(int[] values, int size) {
        _values = values;
        _size = size;
    }

    @Override
    public int get(int key) {
        if (key < 0 || key >= _size) {
            throw new UnmappedKeyException();
        }

        return _values[key];
    }

    @Override
    public int get(int key, int defaultValue) {
        return (key >= 0 && key < _size)? _values[key] : defaultValue;
    }

    @Override
    public IntSet toSet() {
        final ImmutableIntSetBuilder builder = new ImmutableIntSetBuilder();
        for (int i = 0; i < _size; i++) {
            builder.add(_values[i]);
        }
        return builder.build();
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    public ImmutableIntList toImmutable() {
        if (_size == 0) {
            return ImmutableIntList.empty();
        }
        else {
            final int[] values = new int[_size];
            System.arraycopy(_values, 0, values, 0, _size);
            return new ImmutableIntList(values);
        }
    }

    public void append(int value) {
        final int length = suitableArrayLength(_size + 1);
        if (length != _values.length) {
            final int[] values = new int[length];
            System.arraycopy(_values, 0, values, 0, _size);
            _values = values;
        }

        _values[_size++] = value;
    }

    public void appendAll(IntList that) {
        for (int item : that) {
            append(item);
        }
    }

    /**
     * Removes from this collection the item in the given position.
     *
     * If no exception is thrown because of a wrong index, this method will always
     * shrink the size of this collection by 1.
     * @param index Index within this collection for the element to be removed.
     *              It must be between 0 (included) and the value returned by {@link #size()} (excluded)
     * @throws java.lang.IndexOutOfBoundsException if index is negative or it is equal or greater than the current size
     */
    public void removeAt(int index) {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        final int length = suitableArrayLength(--_size);
        if (length != _values.length) {
            final int[] values = new int[length];
            if (index > 0) {
                System.arraycopy(_values, 0, values, 0, index);
            }

            if (index < _size) {
                System.arraycopy(_values, index + 1, values, index, _size - index);
            }
            _values = values;
        }
        else {
            for (int i = index; i < _size; i++) {
                _values[i] = _values[i + 1];
            }
        }
    }

    private class Iterator implements IntTraverser {
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
    public IntTraverser iterator() {
        return new Iterator();
    }

    public static final class Builder implements IntCollectionBuilder<MutableIntList> {

        private final MutableIntList _list = MutableIntList.empty();

        public Builder append(int value) {
            _list.append(value);
            return this;
        }

        @Override
        public Builder add(int value) {
            _list.append(value);
            return this;
        }

        @Override
        public MutableIntList build() {
            return _list;
        }
    }

    @Override
    public int hashCode() {
        final int length = _size;
        int hash = length;

        for (int i = 0; i < length; i++) {
            hash = hash * 31 + _values[i];
        }

        return hash;
    }
}

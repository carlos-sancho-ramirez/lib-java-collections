package sword.collections;

import static sword.collections.SortUtils.DEFAULT_GRANULARITY;

public final class MutableIntList extends AbstractIntTraversable implements IntList, MutableIntTransformable {

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
        final ImmutableIntSetCreator builder = new ImmutableIntSetCreator();
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
    public IntList mapToInt(IntToIntFunction func) {
        if (_size == 0) {
            return ImmutableIntList.empty();
        }

        final int[] newValues = new int[_size];
        for (int i = 0; i < _size; i++) {
            newValues[i] = func.apply(_values[i]);
        }

        return new ImmutableIntList(newValues);
    }

    @Override
    public <U> List<U> map(IntFunction<U> func) {
        if (_size == 0) {
            return ImmutableList.empty();
        }

        final Object[] newValues = new Object[_size];
        for (int i = 0; i < _size; i++) {
            newValues[i] = func.apply(_values[i]);
        }

        return new ImmutableList<>(newValues);
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

    @Override
    public void removeAt(int index) throws IndexOutOfBoundsException {
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

    @Override
    public boolean clear() {
        final int suitableLength = suitableArrayLength(0);
        if (_values.length != suitableLength) {
            _values = new int[suitableLength];
        }

        final boolean changed = _size > 0;
        _size = 0;
        return changed;
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

    @Override
    public IntList toList() {
        return this;
    }

    public static final class Builder implements MutableIntTransformableBuilder<MutableIntList> {

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

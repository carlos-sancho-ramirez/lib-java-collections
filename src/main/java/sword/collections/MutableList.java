package sword.collections;

public final class MutableList<T> extends AbstractIterable<T> implements List<T> {

    private static final int GRANULARITY = 4;

    public static <E> MutableList<E> empty() {
        final Object[] values = new Object[suitableArrayLength(0)];
        return new MutableList<>(values, 0);
    }

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    private Object[] _values;
    private int _size;

    MutableList(Object[] values, int size) {
        _values = values;
        _size = size;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        return (T) _values[index];
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    public int indexOf(T value) {
        return SortUtils.indexOf(_values, _size, value);
    }

    @Override
    public boolean contains(T value) {
        return indexOf(value) >= 0;
    }

    @Override
    public java.util.Iterator<T> iterator() {
        return new Iterator();
    }

    private class Iterator implements java.util.Iterator<T> {
        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            return (T) _values[_index++];
        }

        @Override
        public void remove() {
            removeAt(--_index);
        }
    }

    @Override
    public T findFirst(Predicate<T> predicate, T defaultValue) {
        return SortUtils.findFirst(_values, _size, predicate, defaultValue);
    }

    @Override
    public ImmutableList<T> toImmutable() {
        final Object[] newValues = new Object[_size];
        System.arraycopy(_values, 0, newValues, 0, _size);
        return new ImmutableList<>(newValues);
    }

    @Override
    public MutableList<T> mutate() {
        final Object[] newValues = new Object[_values.length];
        System.arraycopy(_values, 0, newValues, 0, _size);
        return new MutableList<>(newValues, _size);
    }

    public static class Builder<E> implements CollectionBuilder<E> {
        private final MutableList<E> _list = MutableList.empty();

        @Override
        public Builder<E> add(E item) {
            _list.append(item);
            return this;
        }

        @Override
        public MutableList<E> build() {
            return _list;
        }
    }

    private void removeAt(int index) {
        final int arrayLength = suitableArrayLength(--_size);
        if (arrayLength != _values.length) {
            Object[] newValues = new Object[arrayLength];

            if (index > 0) {
                System.arraycopy(_values, 0, newValues, 0, index);
            }

            if (_size > index) {
                System.arraycopy(_values, index + 1, newValues, index, _size - index);
            }
            _values = newValues;
        }
        else {
            for (int i = index; i < _size; i++) {
                _values[i] = _values[i + 1];
            }
        }
    }

    /**
     * Inserts the given value in the given position.
     *
     * This will shift all elements with higher index or equal index in one,
     * in order to make space for the new value.
     * @param index Index where this value wants to be included.
     *              Index must be between zero and the value returned by {@link #size()}, both included.
     * @param value Value to be inserted in that position.
     * @throws IndexOutOfBoundsException if the given index is not within the expected range.
     */
    public void insert(int index, T value) {
        if (index < 0 || index > _size) {
            throw new IndexOutOfBoundsException();
        }

        final int arrayLength = suitableArrayLength(_size + 1);
        if (arrayLength != _values.length) {
            Object[] newValues = new Object[arrayLength];
            if (index > 0) {
                System.arraycopy(_values, 0, newValues, 0, index);
            }
            if (_size > index) {
                System.arraycopy(_values, index, newValues, index + 1, _size - index);
            }
            _values = newValues;
        }
        else {
            for (int i = _size; i > index; i--) {
                _values[i] = _values[i - 1];
            }
        }
        _values[index] = value;
        ++_size;
    }

    public void prepend(T item) {
        insert(0, item);
    }

    public void append(T item) {
        insert(_size, item);
    }

    public void appendAll(List<T> that) {
        if (that == null || that.isEmpty()) {
            return;
        }

        final int arrayLength = suitableArrayLength(_size + that.size());
        if (arrayLength != _values.length) {
            Object[] newValues = new Object[arrayLength];
            System.arraycopy(_values, 0, newValues, 0, _values.length);
            _values = newValues;
        }

        for (T item : that) {
            _values[_size++] = item;
        }
    }

    @Override
    public int hashCode() {
        int hash = _size;

        for (int i = 0; i < _size; i++) {
            final Object item = _values[i];
            hash = hash * 31 + ((item != null)? item.hashCode() : 0);
        }

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof MutableList)) {
            return false;
        }
        else if (this == object) {
            return true;
        }

        final MutableList that = (MutableList) object;
        final int length = that._size;
        if (length != _size) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            final Object thisValue = _values[i];
            final Object thatValue = that._values[i];

            if (thisValue == null && thatValue != null || thisValue != null && !thisValue.equals(thatValue)) {
                return false;
            }
        }

        return true;
    }
}

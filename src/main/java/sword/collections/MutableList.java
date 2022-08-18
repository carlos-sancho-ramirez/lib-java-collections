package sword.collections;

import static sword.collections.SortUtils.equal;

public final class MutableList<T> extends AbstractTraversable<T> implements List<T>, MutableTraversable<T> {

    public static <E> MutableList<E> empty(ArrayLengthFunction arrayLengthFunction) {
        final int arrayLength = arrayLengthFunction.suitableArrayLength(0, 0);
        return new MutableList<>(arrayLengthFunction, new Object[arrayLength], 0);
    }

    public static <E> MutableList<E> empty() {
        return empty(GranularityBasedArrayLengthFunction.getInstance());
    }

    private final ArrayLengthFunction _arrayLengthFunction;
    private Object[] _values;
    private int _size;

    MutableList(ArrayLengthFunction arrayLengthFunction, Object[] values, int size) {
        _arrayLengthFunction = arrayLengthFunction;
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
    @SuppressWarnings("unchecked")
    public T valueAt(int index) {
        if (index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        return (T) _values[index];
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    public T last() throws EmptyCollectionException {
        if (_size == 0) {
            throw new EmptyCollectionException();
        }

        return valueAt(_size - 1);
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
    public Transformer<T> iterator() {
        return new Iterator();
    }

    @Override
    public List<T> toList() {
        return this;
    }

    /**
     * Create a new mutable list instance and copy from this collection the actual data references. After it, this collection gets cleared.
     * <p>
     * This is a more efficient alternative to the following code:
     * <code>
     * <br>MutableList newList = list.mutate();
     * <br>list.clear();
     * </code>
     *
     * @return A new mutable list that contains the actual data of this map.
     */
    public MutableList<T> donate() {
        final MutableList<T> newList = new MutableList<>(_arrayLengthFunction, _values, _size);
        final int length = _arrayLengthFunction.suitableArrayLength(0, 0);
        _values = new Object[length];
        _size = 0;
        return newList;
    }

    private class Iterator extends AbstractTransformer<T> {
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
    public <E> List<E> map(Function<? super T, ? extends E> func) {
        return new MapResultList<>(this, func);
    }

    @Override
    public ImmutableList<T> toImmutable() {
        final Object[] newValues = new Object[_size];
        System.arraycopy(_values, 0, newValues, 0, _size);
        return new ImmutableList<>(newValues);
    }

    @Override
    public MutableList<T> mutate(ArrayLengthFunction arrayLengthFunction) {
        final Object[] newValues = new Object[arrayLengthFunction.suitableArrayLength(0, _size)];
        System.arraycopy(_values, 0, newValues, 0, _size);
        return new MutableList<>(arrayLengthFunction, newValues, _size);
    }

    @Override
    public MutableList<T> mutate() {
        return mutate(_arrayLengthFunction);
    }

    /**
     * Sort the items in the connection according to the given function.
     *
     * @param function Used to sort the elements within this list.
     * @return Whether if the collection has changed.
     */
    public boolean arrange(SortFunction<T> function) {
        final int length = _size;
        if (length < 2) {
            return false;
        }

        final Object[] newValues = new Object[_values.length];
        newValues[0] = _values[0];
        boolean changed = false;

        for (int i = 1; i < length; i++) {
            final int index = SortUtils.findSuitableIndex(function, newValues, i, (T) _values[i]);
            changed |= index != i;
            for (int j = i; j > index; j--) {
                newValues[j] = newValues[j - 1];
            }
            newValues[index] = _values[i];
        }

        _values = newValues;
        return changed;
    }

    public static class Builder<E> implements ListBuilder<E>, MutableTraversableBuilder<E> {
        private final MutableList<E> _list;

        public Builder(ArrayLengthFunction arrayLengthFunction) {
            _list = MutableList.empty(arrayLengthFunction);
        }

        public Builder() {
            _list = MutableList.empty();
        }

        @Override
        public Builder<E> append(E item) {
            _list.append(item);
            return this;
        }

        @Override
        public Builder<E> add(E item) {
            return append(item);
        }

        @Override
        public MutableList<E> build() {
            return _list;
        }
    }

    @Override
    public void removeAt(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        final int arrayLength = _arrayLengthFunction.suitableArrayLength(_values.length, --_size);
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

    @Override
    public boolean clear() {
        final int suitableLength = _arrayLengthFunction.suitableArrayLength(_values.length, 0);
        if (_values.length != suitableLength) {
            _values = new Object[suitableLength];
        }
        else {
            for (int i = 0; i < _size; i++) {
                _values[i] = null;
            }
        }

        final boolean changed = _size > 0;
        _size = 0;

        return changed;
    }

    /**
     * Replace the value in the given index by the one provided here.
     *
     * This method will never modify the size of this collection.
     * It will always replace an existing value in the given index.
     *
     * @param index Index where this value must be replaced.
     *              Index must be between zero (included) and the value returned by {@link #size()} (excluded).
     * @param value Value to be set in that position.
     * @return True if this operation modified the collection values.
     *         False if the value provided matches the one it was there before.
     * @throws IndexOutOfBoundsException if the given index is not within the expected range.
     */
    public boolean put(int index, T value) {
        final boolean result = !equal(_values[index], value);
        _values[index] = value;
        return result;
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

        final int arrayLength = _arrayLengthFunction.suitableArrayLength(_values.length, _size + 1);
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

    public void appendAll(Iterable<T> that) {
        if (that == null) {
            return;
        }

        if (that instanceof Sizable) {
            final int arrayLength = _arrayLengthFunction.suitableArrayLength(_values.length, _size + ((Sizable) that).size());
            if (arrayLength != _values.length) {
                Object[] newValues = new Object[arrayLength];
                System.arraycopy(_values, 0, newValues, 0, _values.length);
                _values = newValues;
            }

            for (T item : that) {
                _values[_size++] = item;
            }
        }
        else {
            for (T item : that) {
                append(item);
            }
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
        if (!(object instanceof MutableList)) {
            return super.equals(object);
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

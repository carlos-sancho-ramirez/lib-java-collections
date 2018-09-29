package sword.collections;

import static sword.collections.SortUtils.DEFAULT_GRANULARITY;
import static sword.collections.SortUtils.equal;

public final class MutableList<T> extends AbstractIterable<T> implements List<T> {

    private static final int GRANULARITY = DEFAULT_GRANULARITY;

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
    @SuppressWarnings("unchecked")
    public T valueAt(int index) {
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
    public Traverser<T> iterator() {
        return new Iterator();
    }

    private class Iterator implements Traverser<T> {
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
    public Set<T> toSet() {
        final ImmutableHashSet.Builder<T> builder = new ImmutableHashSet.Builder<>();
        for (int i = 0; i < _size; i++) {
            builder.add(valueAt(i));
        }

        return builder.build();
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

    /**
     * Sorts this collection according to the given function.
     * @param function Used to sort the elements within this list.
     * @return Whether if the collection has changed.
     */
    public boolean sort(SortFunction<T> function) {
        final int length = _size;
        if (length < 2) {
            return false;
        }

        final Object[] newValues = new Object[length];
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

    /**
     * Removes from this collection the item in the given position.
     *
     * If no exception is thrown because of a wrong index, this method will always
     * shrink the size of this collection by 1.
     * @param index Index within this collection for the element to be removed.
     *              It must be between 0 (included) and the value returned by {@link #size()} (excluded)
     */
    public void removeAt(int index) {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

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

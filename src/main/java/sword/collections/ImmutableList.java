package sword.collections;

import java.util.Collection;

public final class ImmutableList<T> extends AbstractImmutableIterable<T> implements List<T> {

    private static final ImmutableList<Object> EMPTY = new ImmutableList<>(new Object[0]);

    @SuppressWarnings("unchecked")
    public static <E> ImmutableList<E> empty() {
        return (ImmutableList<E>) EMPTY;
    }

    public static <E> ImmutableList<E> singleton(E item) {
        return new ImmutableList<>(new Object[] { item });
    }

    private final Object[] _values;

    ImmutableList(Object[] values) {
        _values = values;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) _values[index];
    }

    @Override
    public int size() {
        return _values.length;
    }

    @Override
    public int indexOf(T value) {
        return SortUtils.indexOf(_values, _values.length, value);
    }

    /**
     * Return true if an equivalent item is found in the list, this means
     * that it will be true if calling {@link Object#equals(Object)} with
     * this value returns true on any of the item.
     *
     * @param value Value to check
     */
    @Override
    public boolean contains(T value) {
        return indexOf(value) >= 0;
    }

    @Override
    public ImmutableList<T> filter(Predicate<T> predicate) {
        return (ImmutableList<T>) super.filter(predicate);
    }

    @Override
    public ImmutableList<T> filterNot(Predicate<T> predicate) {
        return (ImmutableList<T>) super.filterNot(predicate);
    }

    /**
     * Returns a new ImmutableList of the same type where the
     * <code>length</code> amount of first elements has been removed.
     * <p>
     * This will return an empty list if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the start of the list.
     * @return A new ImmutableList instance without the first elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance of the given length is equal or greater
     *         than the actual length of the list.
     */
    public ImmutableList<T> skip(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Unable to skip a negative number of elements");
        }
        else if (length == 0) {
            return this;
        }
        else if (length >= _values.length) {
            return empty();
        }

        final int remain = _values.length - length;
        Object[] newValues = new Object[remain];
        System.arraycopy(_values, length, newValues, 0, remain);
        return new ImmutableList<>(newValues);
    }

    /**
     * Method that iterates only once the collection and creates a pair of lists.
     * The left list in the pair will contain all elements that satisfies the given predicate,
     * while the right list in the pair will contain all elements that will not satisfy the predicate.
     *
     * This method is equivalent to:
     * <br>ImmutableList&lt;U&gt; left = this.filter(predicate);
     * <br>ImmutableList&lt;U&gt; right = this.filterNot(predicate);
     *
     * However this method only iterates once the collection, which makes it more efficient,
     * especially for large collections.
     *
     * @param predicate Condition to evaluate for each element. If true, the element will be
     *                  included in the 'left' list, if false, it will be included in the 'right' list.
     * @return An {@link ImmutablePair} of lists. This will never be null.
     */
    public ImmutablePair<ImmutableList<T>, ImmutableList<T>> span(Predicate<T> predicate) {
        boolean allTrue = true;
        boolean allFalse = true;

        Builder<T> builderForTrue = newBuilder();
        Builder<T> builderForFalse = newBuilder();

        for (T item : this) {
            if (predicate.apply(item)) {
                allFalse = false;
                builderForTrue.add(item);
            }
            else {
                allTrue = false;
                builderForFalse.add(item);
            }
        }

        if (allTrue) {
            final ImmutableList<T> empty = empty();
            return new ImmutablePair<>(this, empty);
        }
        else if (allFalse) {
            final ImmutableList<T> empty = empty();
            return new ImmutablePair<>(empty, this);
        }
        else {
            return new ImmutablePair<>(builderForTrue.build(), builderForFalse.build());
        }
    }

    @Override
    public <U> ImmutableList<U> map(Function<T, U> func) {
        return (ImmutableList<U>) super.map(func);
    }

    @Override
    public java.util.Iterator<T> iterator() {
        return new Iterator();
    }

    @Override
    <U> Builder<U> newBuilder() {
        return new Builder<U>();
    }

    @Override
    public ImmutableList<T> toImmutable() {
        return this;
    }

    @Override
    public MutableList<T> mutate() {
        final int length = _values.length;
        final int newLength = MutableList.suitableArrayLength(length);

        Object[] values = new Object[newLength];
        System.arraycopy(_values, 0, values, 0, length);

        return new MutableList<>(values, length);
    }

    private class Iterator extends IteratorForImmutable<T> {
        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _values.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            return (T) _values[_index++];
        }
    }

    public static class Builder<E> implements ImmutableCollectionBuilder<E> {
        private static final int DEFAULT_GRANULARITY = 12;
        private final int _granularity;
        private int _size;
        private Object[] _values;

        public Builder() {
            _granularity = DEFAULT_GRANULARITY;
            _values = new Object[_granularity];
        }

        public Builder(int length) {
            _granularity = (length >= 0)? length : DEFAULT_GRANULARITY;
            _values = new Object[(length != 0)? _granularity : 0];
        }

        private void enlargeArray() {
            Object[] oldValues = _values;
            _values = new Object[_size + _granularity];
            System.arraycopy(oldValues, 0, _values, 0, _size);
        }

        public Builder<E> append(E item) {
            if (_size == _values.length) {
                enlargeArray();
            }

            _values[_size++] = item;
            return this;
        }

        @Override
        public Builder<E> add(E item) {
            return append(item);
        }

        @Override
        public ImmutableList<E> build() {
            if (_size == 0) {
                return empty();
            }

            final Object[] values;
            if (_size == _values.length) {
                values = _values;
            }
            else {
                values = new Object[_size];
                System.arraycopy(_values, 0, values, 0, _size);
            }

            return new ImmutableList<E>(values);
        }
    }

    public static <E> ImmutableList<E> from(E[] items) {
        return new ImmutableList<E>(items);
    }

    public static <E> ImmutableList<E> from(Collection<E> collection) {
        final ImmutableList.Builder<E> builder = new ImmutableList.Builder<>(collection.size());
        for (E token : collection) {
            builder.append(token);
        }

        return builder.build();
    }

    /**
     * Creates a new {@link ImmutableList} of the same type where the given
     * value is included at the end of the list.
     *
     * @param item item to be included at the end of the list.
     */
    public ImmutableList<T> append(T item) {
        final int length = _values.length;
        final Object[] newValues = new Object[length + 1];
        System.arraycopy(_values, 0, newValues, 0, length);
        newValues[length] = item;
        return new ImmutableList<T>(newValues);
    }

    /**
     * Creates a new {@link ImmutableList} of the same type where the given
     * value is included at the beginning of the list.
     *
     * @param item item to be included at the beginning of the list.
     */
    public ImmutableList<T> prepend(T item) {
        final int length = _values.length;
        final Object[] newValues = new Object[length + 1];
        System.arraycopy(_values, 0, newValues, 1, length);
        newValues[0] = item;
        return new ImmutableList<T>(newValues);
    }

    public ImmutableList<T> appendAll(ImmutableList<T> that) {
        if (that == null || that.isEmpty()) {
            return this;
        }
        else if (isEmpty()) {
            return that;
        }
        else {
            final Builder<T> builder = new Builder<T>();
            final int thisLength = _values.length;

            for (int i = 0; i < thisLength; i++) {
                builder.append(get(i));
            }

            for (T item : that) {
                builder.append(item);
            }

            return builder.build();
        }
    }

    @Override
    public int hashCode() {
        final int length = _values.length;
        int hash = length;

        for (int i = 0; i < length; i++) {
            final Object item = _values[i];
            hash = hash * 31 + ((item != null)? item.hashCode() : 0);
        }

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof ImmutableList)) {
            return false;
        }
        else if (this == object) {
            return true;
        }

        final ImmutableList that = (ImmutableList) object;
        final int length = that._values.length;
        if (length != _values.length) {
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

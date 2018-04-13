package sword.collections;

public class ImmutableIntList extends AbstractImmutableIntIterable {

    private static final ImmutableIntList EMPTY = new ImmutableIntList(new int[0]);

    public static ImmutableIntList empty() {
        return EMPTY;
    }

    private final int[] _values;

    private ImmutableIntList(int[] values) {
        _values = values;
    }

    public int get(int index) {
        return _values[index];
    }

    @Override
    public int valueAt(int index) {
        return _values[index];
    }

    @Override
    public int size() {
        return _values.length;
    }

    private int indexOf(int value) {
        for (int i = 0; i < _values.length; i++) {
            if (value == _values[i]) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean contains(int value) {
        return indexOf(value) >= 0;
    }

    @Override
    public ImmutableIntList filter(IntPredicate predicate) {
        return (ImmutableIntList) super.filter(predicate);
    }

    @Override
    public ImmutableIntList filterNot(IntPredicate predicate) {
        return (ImmutableIntList) super.filterNot(predicate);
    }

    @Override
    public <U> ImmutableList<U> map(IntFunction<U> func) {
        return (ImmutableList<U>) super.map(func);
    }

    /**
     * Reduces the collection to a single element by applying the given function on each pair of values.
     * @param func Associate function to be applied on each pair of elements.
     * @return The resulting value of applying the given function to each value pair.
     */
    public int reduce(ReduceIntFunction func) {
        final int size = _values.length;
        if (size == 0) {
            throw new UnsupportedOperationException("Unable to reduce an empty collection");
        }

        if (size == 1) {
            return _values[0];
        }

        int result = func.apply(_values[0], _values[1]);
        for (int i = 2; i < size; i++) {
            result = func.apply(result, _values[i]);
        }

        return result;
    }

    @Override
    public java.util.Iterator<Integer> iterator() {
        return new Iterator();
    }

    @Override
    Builder newIntBuilder() {
        return new Builder();
    }

    @Override
    <U> ImmutableList.Builder<U> newBuilder() {
        return new ImmutableList.Builder<U>();
    }

    private class Iterator extends IteratorForImmutable<Integer> {
        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _values.length;
        }

        @Override
        public Integer next() {
            return _values[_index++];
        }
    }

    /**
     * Returns the first value matching the predicate or the default value if none matches.
     */
    public int findFirst(IntPredicate predicate, int defaultValue) {
        final int length = _values.length;
        for (int i = 0; i < length; i++) {
            final int item = _values[i];
            if (predicate.apply(item)) {
                return item;
            }
        }

        return defaultValue;
    }

    public static class Builder implements ImmutableIntCollectionBuilder<ImmutableIntList> {
        private static final int DEFAULT_GRANULARITY = 12;
        private final int _granularity;
        private int _size;
        private int[] _values;

        public Builder() {
            _granularity = DEFAULT_GRANULARITY;
            _values = new int[_granularity];
        }

        public Builder(int length) {
            _granularity = (length >= 0)? length : DEFAULT_GRANULARITY;
            _values = new int[(length != 0)? _granularity : 0];
        }

        private void enlargeArray() {
            int[] oldValues = _values;
            _values = new int[_size + _granularity];
            System.arraycopy(oldValues, 0, _values, 0, _size);
        }

        public Builder append(int item) {
            if (_size == _values.length) {
                enlargeArray();
            }

            _values[_size++] = item;
            return this;
        }

        @Override
        public Builder add(int value) {
            return append(value);
        }

        @Override
        public ImmutableIntList build() {
            if (_size == 0) {
                return empty();
            }

            final int[] values;
            if (_size == _values.length) {
                values = _values;
            }
            else {
                values = new int[_size];
                System.arraycopy(_values, 0, values, 0, _size);
            }

            return new ImmutableIntList(values);
        }
    }

    /**
     * Creates a new {@link ImmutableIntList} of the same type where the given
     * value is included at the end of the list.
     *
     * @param value Item to be included at the end of the list.
     */
    public ImmutableIntList append(int value) {
        final int length = _values.length;
        final int[] newValues = new int[length + 1];
        System.arraycopy(_values, 0, newValues, 0, length);
        newValues[length] = value;
        return new ImmutableIntList(newValues);
    }

    public ImmutableIntList appendAll(ImmutableIntList that) {
        if (that == null || that.isEmpty()) {
            return this;
        }
        else if (isEmpty()) {
            return that;
        }
        else {
            final Builder builder = new Builder();
            final int thisLength = _values.length;

            for (int i = 0; i < thisLength; i++) {
                builder.append(get(i));
            }

            for (int item : that) {
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
            hash = hash * 31 + _values[i];
        }

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof ImmutableIntList)) {
            return false;
        }
        else if (this == object) {
            return true;
        }

        final ImmutableIntList that = (ImmutableIntList) object;
        final int length = that._values.length;
        if (length != _values.length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            final int thisValue = _values[i];
            final int thatValue = that._values[i];

            if (thisValue != thatValue) {
                return false;
            }
        }

        return true;
    }
}

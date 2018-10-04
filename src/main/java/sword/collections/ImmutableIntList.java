package sword.collections;

public final class ImmutableIntList extends AbstractImmutableIntIterable implements IntList {

    private static final ImmutableIntList EMPTY = new ImmutableIntList(new int[0]);

    public static ImmutableIntList empty() {
        return EMPTY;
    }

    private final int[] _values;

    ImmutableIntList(int[] values) {
        _values = values;
    }

    @Override
    public int get(int key) {
        if (key < 0 || key >= _values.length) {
            throw new UnmappedKeyException();
        }

        return _values[key];
    }

    @Override
    public int get(int key, int defaultValue) {
        return (key >= 0 && key < _values.length)? _values[key] : defaultValue;
    }

    @Override
    public ImmutableIntSet toSet() {
        final ImmutableIntSetBuilder builder = new ImmutableIntSetBuilder();
        for (int i = 0; i < _values.length; i++) {
            builder.add(_values[i]);
        }
        return builder.build();
    }

    @Override
    public ImmutableIntList toImmutable() {
        return this;
    }

    @Override
    public int valueAt(int index) {
        return _values[index];
    }

    @Override
    public int size() {
        return _values.length;
    }

    @Override
    public int indexOf(int value) {
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

    @Override
    public int reduce(IntReduceFunction func) {
        final int size = _values.length;
        if (size == 0) {
            throw new EmptyCollectionException();
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
    public IntTraverser iterator() {
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

    private class Iterator implements IntTraverser {
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
        private final MutableIntList _list = MutableIntList.empty();

        public Builder append(int item) {
            _list.append(item);
            return this;
        }

        @Override
        public Builder add(int value) {
            _list.append(value);
            return this;
        }

        @Override
        public ImmutableIntList build() {
            return _list.toImmutable();
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

    public ImmutableIntList appendAll(IntList that) {
        if (that == null || that.isEmpty()) {
            return this;
        }
        else if (isEmpty()) {
            return that.toImmutable();
        }
        else {
            final Builder builder = new Builder();
            final int thisLength = _values.length;

            for (int i = 0; i < thisLength; i++) {
                builder.append(_values[i]);
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
}

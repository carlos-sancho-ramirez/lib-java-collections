package sword.collections;

/**
 * Sorted set of integers for a given range.
 */
public final class ImmutableIntRange extends AbstractImmutableIntSet {

    private final int _min;
    private final int _max;

    public ImmutableIntRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException();
        }

        _min = min;
        _max = max;
    }

    @Override
    public int size() {
        return _max - _min + 1;
    }

    @Override
    public int indexOf(int value) {
        return (value >= _min && value <= _max)? value - _min : -1;
    }

    @Override
    public int valueAt(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        return _min + index;
    }

    @Override
    public int min() {
        return _min;
    }

    @Override
    public int max() {
        return _max;
    }

    @Override
    public boolean contains(int value) {
        return value >= _min && value <= _max;
    }

    @Override
    public ImmutableIntRange toImmutable() {
        return this;
    }

    @Override
    public MutableIntArraySet mutate() {
        return MutableIntArraySet.fromIntRange(this);
    }

    @Override
    public <U> ImmutableList<U> map(IntFunction<? extends U> func) {
        final int size = size();
        final Object[] newValues = new Object[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = func.apply(_min + i);
        }

        return new ImmutableList<>(newValues);
    }

    @Override
    public ImmutableIntList mapToInt(IntToIntFunction func) {
        final int size = size();
        final int[] newValues = new int[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = func.apply(_min + i);
        }

        return new ImmutableIntList(newValues);
    }

    @Override
    public ImmutableIntSet add(int value) {
        if (value >= _min && value <= _max) {
            return this;
        }

        if (value == _min - 1 && _min != Integer.MIN_VALUE) {
            return new ImmutableIntRange(value, _max);
        }

        if (value == _max + 1 && _max != Integer.MAX_VALUE) {
            return new ImmutableIntRange(_min, value);
        }

        final ImmutableIntSetCreator builder = new ImmutableIntSetCreator();
        for (int i = _min; i < _max; i++) {
            builder.add(i);
        }
        builder.add(_max);

        return builder.add(value).build();
    }

    /**
     * Returns a new ImmutableIntSet where the <code>length</code> amount of last
     * elements has been removed.
     * <p>
     * This will return an empty set if the given parameter matches or exceeds
     * the length of this collection.
     *
     * @param length the amount of elements to be removed from the end of the set.
     * @return A new ImmutableIntSet instance without the last elements,
     *         the same instance in case the given length is 0 or this set is already empty,
     *         or the empty instance if the given length is equal or greater
     *         than the actual length of the set.
     */
    public ImmutableIntSet skipLast(int length) {
        if (length == 0) {
            return this;
        }

        final int newMax = _max - length;
        return (newMax < _max && newMax >= _min)? new ImmutableIntRange(_min, _max - length) :
                ImmutableIntArraySet.empty();
    }

    @Override
    public ImmutableIntSet removeAt(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        return remove(_min + index);
    }

    @Override
    public ImmutableIntSet remove(int value) {
        if (value < _min || value > _max) {
            return this;
        }

        if (_min == _max) {
            return ImmutableIntArraySet.empty();
        }

        if (value == _min) {
            return new ImmutableIntRange(value + 1, _max);
        }

        if (value == _max) {
            return new ImmutableIntRange(_min, value - 1);
        }

        final ImmutableIntSetCreator builder = new ImmutableIntSetCreator();
        for (int i = _min; i < _max; i++) {
            if (i != value) {
                builder.add(i);
            }
        }
        builder.add(_max);

        return builder.build();
    }

    @Override
    public ImmutableIntList toList() {
        final int length = _max - _min + 1;
        final int[] values = new int[length];
        for (int i = 0; i < length; i++) {
            values[i] = _min + i;
        }

        return new ImmutableIntList(values);
    }

    @Override
    public IntTransformer iterator() {
        return new Iterator();
    }

    private class Iterator extends AbstractIntTransformer {
        private int _value;

        Iterator() {
            _value = _min;
        }

        @Override
        public boolean hasNext() {
            return _value <= _max;
        }

        @Override
        public Integer next() {
            return _value++;
        }
    }
}

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
    public MutableIntSet mutate() {
        return MutableIntSet.fromIntRange(this);
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

        final ImmutableIntSetBuilder builder = new ImmutableIntSetBuilder();
        for (int i = _min; i < _max; i++) {
            builder.add(i);
        }
        builder.add(_max);

        return builder.add(value).build();
    }

    @Override
    public ImmutableIntSet remove(int value) {
        if (value < _min || value > _max) {
            return this;
        }

        if (_min == _max) {
            return ImmutableIntSetImpl.empty();
        }

        if (value == _min) {
            return new ImmutableIntRange(value + 1, _max);
        }

        if (value == _max) {
            return new ImmutableIntRange(_min, value - 1);
        }

        final ImmutableIntSetBuilder builder = new ImmutableIntSetBuilder();
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
    public java.util.Iterator<Integer> iterator() {
        return new Iterator();
    }

    private class Iterator implements java.util.Iterator<Integer> {

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

package sword.collections;

import java.util.Arrays;

import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;

/**
 * Efficient implementation for an immutable Set when few integers are included.
 * 'Set' must be understood as a collection where its elements cannot be repeated.
 *
 * This Set is immutable, that means that its content cannot be modified once
 * it is created. This also means that, as no algorithms to insert, modify and
 * remove are required, its memory layout can be simplified and its footprint
 * can be reduced in a more optimal way.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should create a builder in order to obtain
 * an instance of it.
 *
 * This class also implements the {@link Iterable} interface, which
 * ensures that the for-each construction can be used.
 */
final class ImmutableIntSetImpl extends AbstractImmutableIntSet {

    private static final ImmutableIntSetImpl EMPTY = new ImmutableIntSetImpl(new int[0]);

    public static ImmutableIntSetImpl empty() {
        return EMPTY;
    }

    /**
     * Sorted set of integers
     */
    private final int[] _values;

    ImmutableIntSetImpl(int[] values) {
        _values = values;
    }

    @Override
    public boolean contains(int value) {
        return indexOf(value) >= 0;
    }

    @Override
    public int indexOf(int value) {
        return findKey(_values, _values.length, value);
    }

    @Override
    public int size() {
        return _values.length;
    }

    @Override
    public <U> ImmutableList<U> map(IntFunction<U> func) {
        final int size = _values.length;
        if (size == 0) {
            return ImmutableList.empty();
        }

        final Object[] newValues = new Object[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = func.apply(_values[i]);
        }

        return new ImmutableList<>(newValues);
    }

    @Override
    public ImmutableIntList mapToInt(IntToIntFunction func) {
        final int size = _values.length;
        if (size == 0) {
            return ImmutableIntList.empty();
        }

        final int[] newValues = new int[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = func.apply(_values[i]);
        }

        return new ImmutableIntList(newValues);
    }

    @Override
    public ImmutableIntSet add(int value) {
        final int length = _values.length;
        if (findKey(_values, length, value) >= 0) {
            return this;
        }

        if (length == 0) {
            int[] newValues = new int[1];
            newValues[0] = value;
            return new ImmutableIntSetImpl(newValues);
        }

        if (ImmutableIntSetBuilder.betterAsBitSet(_values[0], _values[length - 1], _values.length)) {
            ImmutableBitSetImpl.Builder builder = new ImmutableBitSetImpl.Builder();
            for (int i = 0; i < length; i++) {
                builder.add(_values[i]);
            }
            builder.add(value);
            return builder.build();
        }
        else {
            final int index = findSuitableIndex(_values, length, value);

            final int newLength = _values.length + 1;
            final int[] newValues = new int[newLength];

            for (int i = 0; i < newLength; i++) {
                newValues[i] = (i < index) ? _values[i] : (i == index) ? value : _values[i - 1];
            }

            return new ImmutableIntSetImpl(newValues);
        }
    }

    @Override
    public ImmutableIntSetImpl remove(int value) {
        final int length = _values.length;
        final int index = findKey(_values, length, value);
        if (index < 0) {
            return this;
        }

        final int[] result = new int[length - 1];
        System.arraycopy(_values, 0, result, 0, index);
        System.arraycopy(_values, index + 1, result, index, length - index - 1);

        return new ImmutableIntSetImpl(result);
    }

    @Override
    public ImmutableIntList toList() {
        return new ImmutableIntList(_values);
    }

    @Override
    public IntTransformer iterator() {
        return new IntIterator();
    }

    static ImmutableIntSetImpl fromMutableIntSet(MutableIntSet set) {
        final int length = set.size();
        if (length == 0) {
            return empty();
        }

        final int[] values = new int[length];
        for (int i = 0; i < length; i++) {
            values[i] = set.valueAt(i);
        }

        return new ImmutableIntSetImpl(values);
    }

    @Override
    public int valueAt(int index) {
        return _values[index];
    }

    @Override
    public int min() throws EmptyCollectionException {
        if (_values.length == 0) {
            throw new EmptyCollectionException();
        }

        return _values[0];
    }

    @Override
    public int max() throws EmptyCollectionException {
        final int length = _values.length;
        if (length == 0) {
            throw new EmptyCollectionException();
        }

        return _values[length - 1];
    }

    @Override
    public ImmutableIntSet toImmutable() {
        return this;
    }

    @Override
    public MutableIntSet mutate() {
        return MutableIntSet.fromIntSet(this);
    }

    static class Builder implements ImmutableIntSet.Builder {

        private final MutableIntSet _set = MutableIntSet.empty();

        @Override
        public Builder add(int value) {
            _set.add(value);
            return this;
        }

        @Override
        public ImmutableIntSetImpl build() {
            return fromMutableIntSet(_set);
        }
    }

    private class IntIterator extends AbstractIntTransformer {
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

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ImmutableIntSetImpl)) {
            return super.equals(other);
        }

        final ImmutableIntSetImpl that = (ImmutableIntSetImpl) other;
        return Arrays.equals(_values, that._values);
    }
}

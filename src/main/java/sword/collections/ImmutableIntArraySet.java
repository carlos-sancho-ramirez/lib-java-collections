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
public final class ImmutableIntArraySet extends AbstractImmutableIntSet {

    private static final ImmutableIntArraySet EMPTY = new ImmutableIntArraySet(new int[0]);

    public static ImmutableIntArraySet empty() {
        return EMPTY;
    }

    /**
     * Sorted set of integers
     */
    private final int[] _values;

    ImmutableIntArraySet(int[] values) {
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
    public int last() throws EmptyCollectionException {
        final int size = _values.length;
        if (size == 0) {
            throw new EmptyCollectionException();
        }

        return _values[size - 1];
    }

    @Override
    public <U> ImmutableList<U> map(IntFunction<? extends U> func) {
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
    public <E> ImmutableIntKeyMap<E> assign(IntFunction<? extends E> function) {
        final int size = _values.length;
        if (size == 0) {
            return ImmutableIntKeyMap.empty();
        }

        final Object[] values = new Object[size];

        for (int i = 0; i < size; i++) {
            values[i] = function.apply(_values[i]);
        }

        return new ImmutableIntKeyMap<>(_values, values);
    }

    @Override
    public ImmutableIntPairMap assignToInt(IntToIntFunction function) {
        final int size = _values.length;
        if (size == 0) {
            return ImmutableIntPairMap.empty();
        }

        final int[] values = new int[size];

        for (int i = 0; i < size; i++) {
            values[i] = function.apply(_values[i]);
        }

        return new ImmutableIntPairMap(_values, values);
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
            return new ImmutableIntArraySet(newValues);
        }

        if (ImmutableIntSetCreator.betterAsBitSet(_values[0], _values[length - 1], _values.length)) {
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

            return new ImmutableIntArraySet(newValues);
        }
    }

    @Override
    public ImmutableIntArraySet removeAt(int index) {
        final int size = _values.length;
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        final int[] newValues = new int[size - 1];
        if (index > 0) {
            System.arraycopy(_values, 0, newValues, 0, index);
        }

        final int remaining = size - index - 1;
        if (remaining > 0) {
            System.arraycopy(_values, index + 1, newValues, index, remaining);
        }

        return new ImmutableIntArraySet(newValues);
    }

    @Override
    public ImmutableIntArraySet remove(int value) {
        final int length = _values.length;
        final int index = findKey(_values, length, value);
        if (index < 0) {
            return this;
        }

        final int[] result = new int[length - 1];
        System.arraycopy(_values, 0, result, 0, index);
        System.arraycopy(_values, index + 1, result, index, length - index - 1);

        return new ImmutableIntArraySet(result);
    }

    @Override
    public ImmutableIntList toList() {
        return new ImmutableIntList(_values);
    }

    @Override
    public IntTransformer iterator() {
        return new IntIterator();
    }

    static ImmutableIntArraySet fromMutableIntSet(MutableIntArraySet set) {
        final int length = set.size();
        if (length == 0) {
            return empty();
        }

        final int[] values = new int[length];
        for (int i = 0; i < length; i++) {
            values[i] = set.valueAt(i);
        }

        return new ImmutableIntArraySet(values);
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
    public ImmutableIntArraySet slice(ImmutableIntRange range) {
        final int size = _values.length;
        final int min = range.min();
        final int max = range.max();
        if (range.min() <= 0 && range.max() >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableIntArraySet.empty();
        }

        final int newSize = Math.min(max + 1, size) - min;
        final int[] newValues = new int[newSize];
        System.arraycopy(_values, min, newValues, 0, newSize);
        return new ImmutableIntArraySet(newValues);
    }

    private ImmutableIntArraySet skip(int index, int length) {
        if (length == 0) {
            return this;
        }

        final int size = _values.length;
        if (length >= size) {
            return ImmutableIntArraySet.empty();
        }

        final int newSize = size - length;
        final int[] newValues = new int[newSize];
        System.arraycopy(_values, index, newValues, 0, newSize);
        return new ImmutableIntArraySet(newValues);
    }

    private ImmutableIntArraySet take(int index, int length) {
        final int size = _values.length;
        if (length >= size) {
            return this;
        }

        if (length == 0) {
            return ImmutableIntArraySet.empty();
        }

        final int[] newValues = new int[length];
        System.arraycopy(_values, index, newValues, 0, length);
        return new ImmutableIntArraySet(newValues);
    }

    /**
     * Returns a new ImmutableIntArraySet where the <code>length</code>
     * amount of first elements in iteration order has been removed.
     * <p>
     * This will return an empty set if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the start of the set.
     * @return A new ImmutableIntSet instance without the first elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance of the given length is equal or greater
     *         than the actual length of the list.
     */
    @Override
    public ImmutableIntArraySet skip(int length) {
        return skip(length, length);
    }

    /**
     * Returns a new ImmutableIntArraySet where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of the set in iteration order.
     * @return A new ImmutableIntArraySet instance just including the first elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length is equal or greater than the
     *         actual size of this collection.
     */
    @Override
    public ImmutableIntArraySet take(int length) {
        return take(0, length);
    }

    /**
     * Returns a new ImmutableIntArraySet where the <code>length</code> amount of
     * last elements has been removed.
     * <p>
     * This will return the empty instance if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the end of the set.
     * @return A new ImmutableIntArraySet instance without the last elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance if the given length is equal or greater
     *         than the actual length of the set.
     */
    @Override
    public ImmutableIntArraySet skipLast(int length) {
        return skip(0, length);
    }

    /**
     * Returns a new ImmutableIntArraySet where only the <code>length</code> amount of
     * last elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the end of this set.
     * @return A new ImmutableIntArraySet instance just including the last elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length equals or greater than the
     *         actual size of this collection.
     */
    @Override
    public ImmutableIntArraySet takeLast(int length) {
        return take(_values.length - length, length);
    }

    @Override
    public ImmutableIntSet toImmutable() {
        return this;
    }

    @Override
    public MutableIntArraySet mutate() {
        return MutableIntArraySet.fromIntSet(this);
    }

    static class Builder implements ImmutableIntSet.Builder {

        private final MutableIntArraySet _set = MutableIntArraySet.empty();

        @Override
        public Builder add(int value) {
            _set.add(value);
            return this;
        }

        @Override
        public ImmutableIntArraySet build() {
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
        if (!(other instanceof ImmutableIntArraySet)) {
            return super.equals(other);
        }

        final ImmutableIntArraySet that = (ImmutableIntArraySet) other;
        return Arrays.equals(_values, that._values);
    }
}

package sword.collections;

import static sword.collections.SortUtils.equal;
import static sword.collections.SortUtils.findValue;

/**
 * Efficient implementation for an immutable Set when few elements are included.
 * 'Set' must be understood as a collection where its elements cannot be repeated.
 * 2 elements are considered to be the same, so they would be duplicated, if both
 * return the same hash code and calling equals returns true.
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
 * This implementation assumes that elements inserted are also immutable.
 * It is not guaranteed to work if any of the elements is mutable.
 *
 * This class also implements the {@link Iterable} interface, which
 * ensures that the for-each construction can be used.
 *
 * @param <T> Type for the elements within the Set
 */
public final class ImmutableSortedSet<T> extends AbstractImmutableSet<T> {

    private SortFunction<? super T> _sortFunction;

    ImmutableSortedSet(SortFunction<? super T> sortFunction, Object[] keys) {
        super(keys);
        _sortFunction = sortFunction;
    }

    @Override
    <U> ImmutableTransformableBuilder<U> newBuilder() {
        return new ImmutableHashSet.Builder<>();
    }

    @Override
    public int indexOf(T value) {
        return findValue(_sortFunction, _values, _values.length, value);
    }

    @Override
    public ImmutableSet<T> toSet() {
        return this;
    }

    @Override
    public ImmutableIntSet indexes() {
        final int size = size();
        return (size == 0)? ImmutableIntArraySet.empty() : new ImmutableIntRange(0, size - 1);
    }

    @Override
    public ImmutableSortedSet<T> filter(Predicate<? super T> predicate) {
        boolean somethingRemoved = false;
        final ImmutableSortedSet.Builder<T> builder = new Builder<>(_sortFunction);
        for (T item : this) {
            if (predicate.apply(item)) {
                builder.add(item);
            }
            else {
                somethingRemoved = true;
            }
        }

        return somethingRemoved? builder.build() : this;
    }

    @Override
    public ImmutableSortedSet<T> filterNot(Predicate<? super T> predicate) {
        boolean somethingRemoved = false;
        final ImmutableSortedSet.Builder<T> builder = new Builder<>(_sortFunction);
        for (T item : this) {
            if (predicate.apply(item)) {
                somethingRemoved = true;
            }
            else {
                builder.add(item);
            }
        }

        return somethingRemoved? builder.build() : this;
    }

    @Override
    public <E> ImmutableSortedMap<T, E> assign(Function<? super T, ? extends E> function) {
        final int size = _values.length;
        final Object[] values = new Object[size];

        for (int i = 0; i < size; i++) {
            values[i] = function.apply(valueAt(i));
        }

        return new ImmutableSortedMap<>(_sortFunction, _values, values);
    }

    @Override
    public ImmutableIntValueSortedMap<T> assignToInt(IntResultFunction<? super T> function) {
        final int size = _values.length;
        final int[] values = new int[size];

        for (int i = 0; i < size; i++) {
            final T key = valueAt(i);
            values[i] = function.apply(key);
        }

        return new ImmutableIntValueSortedMap<>(_sortFunction, _values, values);
    }

    @Override
    public ImmutableSortedSet<T> add(T value) {
        if (contains(value)) {
            return this;
        }

        final int length = _values.length;
        final int index = SortUtils.findSuitableIndex(_sortFunction, _values, length, value);
        final Object[] newKeys = new Object[length + 1];
        if (index > 0) {
            System.arraycopy(_values, 0, newKeys, 0, index);
        }
        newKeys[index] = value;
        if (index < length) {
            System.arraycopy(_values, index, newKeys, index + 1, length - index);
        }

        return new ImmutableSortedSet<>(_sortFunction, newKeys);
    }

    @Override
    public ImmutableSortedSet<T> addAll(Iterable<T> iterable) {
        final MutableSortedSet<T> result = mutate();
        for (T item : iterable) {
            result.add(item);
        }

        return (result.size() == _values.length)? this : result.toImmutable();
    }

    @Override
    public ImmutableSortedSet<T> toImmutable() {
        return this;
    }

    @Override
    public MutableSortedSet<T> mutate(ArrayLengthFunction arrayLengthFunction) {
        final int size = _values.length;
        final int length = arrayLengthFunction.suitableArrayLength(0, size);

        Object[] keys = new Object[length];
        System.arraycopy(_values, 0, keys, 0, size);
        return new MutableSortedSet<>(arrayLengthFunction, _sortFunction, keys, size);
    }

    @Override
    public MutableSortedSet<T> mutate() {
        return mutate(GranularityBasedArrayLengthFunction.getInstance());
    }

    @Override
    public ImmutableSortedSet<T> sort(SortFunction<? super T> function) {
        return equal(_sortFunction, function)? this : super.sort(function);
    }

    @Override
    public ImmutableSortedSet<T> removeAt(int index) {
        final int size = _values.length;
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        final Object[] newValues = new Object[size - 1];
        if (index > 0) {
            System.arraycopy(_values, 0, newValues, 0, index);
        }

        final int remaining = size - index - 1;
        if (remaining > 0) {
            System.arraycopy(_values, index + 1, newValues, index, size - index - 1);
        }

        return new ImmutableSortedSet<>(_sortFunction, newValues);
    }

    @Override
    public ImmutableSortedSet<T> remove(T value) {
        final int index = indexOf(value);
        return (index < 0)? this : removeAt(index);
    }

    @Override
    public ImmutableSortedSet<T> slice(ImmutableIntRange range) {
        final int size = _values.length;
        final int min = range.min();
        final int max = range.max();
        if (min >= size || max < 0) {
            return new ImmutableSortedSet<>(_sortFunction, new Object[0]);
        }

        if (range.min() <= 0 && range.max() >= size - 1) {
            return this;
        }

        final int newSize = Math.min(max, size - 1) - min + 1;
        final Object[] newValues = new Object[newSize];
        System.arraycopy(_values, min, newValues, 0, newSize);

        return new ImmutableSortedSet<>(_sortFunction, newValues);
    }

    @Override
    public ImmutableSortedSet<T> skip(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Unable to skip a negative number of elements");
        }
        else if (length == 0) {
            return this;
        }
        else if (length >= _values.length) {
            return new ImmutableSortedSet<>(_sortFunction, new Object[0]);
        }

        final int remain = _values.length - length;
        final Object[] newValues = new Object[remain];
        System.arraycopy(_values, length, newValues, 0, remain);
        return new ImmutableSortedSet<>(_sortFunction, newValues);
    }

    /**
     * Returns a new ImmutableSortedSet where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of the set in iteration order.
     * @return A new ImmutableSortedSet instance just including the first
     *         elements and in the same order, or the same instance in case the
     *         given length is equal or greater than the actual size of this
     *         set.
     */
    public ImmutableSortedSet<T> take(int length) {
        final int size = _values.length;
        if (length >= size) {
            return this;
        }

        if (length == 0) {
            return new ImmutableSortedSet<>(_sortFunction, new Object[0]);
        }

        final Object[] newValues = new Object[length];
        System.arraycopy(_values, 0, newValues, 0, length);
        return new ImmutableSortedSet<>(_sortFunction, newValues);
    }

    public static class Builder<E> implements ImmutableSet.Builder<E> {
        private final MutableSortedSet<E> _set;

        public Builder(SortFunction<? super E> sortFunction) {
            _set = MutableSortedSet.empty(sortFunction);
        }

        public Builder(ArrayLengthFunction arrayLengthFunction, SortFunction<? super E> sortFunction) {
            _set = MutableSortedSet.empty(arrayLengthFunction, sortFunction);
        }

        @Override
        public Builder<E> add(E key) {
            _set.add(key);
            return this;
        }

        @Override
        public ImmutableSortedSet<E> build() {
            return _set.toImmutable();
        }
    }
}

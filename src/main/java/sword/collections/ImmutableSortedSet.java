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

    private SortFunction<T> _sortFunction;

    ImmutableSortedSet(SortFunction<T> sortFunction, Object[] keys) {
        super(keys);
        _sortFunction = sortFunction;
    }

    @Override
    <U> ImmutableCollectionBuilder<U> newBuilder() {
        return new ImmutableHashSet.Builder<>();
    }

    @Override
    public int indexOf(T value) {
        return findValue(_sortFunction, _keys, _keys.length, value);
    }

    @Override
    public ImmutableSortedSet<T> filter(Predicate<T> predicate) {
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
    public ImmutableSortedSet<T> filterNot(Predicate<T> predicate) {
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
    public ImmutableSortedSet<T> add(T value) {
        if (contains(value)) {
            return this;
        }

        final int length = _keys.length;
        final int index = SortUtils.findSuitableIndex(_sortFunction, _keys, length, value);
        final Object[] newKeys = new Object[length + 1];
        if (index > 0) {
            System.arraycopy(_keys, 0, newKeys, 0, index);
        }
        newKeys[index] = value;
        if (index < length) {
            System.arraycopy(_keys, index, newKeys, index + 1, length - index);
        }

        return new ImmutableSortedSet<>(_sortFunction, newKeys);
    }

    @Override
    public ImmutableSortedSet<T> addAll(Iterable<T> iterable) {
        final MutableSortedSet<T> result = mutate();
        for (T item : iterable) {
            result.add(item);
        }

        return (result.size() == _keys.length)? this : result.toImmutable();
    }

    @Override
    public ImmutableSortedSet<T> toImmutable() {
        return this;
    }

    @Override
    public MutableSortedSet<T> mutate() {
        final int length = _keys.length;
        final int newLength = MutableHashSet.suitableArrayLength(length);

        Object[] keys = new Object[newLength];
        System.arraycopy(_keys, 0, keys, 0, length);
        return new MutableSortedSet<>(_sortFunction, keys, length);
    }

    @Override
    public ImmutableSortedSet<T> sort(SortFunction<T> function) {
        return equal(_sortFunction, function)? this : super.sort(function);
    }

    public static class Builder<E> implements ImmutableSet.Builder<E> {
        private final MutableSortedSet<E> _set;

        Builder(SortFunction<E> sortFunction) {
            _set = new MutableSortedSet<>(sortFunction, new Object[AbstractMutableSet.suitableArrayLength(0)], 0);
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

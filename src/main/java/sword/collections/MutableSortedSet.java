package sword.collections;

import static sword.collections.SortUtils.findValue;

/**
 * Efficient implementation for small mutable Set where elements are internally
 * sorted using a {@link SortFunction} given in construction time.
 *
 * This implementation may not be efficient enough for big sets as insertion will
 * became slow as this increases.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should get a new instance through the empty method.
 *
 * This implementation assumes that elements inserted are immutable, then its
 * hashCode will not change. It is not guaranteed to work if any of the
 * elements is mutable.
 *
 * @param <T> Type for the elements within the Set
 */
public final class MutableSortedSet<T> extends AbstractMutableSet<T> {

    public static <E> MutableSortedSet<E> empty(SortFunction<E> sortFunction) {
        return new MutableSortedSet<>(sortFunction, new Object[suitableArrayLength(0)], 0);
    }

    private final SortFunction<T> _sortFunction;

    MutableSortedSet(SortFunction<T> sortFunction, Object[] keys, int size) {
        super(keys, size);
        _sortFunction = sortFunction;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T valueAt(int index) {
        return (T) values[index];
    }

    @Override
    public int indexOf(T value) {
        return findValue(_sortFunction, values, _size, value);
    }

    @Override
    public <E> Map<T, E> assign(Function<T, E> function) {
        final int size = _size;
        final Object[] keys = new Object[size];
        final Object[] values = new Object[size];

        for (int i = 0; i < size; i++) {
            final T key = valueAt(i);
            keys[i] = key;
            values[i] = function.apply(key);
        }

        return new ImmutableSortedMap<>(_sortFunction, keys, values);
    }

    @Override
    public ImmutableSortedSet<T> toImmutable() {
        Object[] keys = new Object[_size];
        System.arraycopy(values, 0, keys, 0, _size);
        return new ImmutableSortedSet<>(_sortFunction, keys);
    }

    @Override
    public MutableSortedSet<T> mutate() {
        Object[] keys = new Object[values.length];
        System.arraycopy(values, 0, keys, 0, _size);
        return new MutableSortedSet<>(_sortFunction, keys, _size);
    }

    @Override
    public Set<T> filter(Predicate<T> predicate) {
        final ImmutableSortedSet.Builder<T> builder = new ImmutableSortedSet.Builder<>(_sortFunction);
        for (int i = 0; i < _size; i++) {
            T value = valueAt(i);
            if (predicate.apply(value)) {
                builder.add(value);
            }
        }

        return builder.build();
    }

    @Override
    public Set<T> filterNot(Predicate<T> predicate) {
        final ImmutableSortedSet.Builder<T> builder = new ImmutableSortedSet.Builder<>(_sortFunction);
        for (int i = 0; i < _size; i++) {
            T value = valueAt(i);
            if (!predicate.apply(value)) {
                builder.add(value);
            }
        }

        return builder.build();
    }

    /**
     * Creates a new set where all current elements are sorted following the given function.
     *
     * The result of this method is currently immutable, but it may change in the future.
     * In the resulting instance of this method, call {@link #toImmutable()} to ensure
     * immutability or {@link #mutate()} to get a mutable copy of it.
     *
     * @param function Function the sort the elements within this collection.
     * @return A new set where all current elements and future newly added will
     * be sorted following the the given function.
     */
    @Override
    public Set<T> sort(SortFunction<T> function) {
        return (function == _sortFunction)? this : super.sort(function);
    }

    private void enlargeArrays() {
        Object[] oldKeys = values;

        values = new Object[_size + GRANULARITY];

        for (int i = 0; i < _size; i++) {
            values[i] = oldKeys[i];
        }
    }

    @Override
    int findSuitableIndex(T key) {
        return SortUtils.findSuitableIndex(_sortFunction, values, _size, key);
    }

    @Override
    void insertAt(int index, T value) {
        if (_size != 0 && _size % GRANULARITY == 0) {
            enlargeArrays();
        }

        for (int i = _size; i > index; i--) {
            values[i] = values[i - 1];
        }

        values[index] = value;
        _size++;
    }

    @Override
    public void removeAt(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        if (_size != 1 && (_size % GRANULARITY) == 1) {
            Object[] oldKeys = values;
            values = new Object[--_size];

            if (index > 0) {
                System.arraycopy(oldKeys, 0, values, 0, index);
            }

            if (_size > index) {
                System.arraycopy(oldKeys, index + 1, values, index, _size - index);
            }
        }
        else {
            --_size;
            for (int i = index; i < _size; i++) {
                values[i] = values[i + 1];
            }
        }
    }

    @Override
    public boolean clear() {
        final int suitableLength = suitableArrayLength(0);
        if (values.length != suitableLength) {
            values = new Object[suitableLength];
        }
        else {
            for (int i = 0; i < _size; i++) {
                values[i] = null;
            }
        }

        final boolean changed = _size > 0;
        _size = 0;

        return changed;
    }

    public static class Builder<E> implements MutableSet.Builder<E> {
        private final MutableSortedSet<E> _set;

        Builder(SortFunction<E> sortFunction) {
            _set = new MutableSortedSet<>(sortFunction, new Object[suitableArrayLength(0)], 0);
        }

        @Override
        public Builder<E> add(E key) {
            _set.add(key);
            return this;
        }

        @Override
        public MutableSortedSet<E> build() {
            return _set;
        }
    }
}

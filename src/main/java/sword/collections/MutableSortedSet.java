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

    public static <E> MutableSortedSet<E> empty(ArrayLengthFunction arrayLengthFunction, SortFunction<? super E> sortFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, 0);
        return new MutableSortedSet<>(arrayLengthFunction, sortFunction, new Object[length], 0);
    }

    public static <E> MutableSortedSet<E> empty(SortFunction<? super E> sortFunction) {
        return empty(GranularityBasedArrayLengthFunction.getInstance(), sortFunction);
    }

    private final SortFunction<? super T> _sortFunction;

    MutableSortedSet(ArrayLengthFunction arrayLengthFunction, SortFunction<? super T> sortFunction, Object[] keys, int size) {
        super(arrayLengthFunction, keys, size);
        _sortFunction = sortFunction;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T valueAt(int index) {
        return (T) _values[index];
    }

    @Override
    public int indexOf(T value) {
        return findValue(_sortFunction, _values, _size, value);
    }

    @Override
    public <E> Map<T, E> assign(Function<? super T, ? extends E> function) {
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
    public IntValueMap<T> assignToInt(IntResultFunction<? super T> function) {
        final int size = _size;
        final Object[] keys = new Object[size];
        final int[] values = new int[size];

        for (int i = 0; i < size; i++) {
            final T key = valueAt(i);
            keys[i] = key;
            values[i] = function.apply(key);
        }

        return new ImmutableIntValueSortedMap<>(_sortFunction, keys, values);
    }

    @Override
    public ImmutableSortedSet<T> toImmutable() {
        Object[] keys = new Object[_size];
        System.arraycopy(_values, 0, keys, 0, _size);
        return new ImmutableSortedSet<>(_sortFunction, keys);
    }

    @Override
    public MutableSortedSet<T> mutate(ArrayLengthFunction arrayLengthFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, _size);
        Object[] keys = new Object[length];
        System.arraycopy(_values, 0, keys, 0, _size);
        return new MutableSortedSet<>(arrayLengthFunction, _sortFunction, keys, _size);
    }

    @Override
    public MutableSortedSet<T> mutate() {
        return mutate(_arrayLengthFunction);
    }

    @Override
    public Set<T> filter(Predicate<? super T> predicate) {
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
    public Set<T> filterNot(Predicate<? super T> predicate) {
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
    public Set<T> sort(SortFunction<? super T> function) {
        return (function == _sortFunction)? this : super.sort(function);
    }

    @Override
    int findSuitableIndex(T key) {
        return SortUtils.findSuitableIndex(_sortFunction, _values, _size, key);
    }

    @Override
    void insertAt(int index, T value) {
        final int desiredLength = _arrayLengthFunction.suitableArrayLength(_values.length, _size + 1);
        if (desiredLength != _values.length) {
            Object[] oldKeys = _values;
            _values = new Object[desiredLength];

            if (index > 0) {
                System.arraycopy(oldKeys, 0, _values, 0, index);
            }

            if (_size > index) {
                System.arraycopy(oldKeys, index, _values, index + 1, _size - index);
            }
        }
        else {
            for (int i = _size; i > index; i--) {
                _values[i] = _values[i - 1];
            }
        }

        _values[index] = value;
        _size++;
    }

    @Override
    public void removeAt(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        --_size;
        final int desiredLength = _arrayLengthFunction.suitableArrayLength(_values.length, _size);
        if (desiredLength != _values.length) {
            Object[] oldKeys = _values;
            _values = new Object[desiredLength];

            if (index > 0) {
                System.arraycopy(oldKeys, 0, _values, 0, index);
            }

            if (_size > index) {
                System.arraycopy(oldKeys, index + 1, _values, index, _size - index);
            }
        }
        else {
            for (int i = index; i < _size; i++) {
                _values[i] = _values[i + 1];
            }
        }
    }

    @Override
    public boolean clear() {
        final int suitableLength = _arrayLengthFunction.suitableArrayLength(_values.length, 0);
        if (_values.length != suitableLength) {
            _values = new Object[suitableLength];
        }
        else {
            for (int i = 0; i < _size; i++) {
                _values[i] = null;
            }
        }

        final boolean changed = _size > 0;
        _size = 0;

        return changed;
    }

    @Override
    public MutableSortedSet<T> donate() {
        final MutableSortedSet<T> newSet = new MutableSortedSet<>(_arrayLengthFunction, _sortFunction, _values, _size);
        final int length = _arrayLengthFunction.suitableArrayLength(0, 0);
        _values = new Object[length];
        _size = 0;
        return newSet;
    }

    public static class Builder<E> implements MutableSet.Builder<E> {
        private final MutableSortedSet<E> _set;

        public Builder(ArrayLengthFunction arrayLengthFunction, SortFunction<E> sortFunction) {
            _set = empty(arrayLengthFunction, sortFunction);
        }

        public Builder(SortFunction<E> sortFunction) {
            _set = empty(sortFunction);
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

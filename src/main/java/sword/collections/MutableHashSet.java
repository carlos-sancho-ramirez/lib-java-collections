package sword.collections;

import static sword.collections.SortUtils.findKey;

/**
 * Efficient implementation for small mutable Set where elements are internally sorted by its hash code.
 *
 * This implementation may not be efficient enough for big sets as insertion will
 * become slow as this increases.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should get a new instance through the empty method.
 *
 * This implementation assumes that elements inserted are immutable, then its hashCode will not change.
 * It is not guaranteed to work if any of the elements is mutable.
 *
 * @param <T> Type for the elements within the Set
 */
public final class MutableHashSet<T> extends AbstractMutableSet<T> {

    public static <E> MutableHashSet<E> empty(ArrayLengthFunction arrayLengthFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, 0);
        return new MutableHashSet<>(arrayLengthFunction, new Object[length], new int[length], 0);
    }

    public static <E> MutableHashSet<E> empty() {
        return empty(GranularityBasedArrayLengthFunction.getInstance());
    }

    private int[] _hashCodes;

    MutableHashSet(ArrayLengthFunction arrayLengthFunction, Object[] keys, int[] hashCodes, int size) {
        super(arrayLengthFunction, keys, size);
        _hashCodes = hashCodes;
    }

    @Override
    public int indexOf(T value) {
        return findKey(_hashCodes, _values, _size, value);
    }

    @Override
    public <E> Map<T, E> assign(Function<? super T, ? extends E> function) {
        final int size = _size;
        if (size == 0) {
            return ImmutableHashMap.empty();
        }

        final Object[] keys = new Object[size];
        final int[] hashCodes = new int[size];
        final Object[] values = new Object[size];

        for (int i = 0; i < size; i++) {
            final T key = valueAt(i);
            keys[i] = key;
            hashCodes[i] = _hashCodes[i];
            values[i] = function.apply(key);
        }

        return new ImmutableHashMap<>(keys, hashCodes, values);
    }

    @Override
    public IntValueMap<T> assignToInt(IntResultFunction<? super T> function) {
        final int size = _size;
        if (size == 0) {
            return ImmutableIntValueHashMap.empty();
        }

        final Object[] keys = new Object[size];
        final int[] hashCodes = new int[size];
        final int[] values = new int[size];

        for (int i = 0; i < size; i++) {
            final T key = valueAt(i);
            keys[i] = key;
            hashCodes[i] = _hashCodes[i];
            values[i] = function.apply(key);
        }

        return new ImmutableIntValueHashMap<>(keys, hashCodes, values);
    }

    @Override
    public ImmutableHashSet<T> toImmutable() {
        Object[] keys = new Object[_size];
        int[] hashCodes = new int[_size];

        System.arraycopy(_values, 0, keys, 0, _size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);

        return new ImmutableHashSet<>(keys, hashCodes);
    }

    @Override
    public MutableHashSet<T> mutate(ArrayLengthFunction arrayLengthFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, _size);
        Object[] keys = new Object[length];
        int[] hashCodes = new int[length];

        System.arraycopy(_values, 0, keys, 0, _size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);

        return new MutableHashSet<>(arrayLengthFunction, keys, hashCodes, _size);
    }

    @Override
    public MutableHashSet<T> mutate() {
        return mutate(_arrayLengthFunction);
    }

    @Override
    int findSuitableIndex(T key) {
        return SortUtils.findSuitableIndex(_hashCodes, _size, SortUtils.hashCode(key));
    }

    @Override
    void insertAt(int index, T value) {
        final int newDesiredLength = _arrayLengthFunction.suitableArrayLength(_values.length, _size + 1);
        if (newDesiredLength != _values.length) {
            Object[] oldKeys = _values;
            int[] oldHashCodes = _hashCodes;

            _values = new Object[newDesiredLength];
            _hashCodes = new int[newDesiredLength];

            if (index > 0) {
                System.arraycopy(oldKeys, 0, _values, 0, index);
                System.arraycopy(oldHashCodes, 0, _hashCodes, 0, index);
            }

            if (_size > index) {
                System.arraycopy(oldKeys, index, _values, index + 1, _size - index);
                System.arraycopy(oldHashCodes, index, _hashCodes, index + 1, _size - index);
            }
        }
        else {
            for (int i = _size; i > index; i--) {
                _values[i] = _values[i - 1];
                _hashCodes[i] = _hashCodes[i - 1];
            }
        }

        _values[index] = value;
        _hashCodes[index] = SortUtils.hashCode(value);
        _size++;
    }

    @Override
    public void removeAt(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        --_size;
        final int newDesiredLength = _arrayLengthFunction.suitableArrayLength(_values.length, _size);
        if (newDesiredLength != _values.length) {
            Object[] oldKeys = _values;
            int[] oldHashCodes = _hashCodes;

            _values = new Object[newDesiredLength];
            _hashCodes = new int[newDesiredLength];

            if (index > 0) {
                System.arraycopy(oldKeys, 0, _values, 0, index);
                System.arraycopy(oldHashCodes, 0, _hashCodes, 0, index);
            }

            if (_size > index) {
                System.arraycopy(oldKeys, index + 1, _values, index, _size - index);
                System.arraycopy(oldHashCodes, index + 1, _hashCodes, index, _size - index);
            }
        }
        else {
            for (int i = index; i < _size; i++) {
                _values[i] = _values[i + 1];
                _hashCodes[i] = _hashCodes[i + 1];
            }
        }
    }

    @Override
    public boolean clear() {
        final int suitableLength = _arrayLengthFunction.suitableArrayLength(_values.length, 0);
        if (_values.length != suitableLength) {
            _values = new Object[suitableLength];
            _hashCodes = new int[suitableLength];
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
    public MutableHashSet<T> donate() {
        final MutableHashSet<T> newSet = new MutableHashSet<>(_arrayLengthFunction, _values, _hashCodes, _size);
        final int length = _arrayLengthFunction.suitableArrayLength(0, 0);
        _hashCodes = new int[length];
        _values = new Object[length];
        _size = 0;
        return newSet;
    }

    public static class Builder<E> implements MutableSet.Builder<E> {
        private final MutableHashSet<E> _set;

        public Builder(ArrayLengthFunction arrayLengthFunction) {
            _set = MutableHashSet.empty(arrayLengthFunction);
        }

        public Builder() {
            _set = MutableHashSet.empty();
        }

        @Override
        public Builder<E> add(E key) {
            _set.add(key);
            return this;
        }

        @Override
        public MutableHashSet<E> build() {
            return _set;
        }
    }

    @Override
    int itemHashCode(int index) {
        return _hashCodes[index];
    }
}

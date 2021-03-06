package sword.collections;

import static sword.collections.SortUtils.findSuitableIndex;
import static sword.collections.SortUtils.findValue;

/**
 * Efficient implementation for small mutable map where keys are internally
 * sorted using a {@link SortFunction} given in construction time.
 *
 * This implementation may not be efficient enough for big maps as insertion will
 * become slow as this increases.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should create a builder in order to obtain
 * an instance of it.
 *
 * This implementation assumes that keys inserted are immutable.
 * Mutation of the contained keys may result in duplicates within
 * the keys or wrong sorting of keys.
 * It is not guaranteed to work if keys are mutable.
 * This does no apply to values of the map, that can mutate without risk.
 *
 * @param <T> Type for the key elements within the Map
 */
public final class MutableIntValueSortedMap<T> extends AbstractMutableIntValueMap<T> {

    public static <E> MutableIntValueSortedMap<E> empty(ArrayLengthFunction arrayLengthFunction, SortFunction<? super E> sortFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, 0);
        return new MutableIntValueSortedMap<>(arrayLengthFunction, sortFunction, new Object[length], new int[length], 0);
    }

    public static <E> MutableIntValueSortedMap<E> empty(SortFunction<? super E> sortFunction) {
        return empty(GranularityBasedArrayLengthFunction.getInstance(), sortFunction);
    }

    private SortFunction<? super T> _sortFunction;

    MutableIntValueSortedMap(ArrayLengthFunction arrayLengthFunction, SortFunction<? super T> sortFunction, Object[] keys, int[] values, int size) {
        super(arrayLengthFunction, keys, values, size);
        _sortFunction = sortFunction;
    }

    @Override
    public IntValueMap<T> filter(IntPredicate predicate) {
        final ImmutableIntValueSortedMap.Builder<T> builder = new ImmutableIntValueSortedMap.Builder<>(_sortFunction);
        for (int i = 0; i < _size; i++) {
            final int value = _values[i];
            if (predicate.apply(value)) {
                builder.put(keyAt(i), value);
            }
        }

        return builder.build();
    }

    @Override
    public IntValueMap<T> filterNot(IntPredicate predicate) {
        final ImmutableIntValueSortedMap.Builder<T> builder = new ImmutableIntValueSortedMap.Builder<>(_sortFunction);
        for (int i = 0; i < _size; i++) {
            final int value = _values[i];
            if (!predicate.apply(value)) {
                builder.put(keyAt(i), value);
            }
        }

        return builder.build();
    }

    @Override
    public <U> Map<T, U> map(IntFunction<? extends U> func) {
        final Object[] newKeys = new Object[_size];
        final Object[] newValues = new Object[_size];

        for (int i = 0; i < _size; i++) {
            newKeys[i] = _keys[i];
            newValues[i] = func.apply(valueAt(i));
        }

        return new ImmutableSortedMap<>(_sortFunction, newKeys, newValues);
    }

    @Override
    public IntValueMap<T> mapToInt(IntToIntFunction func) {
        final Object[] newKeys = new Object[_size];
        final int[] newValues = new int[_size];

        for (int i = 0; i < _size; i++) {
            newKeys[i] = _keys[i];
            newValues[i] = func.apply(valueAt(i));
        }

        return new ImmutableIntValueSortedMap<>(_sortFunction, newKeys, newValues);
    }

    @Override
    public int indexOfKey(T key) {
        return findValue(_sortFunction, _keys, _size, key);
    }

    @Override
    public Set<T> keySet() {
        final Object[] keys = new Object[_size];
        System.arraycopy(_keys, 0, keys, 0, _size);
        return new ImmutableSortedSet<>(_sortFunction, keys);
    }

    @Override
    boolean entryLessThan(Entry<T> a, Entry<T> b) {
        return b != null && (a == null || SortUtils.compareByHashCode(a.key(), b.key()));
    }

    @Override
    public ImmutableIntValueSortedMap<T> toImmutable() {
        Object[] keys = new Object[_size];
        int[] values = new int[_size];

        if (_size > 0) {
            System.arraycopy(_keys, 0, keys, 0, _size);
            System.arraycopy(_values, 0, values, 0, _size);
        }

        return new ImmutableIntValueSortedMap<>(_sortFunction, keys, values);
    }

    @Override
    public MutableIntValueSortedMap<T> mutate(ArrayLengthFunction arrayLengthFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, _size);
        Object[] keys = new Object[length];
        int[] values = new int[length];

        if (_size > 0) {
            System.arraycopy(_keys, 0, keys, 0, _size);
            System.arraycopy(_values, 0, values, 0, _size);
        }

        return new MutableIntValueSortedMap<>(arrayLengthFunction, _sortFunction, keys, values, _size);
    }

    @Override
    public MutableIntValueSortedMap<T> mutate() {
        return mutate(_arrayLengthFunction);
    }

    @Override
    public boolean clear() {
        final int suitableLength = _arrayLengthFunction.suitableArrayLength(_values.length, 0);
        final boolean somethingRemoved = _size > 0;
        if (_keys.length != suitableLength) {
            _keys = new Object[suitableLength];
            _values = new int[suitableLength];
        }
        else {
            for (int i = 0; i < _size; i++) {
                _keys[i] = null;
            }
        }

        _size = 0;
        return somethingRemoved;
    }

    @Override
    public boolean put(T key, int value) {
        int index = findValue(_sortFunction, _keys, _size, key);
        if (index < 0) {
            index = findSuitableIndex(_sortFunction, _keys, _size, key);

            final int newLength = _arrayLengthFunction.suitableArrayLength(_keys.length, _size + 1);
            if (newLength != _keys.length) {
                Object[] newKeys = new Object[newLength];
                int[] newValues = new int[newLength];

                if (index > 0) {
                    System.arraycopy(_keys, 0, newKeys, 0, index);
                    System.arraycopy(_values, 0, newValues, 0, index);
                }

                if (_size >= index) {
                    System.arraycopy(_keys, index, newKeys, index + 1, _size - index);
                    System.arraycopy(_values, index, newValues, index + 1, _size - index);
                }

                _keys = newKeys;
                _values = newValues;
            }
            else {
                for (int i = _size; i > index; i--) {
                    _keys[i] = _keys[i - 1];
                    _values[i] = _values[i - 1];
                }
            }

            ++_size;
            _keys[index] = key;
            _values[index] = value;
        }
        else {
            int oldValue = valueAt(index);
            if (oldValue == value) {
                return false;
            }

            _values[index] = value;
        }

        return true;
    }

    @Override
    public MutableIntValueSortedMap<T> donate() {
        final MutableIntValueSortedMap<T> newMap = new MutableIntValueSortedMap<>(_arrayLengthFunction, _sortFunction, _keys, _values, _size);
        final int length = _arrayLengthFunction.suitableArrayLength(0, 0);
        _keys = new Object[length];
        _values = new int[length];
        _size = 0;
        return newMap;
    }

    @Override
    public void removeAt(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        final int desiredLength = _arrayLengthFunction.suitableArrayLength(_keys.length, --_size);
        if (desiredLength != _keys.length) {
            Object[] oldKeys = _keys;
            int[] oldValues = _values;

            _keys = new Object[_size];
            _values = new int[_size];

            if (index > 0) {
                System.arraycopy(oldKeys, 0, _keys, 0, index);
                System.arraycopy(oldValues, 0, _values, 0, index);
            }

            if (_size > index) {
                System.arraycopy(oldKeys, index + 1, _keys, index, _size - index);
                System.arraycopy(oldValues, index + 1, _values, index, _size - index);
            }
        }
        else {
            for (int i = index; i < _size; i++) {
                _keys[i] = _keys[i + 1];
                _values[i] = _values[i + 1];
            }
        }
    }

    public static class Builder<E> implements MutableIntValueMap.Builder<E> {
        private final MutableIntValueSortedMap<E> _map;

        public Builder(ArrayLengthFunction arrayLengthFunction, SortFunction<E> sortFunction) {
            _map = empty(arrayLengthFunction, sortFunction);
        }

        public Builder(SortFunction<E> sortFunction) {
            _map = empty(sortFunction);
        }

        public Builder<E> put(E key, int value) {
            _map.put(key, value);
            return this;
        }

        public MutableIntValueSortedMap<E> build() {
            return _map;
        }
    }
}

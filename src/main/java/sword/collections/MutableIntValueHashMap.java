package sword.collections;

import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;

/**
 * Efficient implementation for small mutable map where keys are internally
 * sorted according to their hash codes in ascending order.
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
public final class MutableIntValueHashMap<T> extends AbstractMutableIntValueMap<T> implements MutableIntValueMap<T> {

    public static <E> MutableIntValueHashMap<E> empty(ArrayLengthFunction arrayLengthFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, 0);
        return new MutableIntValueHashMap<>(arrayLengthFunction, new Object[length], new int[length], new int[length], 0);
    }

    public static <E> MutableIntValueHashMap<E> empty() {
        return empty(GranularityBasedArrayLengthFunction.getInstance());
    }

    private int[] _hashCodes;

    MutableIntValueHashMap(ArrayLengthFunction arrayLengthFunction, Object[] keys, int[] hashCodes, int[] values, int size) {
        super(arrayLengthFunction, keys, values, size);
        _hashCodes = hashCodes;
    }

    @Override
    public IntValueMap<T> filter(IntPredicate predicate) {
        final ImmutableIntValueHashMap.Builder<T> builder = new ImmutableIntValueHashMap.Builder<>();
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
        final ImmutableIntValueHashMap.Builder<T> builder = new ImmutableIntValueHashMap.Builder<>();
        for (int i = 0; i < _size; i++) {
            final int value = _values[i];
            if (!predicate.apply(value)) {
                builder.put(keyAt(i), value);
            }
        }

        return builder.build();
    }

    @Override
    public int indexOfKey(T key) {
        return findKey(_hashCodes, _keys, _size, key);
    }

    @Override
    public Set<T> keySet() {
        final Object[] keys = new Object[_size];
        final int[] hashCodes = new int[_size];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);

        return new ImmutableHashSet<>(keys, hashCodes);
    }

    @Override
    boolean entryLessThan(Entry<T> a, Entry<T> b) {
        return b != null && (a == null || SortUtils.compareByHashCode(a.key(), b.key()));
    }

    @Override
    public ImmutableIntValueHashMap<T> toImmutable() {
        if (_size == 0) {
            return ImmutableIntValueHashMap.empty();
        }
        else {
            Object[] keys = new Object[_size];
            int[] hashCodes = new int[_size];
            int[] values = new int[_size];

            System.arraycopy(_keys, 0, keys, 0, _size);
            System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);
            System.arraycopy(_values, 0, values, 0, _size);

            return new ImmutableIntValueHashMap<>(keys, hashCodes, values);
        }
    }

    @Override
    public MutableIntValueHashMap<T> mutate(ArrayLengthFunction arrayLengthFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, _size);
        Object[] keys = new Object[length];
        int[] hashCodes = new int[length];
        int[] values = new int[length];

        if (_size > 0) {
            System.arraycopy(_keys, 0, keys, 0, _size);
            System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);
            System.arraycopy(_values, 0, values, 0, _size);
        }

        return new MutableIntValueHashMap<>(arrayLengthFunction, keys, hashCodes, values, _size);
    }

    @Override
    public MutableIntValueHashMap<T> mutate() {
        return mutate(_arrayLengthFunction);
    }

    @Override
    public boolean clear() {
        final int suitableLength = _arrayLengthFunction.suitableArrayLength(_values.length, 0);
        final boolean somethingRemoved = _size > 0;
        if (_keys.length != suitableLength) {
            _keys = new Object[suitableLength];
            _hashCodes = new int[suitableLength];
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
        int index = findKey(_hashCodes, _keys, _size, key);
        if (index < 0) {
            final int hashCode = SortUtils.hashCode(key);
            final int desiredLength = _arrayLengthFunction.suitableArrayLength(_values.length, _size + 1);
            index = findSuitableIndex(_hashCodes, _size, hashCode);

            if (desiredLength != _keys.length) {
                Object[] newKeys = new Object[desiredLength];
                int[] newHashCodes = new int[desiredLength];
                int[] newValues = new int[desiredLength];

                if (index > 0) {
                    System.arraycopy(_keys, 0, newKeys, 0, index);
                    System.arraycopy(_hashCodes, 0, newHashCodes, 0, index);
                    System.arraycopy(_values, 0, newValues, 0, index);
                }

                if (_size >= index) {
                    System.arraycopy(_keys, index, newKeys, index + 1, _size - index);
                    System.arraycopy(_hashCodes, index, newHashCodes, index + 1, _size - index);
                    System.arraycopy(_values, index, newValues, index + 1, _size - index);
                }

                _keys = newKeys;
                _hashCodes = newHashCodes;
                _values = newValues;
            }
            else {
                for (int i = _size; i > index; i--) {
                    _keys[i] = _keys[i - 1];
                    _hashCodes[i] = _hashCodes[i - 1];
                    _values[i] = _values[i - 1];
                }
            }

            ++_size;
            _keys[index] = key;
            _hashCodes[index] = hashCode;
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
    public void removeAt(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        final int desiredLength = _arrayLengthFunction.suitableArrayLength(_values.length, --_size);
        if (desiredLength != _values.length) {
            Object[] oldKeys = _keys;
            int[] oldHashCodes = _hashCodes;
            int[] oldValues = _values;

            _keys = new Object[desiredLength];
            _hashCodes = new int[desiredLength];
            _values = new int[desiredLength];

            if (index > 0) {
                System.arraycopy(oldKeys, 0, _keys, 0, index);
                System.arraycopy(oldHashCodes, 0, _hashCodes, 0, index);
                System.arraycopy(oldValues, 0, _values, 0, index);
            }

            if (_size > index) {
                System.arraycopy(oldKeys, index + 1, _keys, index, _size - index);
                System.arraycopy(oldHashCodes, index + 1, _hashCodes, index, _size - index);
                System.arraycopy(oldValues, index + 1, _values, index, _size - index);
            }
        }
        else {
            for (int i = index; i < _size; i++) {
                _keys[i] = _keys[i + 1];
                _hashCodes[i] = _hashCodes[i + 1];
                _values[i] = _values[i + 1];
            }
        }
    }

    public static class Builder<E> implements MutableIntValueMap.Builder<E> {
        private final MutableIntValueHashMap<E> _map;

        public Builder(ArrayLengthFunction arrayLengthFunction) {
            _map = empty(arrayLengthFunction);
        }

        public Builder() {
            _map = empty();
        }

        public Builder<E> put(E key, int value) {
            _map.put(key, value);
            return this;
        }

        public MutableIntValueHashMap<E> build() {
            return _map;
        }
    }
}

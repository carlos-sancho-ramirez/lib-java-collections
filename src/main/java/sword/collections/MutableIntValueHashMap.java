package sword.collections;

import static sword.collections.SortUtils.*;

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

    public static <E> MutableIntValueHashMap<E> empty() {
        final int length = suitableArrayLength(0);
        return new MutableIntValueHashMap<>(new Object[length], new int[length], new int[length], 0);
    }

    private int[] _hashCodes;

    MutableIntValueHashMap(Object[] keys, int[] hashCodes, int[] values, int size) {
        super(keys, values, size);
        _hashCodes = hashCodes;
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
    public MutableIntValueHashMap<T> mutate() {
        Object[] keys = new Object[_keys.length];
        int[] hashCodes = new int[_hashCodes.length];
        int[] values = new int[_values.length];

        if (_size > 0) {
            System.arraycopy(_keys, 0, keys, 0, _size);
            System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);
            System.arraycopy(_values, 0, values, 0, _size);
        }

        return new MutableIntValueHashMap<>(keys, hashCodes, values, _size);
    }

    @Override
    public boolean clear() {
        final int suitableLength = suitableArrayLength(0);
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
            index = findSuitableIndex(_hashCodes, _size, hashCode);

            final int newLength = suitableArrayLength(_size + 1);
            if (newLength != _keys.length) {
                Object[] newKeys = new Object[newLength];
                int[] newHashCodes = new int[newLength];
                int[] newValues = new int[newLength];

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

        if (_size != 1 && (_size % GRANULARITY) == 1) {
            Object[] oldKeys = _keys;
            int[] oldHashCodes = _hashCodes;
            int[] oldValues = _values;

            _keys = new Object[--_size];
            _hashCodes = new int[_size];
            _values = new int[_size];

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
            --_size;
            for (int i = index; i < _size; i++) {
                _keys[i] = _keys[i + 1];
                _hashCodes[i] = _hashCodes[i + 1];
                _values[i] = _values[i + 1];
            }
        }
    }

    public static class Builder<E> implements MutableIntValueMap.Builder<E> {
        private final MutableIntValueHashMap<E> _map = MutableIntValueHashMap.empty();

        public Builder<E> put(E key, int value) {
            _map.put(key, value);
            return this;
        }

        public MutableIntValueHashMap<E> build() {
            return _map;
        }
    }
}

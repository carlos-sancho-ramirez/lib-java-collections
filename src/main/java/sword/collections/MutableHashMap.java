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
 * @param <K> Type for the key elements within the Map
 * @param <V> Type for the value elements within the Map
 */
public final class MutableHashMap<K, V> extends AbstractMutableMap<K, V> {

    public static <K, V> MutableHashMap<K, V> empty(ArrayLengthFunction arrayLengthFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, 0);
        return new MutableHashMap<>(arrayLengthFunction, new Object[length], new int[length], new Object[length], 0);
    }

    public static <K, V> MutableHashMap<K, V> empty() {
        return empty(GranularityBasedArrayLengthFunction.getInstance());
    }

    private int[] _hashCodes;

    MutableHashMap(ArrayLengthFunction arrayLengthFunction, Object[] keys, int[] hashCodes, Object[] values, int size) {
        super(arrayLengthFunction, keys, values, size);
        _hashCodes = hashCodes;
    }

    @Override
    public int indexOfKey(K key) {
        return findKey(_hashCodes, _keys, _size, key);
    }

    @Override
    public Set<K> keySet() {
        final Object[] keys = new Object[_size];
        final int[] hashCodes = new int[_size];

        if (_size > 0) {
            System.arraycopy(_keys, 0, keys, 0, _size);
            System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);
        }

        return new ImmutableHashSet<>(keys, hashCodes);
    }

    @Override
    boolean entryLessThan(Entry<K, V> a, Entry<K, V> b) {
        return b != null && (a == null || SortUtils.compareByHashCode(a.key(), b.key()));
    }

    @Override
    public ImmutableHashMap<K, V> toImmutable() {
        if (_size == 0) {
            return ImmutableHashMap.empty();
        }
        else {
            Object[] keys = new Object[_size];
            int[] hashCodes = new int[_size];
            Object[] values = new Object[_size];

            System.arraycopy(_keys, 0, keys, 0, _size);
            System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);
            System.arraycopy(_values, 0, values, 0, _size);

            return new ImmutableHashMap<>(keys, hashCodes, values);
        }
    }

    @Override
    public MutableHashMap<K, V> mutate(ArrayLengthFunction arrayLengthFunction) {
        final int desiredLength = arrayLengthFunction.suitableArrayLength(0, _size);
        Object[] keys = new Object[desiredLength];
        int[] hashCodes = new int[desiredLength];
        Object[] values = new Object[desiredLength];

        if (_size > 0) {
            System.arraycopy(_keys, 0, keys, 0, _size);
            System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);
            System.arraycopy(_values, 0, values, 0, _size);
        }

        return new MutableHashMap<>(arrayLengthFunction, keys, hashCodes, values, _size);
    }

    @Override
    public MutableHashMap<K, V> mutate() {
        return mutate(_arrayLengthFunction);
    }

    @Override
    public boolean clear() {
        final boolean somethingRemoved = _size > 0;
        final int suitableLength = _arrayLengthFunction.suitableArrayLength(_values.length, 0);
        if (_keys.length != suitableLength) {
            _keys = new Object[suitableLength];
            _hashCodes = new int[suitableLength];
            _values = new Object[suitableLength];
        }
        else {
            for (int i = 0; i < _size; i++) {
                _keys[i] = null;
                _values[i] = null;
            }
        }

        _size = 0;
        return somethingRemoved;
    }

    @Override
    public boolean put(K key, V value) {
        int index = findKey(_hashCodes, _keys, _size, key);
        if (index < 0) {
            final int desiredLength = _arrayLengthFunction.suitableArrayLength(_values.length, _size + 1);
            final int hashCode = SortUtils.hashCode(key);
            index = findSuitableIndex(_hashCodes, _size, hashCode);

            if (desiredLength != _values.length) {
                Object[] oldKeys = _keys;
                int[] oldHashCodes = _hashCodes;
                Object[] oldValues = _values;

                _keys = new Object[desiredLength];
                _hashCodes = new int[desiredLength];
                _values = new Object[desiredLength];

                if (index > 0) {
                    System.arraycopy(oldKeys, 0, _keys, 0, index);
                    System.arraycopy(oldHashCodes, 0, _hashCodes, 0, index);
                    System.arraycopy(oldValues, 0, _values, 0, index);
                }

                if (_size > index) {
                    System.arraycopy(oldKeys, index, _keys, index + 1, _size - index);
                    System.arraycopy(oldHashCodes, index, _hashCodes, index + 1, _size - index);
                    System.arraycopy(oldValues, index, _values, index + 1, _size - index);
                }
            }
            else {
                for (int i = _size; i > index; i--) {
                    _keys[i] = _keys[i - 1];
                    _hashCodes[i] = _hashCodes[i - 1];
                    _values[i] = _values[i - 1];
                }
            }

            _keys[index] = key;
            _hashCodes[index] = hashCode;
            _values[index] = value;

            _size++;
            return true;
        }
        else {
            V oldValue = valueAt(index);
            if (oldValue == null && value == null || oldValue != null && oldValue.equals(value)) {
                return false;
            }

            _values[index] = value;
        }

        return false;
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
            Object[] oldValues = _values;

            _keys = new Object[desiredLength];
            _hashCodes = new int[desiredLength];
            _values = new Object[desiredLength];

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

    @Override
    public Map<K, V> filter(Predicate<? super V> predicate) {
        final ImmutableHashMap.Builder<K, V> builder = new ImmutableHashMap.Builder<>();
        for (int i = 0; i < _size; i++) {
            final V value = valueAt(i);
            if (predicate.apply(value)) {
                builder.put(keyAt(i), value);
            }
        }

        return builder.build();
    }

    @Override
    public Map<K, V> filterNot(Predicate<? super V> predicate) {
        final ImmutableHashMap.Builder<K, V> builder = new ImmutableHashMap.Builder<>();
        for (int i = 0; i < _size; i++) {
            final V value = valueAt(i);
            if (!predicate.apply(value)) {
                builder.put(keyAt(i), value);
            }
        }

        return builder.build();
    }

    @Override
    public <E> Map<K, E> map(Function<? super V, ? extends E> func) {
        final Object[] newKeys = new Object[_size];
        final int[] newHashCodes = new int[_size];
        final Object[] newValues = new Object[_size];

        for (int i = 0; i < _size; i++) {
            newKeys[i] = _keys[i];
            newHashCodes[i] = _hashCodes[i];
            newValues[i] = func.apply(valueAt(i));
        }

        return new ImmutableHashMap<>(newKeys, newHashCodes, newValues);
    }

    @Override
    public IntValueMap<K> mapToInt(IntResultFunction<? super V> func) {
        final Object[] newKeys = new Object[_size];
        final int[] newHashCodes = new int[_size];
        final int[] newValues = new int[_size];

        for (int i = 0; i < _size; i++) {
            newKeys[i] = _keys[i];
            newHashCodes[i] = _hashCodes[i];
            newValues[i] = func.apply(valueAt(i));
        }

        return new ImmutableIntValueHashMap<>(newKeys, newHashCodes, newValues);
    }

    public static class Builder<K, V> implements MapBuilder<K, V> {
        private final MutableHashMap<K, V> _map;

        public Builder(ArrayLengthFunction arrayLengthFunction) {
            _map = empty(arrayLengthFunction);
        }

        public Builder() {
            _map = empty();
        }

        @Override
        public Builder<K, V> put(K key, V value) {
            _map.put(key, value);
            return this;
        }

        @Override
        public MutableHashMap<K, V> build() {
            return _map;
        }
    }
}

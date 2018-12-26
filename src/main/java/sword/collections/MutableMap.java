package sword.collections;

import static sword.collections.SortUtils.*;

/**
 * Efficient implementation for a mutable Map when few elements are included.
 * 'Map' must be understood as a key-value pair collection where its key elements cannot be repeated.
 * 2 key elements are considered to be the same, so they would be duplicated, if both
 * return the same hash code and calling equals returns true.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should get a new instance through the empty method.
 *
 * This implementation assumes that elements inserted are immutable, then its hashCode will not change.
 * It is not guaranteed to work if any of the elements is mutable.
 *
 * This class also implements the {@link Iterable} interface, which
 * ensures that the for-each construction can be used.
 *
 * @param <K> Type for the key elements within the Map
 * @param <V> Type for the value elements within the Map
 */
public class MutableMap<K, V> extends AbstractMap<K, V> implements MutableIterableCollection<V> {

    static final int GRANULARITY = DEFAULT_GRANULARITY;

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    private final SortFunction<K> _sortFunction;
    Object[] _keys;
    Object[] _values;
    int _size;

    MutableMap(SortFunction<K> sortFunction, Object[] keys, Object[] values, int size) {
        _sortFunction = sortFunction;
        _keys = keys;
        _size = size;
        _values = values;
    }

    @Override
    public boolean containsKey(K key) {
        return findValue(_sortFunction, _keys, _size, key) >= 0;
    }

    @Override
    public V get(K key, V defaultValue) {
        final int index = findValue(_sortFunction, _keys, _size, key);
        return (index >= 0)? valueAt(index) : defaultValue;
    }

    @Override
    public V get(K key) {
        final int index = findValue(_sortFunction, _keys, _size, key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        return valueAt(index);
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    @SuppressWarnings("unchecked")
    public K keyAt(int index) {
        return (K) _keys[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public V valueAt(int index) {
        return (V) _values[index];
    }

    @Override
    public int indexOfKey(K key) {
        return findValue(_sortFunction, _keys, _size, key);
    }

    @Override
    public Set<K> keySet() {
        final Object[] keys = new Object[_size];
        System.arraycopy(_keys, 0, keys, 0, _size);
        return new ImmutableSortedSet<>(_sortFunction, keys);
    }

    @Override
    public List<V> valueList() {
        final int length = _size;
        final Object[] newValues = new Object[length];
        System.arraycopy(_values, 0, newValues, 0, length);
        return new ImmutableList<>(newValues);
    }

    @Override
    public Set<Entry<K, V>> entries() {
        final int length = _size;
        final Entry[] entries = new Entry[length];

        for (int index = 0; index < length; index++) {
            entries[index] = new Entry<>(index, _keys[index], _values[index]);
        }

        final SortFunction<Entry<K, V>> entrySortFunction = (a, b) ->
                b != null && (a == null || _sortFunction.lessThan(a.key(), b.key()));
        return new ImmutableSortedSet<>(entrySortFunction, entries);
    }

    private class Iterator implements Traverser<V> {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V next() {
            return (V) _values[_index++];
        }

        @Override
        public void remove() {
            removeAt(--_index);
        }
    }

    @Override
    public Traverser<V> iterator() {
        return new Iterator();
    }

    @Override
    public ImmutableMap<K, V> toImmutable() {
        if (_size == 0) {
            return new ImmutableMap<>(_sortFunction, new Object[0], new Object[0]);
        }
        else {
            Object[] keys = new Object[_size];
            Object[] values = new Object[_size];

            System.arraycopy(_keys, 0, keys, 0, _size);
            System.arraycopy(_values, 0, values, 0, _size);

            return new ImmutableMap<>(_sortFunction, keys, values);
        }
    }

    @Override
    public MutableMap<K, V> mutate() {
        Object[] keys = new Object[_keys.length];
        Object[] values = new Object[_values.length];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_values, 0, values, 0, _size);

        return new MutableMap<>(_sortFunction, keys, values, _size);
    }

    private void enlargeArrays() {
        Object[] oldKeys = _keys;
        Object[] oldValues = _values;

        _keys = new Object[_size + GRANULARITY];
        _values = new Object[_size + GRANULARITY];

        for (int i = 0; i < _size; i++) {
            _keys[i] = oldKeys[i];
            _values[i] = oldValues[i];
        }
    }

    @Override
    public boolean clear() {
        final boolean somethingRemoved = _size > 0;
        final int suitableLength = suitableArrayLength(0);
        if (_keys.length != suitableLength) {
            _keys = new Object[suitableLength];
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

    public boolean put(K key, V value) {
        int index = findValue(_sortFunction, _keys, _size, key);
        if (index < 0) {
            if (_size != 0 && _size % GRANULARITY == 0) {
                enlargeArrays();
            }

            index = findSuitableIndex(_sortFunction, _keys, _size, key);
            for (int i = _size; i > index; i--) {
                _keys[i] = _keys[i - 1];
                _values[i] = _values[i - 1];
            }

            _keys[index] = key;
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

        if (_size != 1 && (_size % GRANULARITY) == 1) {
            Object[] oldKeys = _keys;
            Object[] oldValues = _values;

            _keys = new Object[--_size];
            _values = new Object[_size];

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
            --_size;
            for (int i = index; i < _size; i++) {
                _keys[i] = _keys[i + 1];
                _values[i] = _values[i + 1];
            }
        }
    }

    public boolean remove(K key) {
        int index = findValue(_sortFunction, _keys, _size, key);
        if (index >= 0) {
            removeAt(index);
            return true;
        }

        return false;
    }

    public static class Builder<K, V> implements MapBuilder<K, V> {
        private final MutableMap<K, V> _map;

        Builder(SortFunction<K> sortFunction) {
            _map = new MutableMap<>(sortFunction, new Object[GRANULARITY], new Object[GRANULARITY], 0);
        }

        @Override
        public Builder<K, V> put(K key, V value) {
            _map.put(key, value);
            return this;
        }

        @Override
        public MutableMap<K, V> build() {
            return _map;
        }
    }

    @Override
    public int hashCode() {
        final int length = _size;
        int hash = length;

        for (int i = 0; i < length; i++) {
            hash = hash * 31 + SortUtils.hashCode(_keys[i]);
        }

        return hash;
    }
}

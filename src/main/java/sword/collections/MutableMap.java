package sword.collections;

import static sword.collections.SortUtils.HASH_FOR_NULL;
import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;

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
public class MutableMap<K, V> extends AbstractIterable<V> implements Map<K, V> {

    private static final int GRANULARITY = 4;

    public static <K, V> MutableMap<K, V> empty() {
        return new MutableMap<>();
    }

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    private Object[] _keys;
    private int[] _hashCodes;
    private Object[] _values;
    private int _size;

    private MutableMap() {
        _keys = new Object[GRANULARITY];
        _hashCodes = new int[GRANULARITY];
        _values = new Object[GRANULARITY];
    }

    MutableMap(Object[] keys, int[] hashCodes, Object[] values, int size) {
        _keys = keys;
        _hashCodes = hashCodes;
        _size = size;
        _values = values;
    }

    @Override
    public boolean containsKey(K key) {
        return findKey(_hashCodes, _keys, _size, key) >= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K key, V defaultValue) {
        final int index = findKey(_hashCodes, _keys, _size, key);
        return (index >= 0)? (V) _values[index] : defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K key) {
        final int index = findKey(_hashCodes, _keys, _size, key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        return (V) _values[index];
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
        return findKey(_hashCodes, _keys, _size, key);
    }

    @Override
    public Set<K> keySet() {
        final Object[] keys = new Object[_size];
        final int[] hashCodes = new int[_size];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);

        return new ImmutableSet<>(keys, hashCodes);
    }

    @Override
    public Set<Entry<K, V>> entries() {
        final int length = _size;
        final Entry[] entries = new Entry[length];
        final int[] hashCodes = new int[length];

        for (int index = 0; index < length; index++) {
            entries[index] = new Entry<>(index, _keys[index], _values[index]);
            hashCodes[index] = entries[index].hashCode();
        }

        return new ImmutableSet<>(entries, hashCodes);
    }

    private class Iterator implements java.util.Iterator<V> {

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
    public Iterator iterator() {
        return new Iterator();
    }

    @Override
    public ImmutableMap<K, V> toImmutable() {
        Object[] keys = new Object[_size];
        int[] hashCodes = new int[_size];
        Object[] values = new Object[_size];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);
        System.arraycopy(_values, 0, values, 0, _size);

        return new ImmutableMap<>(keys, hashCodes, values);
    }

    @Override
    public MutableMap<K, V> mutate() {
        Object[] keys = new Object[_keys.length];
        int[] hashCodes = new int[_hashCodes.length];
        Object[] values = new Object[_values.length];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);
        System.arraycopy(_values, 0, values, 0, _size);

        return new MutableMap<>(keys, hashCodes, values, _size);
    }

    private void enlargeArrays() {
        Object[] oldKeys = _keys;
        int[] oldHashCodes = _hashCodes;
        Object[] oldValues = _values;

        _keys = new Object[_size + GRANULARITY];
        _hashCodes = new int[_size + GRANULARITY];
        _values = new Object[_size + GRANULARITY];

        for (int i = 0; i < _size; i++) {
            _keys[i] = oldKeys[i];
            _hashCodes[i] = oldHashCodes[i];
            _values[i] = oldValues[i];
        }
    }

    public boolean clear() {
        final boolean somethingRemoved = _size > 0;
        if (_size > GRANULARITY) {
            _hashCodes = new int[GRANULARITY];
        }

        if (somethingRemoved) {
            _keys = new Object[GRANULARITY];
            _values = new Object[GRANULARITY];
        }

        _size = 0;
        return somethingRemoved;
    }

    public boolean put(K key, V value) {
        int index = findKey(_hashCodes, _keys, _size, key);
        if (index < 0) {
            if (_size != 0 && _size % GRANULARITY == 0) {
                enlargeArrays();
            }

            final int hashCode = (key != null)? key.hashCode() : HASH_FOR_NULL;
            index = findSuitableIndex(_hashCodes, _size, hashCode);
            for (int i = _size; i > index; i--) {
                _keys[i] = _keys[i - 1];
                _hashCodes[i] = _hashCodes[i - 1];
                _values[i] = _values[i - 1];
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

    private void removeAt(int index) {
        if (_size != 1 && (_size % GRANULARITY) == 1) {
            Object[] oldKeys = _keys;
            int[] oldHashCodes = _hashCodes;
            Object[] oldValues = _values;

            _keys = new Object[--_size];
            _hashCodes = new int[_size];
            _values = new Object[_size];

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

    public boolean remove(K key) {
        int index = findKey(_hashCodes, _keys, _size, key);
        if (index >= 0) {
            removeAt(index);
            return true;
        }

        return false;
    }

    public static class Builder<K, V> implements MapBuilder<K, V> {
        private final MutableMap<K, V> _map = MutableMap.empty();

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
        final int length = _hashCodes.length;
        int hash = length;

        for (int i = 0; i < length; i++) {
            hash = hash * 31 + _hashCodes[i];
        }

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof MutableMap)) {
            return false;
        }
        else if (this == object) {
            return true;
        }

        final MutableMap that = (MutableMap) object;
        final int[] thatHashCodes = that._hashCodes;
        final int length = _hashCodes.length;
        if (length != thatHashCodes.length) {
            return false;
        }

        final ImmutableBitSetImpl.Builder builder = new ImmutableBitSetImpl.Builder();
        for (int i = 0; i < length; i++) {
            final int thisHash = _hashCodes[i];
            if (thisHash != thatHashCodes[i]) {
                return false;
            }

            if (i > 0 && _hashCodes[i - 1] == thisHash) {
                builder.add(i - 1);
                builder.add(i);
            }
        }
        final ImmutableBitSetImpl thisDuplicated = builder.build();
        ImmutableBitSetImpl thatDuplicated = thisDuplicated;

        final Object[] thatKeys = that._keys;
        final Object[] thatValues = that._values;
        for (int i = 0; i < length; i++) {
            final Object thisKey = _keys[i];
            final Object thisValue = _values[i];
            if (thisDuplicated.contains(i)) {
                boolean found = false;
                for (int pos : thatDuplicated) {
                    if ((thisKey == null && thatKeys[pos] == null || thisKey != null && thisKey.equals(thatKeys[pos])) &&
                            (thisValue == null && thatValues[pos] == null || thisValue != null && thisValue.equals(thatValues[pos]))) {
                        thatDuplicated = thatDuplicated.remove(pos);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    return false;
                }
            }
            else {
                final Object thatKey = thatKeys[i];
                final Object thatValue = thatValues[i];

                if (thisKey == null && thatKey != null || thisKey != null && !thisKey.equals(thatKey) ||
                        thisValue == null && thatValue != null || thisValue != null && !thisValue.equals(thatValue)) {
                    return false;
                }
            }
        }

        return thatDuplicated.isEmpty();
    }
}

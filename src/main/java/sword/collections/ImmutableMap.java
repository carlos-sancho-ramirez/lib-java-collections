package sword.collections;

import static sword.collections.SortUtils.HASH_FOR_NULL;
import static sword.collections.SortUtils.equal;
import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;

/**
 * Efficient implementation for an immutable Map when few elements are included.
 * 'Map' must be understood as a key-value pair collection where its key elements cannot be repeated.
 * 2 key elements are considered to be the same, so they would be duplicated, if both
 * return the same hash code and calling equals returns true.
 *
 * This Map is immutable, that means that its content cannot be modified once
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
 * @param <K> Type for the key elements within the Map
 * @param <V> Type for the value elements within the Map
 */
public final class ImmutableMap<K, V> extends AbstractIterable<Map.Entry<K, V>> implements Map<K, V> {

    private static final ImmutableMap<Object, Object> EMPTY = new ImmutableMap<>(new Object[0], new int[0], new Object[0]);

    @SuppressWarnings("unchecked")
    public static <K, V> ImmutableMap<K, V> empty() {
        return (ImmutableMap<K, V>) EMPTY;
    }

    private final Object[] _keys;
    private final int[] _hashCodes;
    private final Object[] _values;

    ImmutableMap(Object[] keys, int[] hashCodes, Object[] values) {
        _keys = keys;
        _hashCodes = hashCodes;
        _values = values;
    }

    @Override
    public boolean containsKey(K key) {
        return findKey(_hashCodes, _keys, _keys.length, key) >= 0;
    }

    @Override
    public V get(K key) {
        return get(key, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K key, V defaultValue) {
        final int index = findKey(_hashCodes, _keys, _keys.length, key);
        return (index >= 0)? (V) _values[index] : defaultValue;
    }

    @Override
    public int size() {
        return _keys.length;
    }

    @SuppressWarnings("unchecked")
    public K keyAt(int index) {
        return (K) _keys[index];
    }

    @SuppressWarnings("unchecked")
    public V valueAt(int index) {
        return (V) _values[index];
    }

    @Override
    public int indexOfKey(K key) {
        return findKey(_hashCodes, _keys, _keys.length, key);
    }

    @Override
    public ImmutableSet<K> keySet() {
        return new ImmutableSet<>(_keys, _hashCodes);
    }

    @Override
    public ImmutableMap<K, V> toImmutable() {
        return this;
    }

    @Override
    public MutableMap<K, V> mutate() {
        final int length = _keys.length;
        final int newLength = MutableMap.suitableArrayLength(length);

        Object[] keys = new Object[newLength];
        int[] hashCodes = new int[newLength];
        Object[] values = new Object[newLength];

        System.arraycopy(_keys, 0, keys, 0, length);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, length);
        System.arraycopy(_values, 0, values, 0, length);

        return new MutableMap<>(keys, hashCodes, values, length);
    }

    public ImmutableMap<K, V> put(K key, V value) {
        int index = findKey(_hashCodes, _keys, _keys.length, key);
        if (index >= 0) {
            if (equal(_values[index], value)) {
                return this;
            }
            else {
                final int length = _values.length;
                final Object[] newValues = new Object[length];
                for (int i = 0; i < length; i++) {
                    newValues[i] = (i == index)? value : _values[i];
                }

                return new ImmutableMap<>(_keys, _hashCodes, newValues);
            }
        }
        else {
            final int hashCode = (key != null)? key.hashCode() : HASH_FOR_NULL;
            index = findSuitableIndex(_hashCodes, _hashCodes.length, hashCode);

            final int newLength = _values.length + 1;
            final int[] newHashCodes = new int[newLength];
            final Object[] newKeys = new Object[newLength];
            final Object[] newValues = new Object[newLength];

            for (int i = 0; i < newLength; i++) {
                newHashCodes[i] = (i < index)? _hashCodes[i] : (i == index)? hashCode : _hashCodes[i - 1];
                newKeys[i] = (i < index)? _keys[i] : (i == index)? key : _keys[i - 1];
                newValues[i] = (i < index)? _values[i] : (i == index)? value : _values[i - 1];
            }

            return new ImmutableMap<>(newKeys, newHashCodes, newValues);
        }
    }

    /**
     * Return a new map instance where value has been transformed following the given function. Keys remain the same.
     * @param mapFunc Function to be applied to each value.
     */
    public ImmutableIntValueMap<K> mapValues(IntResultFunction<V> mapFunc) {
        final int itemCount = _keys.length;
        final int[] newValues = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableIntValueMap<>(_keys, _hashCodes, newValues);
    }

    /**
     * Return a new map instance where value has been transformed following the given function. Keys remain the same.
     * @param mapFunc Function to be applied to each value.
     * @param <U> New type for values
     */
    public <U> ImmutableMap<K, U> mapValues(Function<V, U> mapFunc) {
        final int itemCount = _keys.length;
        final Object[] newValues = new Object[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableMap<>(_keys, _hashCodes, newValues);
    }

    private class Iterator extends IteratorForImmutable<Entry<K, V>> {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _keys.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Entry<K, V> next() {
            return new Entry<>(_index, (K) _keys[_index], (V) _values[_index++]);
        }
    }

    @Override
    public Iterator iterator() {
        return new Iterator();
    }

    public static class Builder<K, V> implements MapBuilder<K, V> {
        private final MutableMap<K, V> _map = MutableMap.empty();

        @Override
        public Builder<K, V> put(K key, V value) {
            _map.put(key, value);
            return this;
        }

        @Override
        public ImmutableMap<K, V> build() {
            return _map.toImmutable();
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
        if (object == null || !(object instanceof ImmutableMap)) {
            return false;
        }
        else if (this == object) {
            return true;
        }

        final ImmutableMap that = (ImmutableMap) object;
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

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
 * @param <T> Type for the key elements within the Map
 */
public final class MutableIntValueMap<T> extends AbstractIterable<IntValueMap.Entry<T>> implements IntValueMap<T> {

    private static final int GRANULARITY = 4;

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    public static <E> MutableIntValueMap<E> empty() {
        final int length = suitableArrayLength(0);
        return new MutableIntValueMap<>(new Object[length], new int[length], new int[length], 0);
    }

    private Object[] _keys;
    private int[] _hashCodes;
    private int[] _values;
    private int _size;

    MutableIntValueMap(Object[] keys, int[] hashCodes, int[] values, int size) {
        _keys = keys;
        _hashCodes = hashCodes;
        _values = values;
        _size = size;
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    public int get(T key) {
        final int index = findKey(_hashCodes, _keys, _keys.length, key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        return _values[index];
    }

    @Override
    public int get(T key, int defaultValue) {
        final int index = findKey(_hashCodes, _keys, _size, key);
        return (index >= 0)? _values[index] : defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T keyAt(int index) {
        return (T) _keys[index];
    }

    @Override
    public int valueAt(int index) {
        return _values[index];
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

        return new ImmutableSet<>(keys, hashCodes);
    }

    private class Iterator implements java.util.Iterator<Entry<T>> {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Entry<T> next() {
            return new Entry<>(_index, (T) _keys[_index], _values[_index++]);
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
    public ImmutableIntValueMap<T> toImmutable() {
        Object[] keys = new Object[_size];
        int[] hashCodes = new int[_size];
        int[] values = new int[_size];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);
        System.arraycopy(_values, 0, values, 0, _size);

        return new ImmutableIntValueMap<>(keys, hashCodes, values);
    }

    @Override
    public MutableIntValueMap<T> mutate() {
        Object[] keys = new Object[_keys.length];
        int[] hashCodes = new int[_hashCodes.length];
        int[] values = new int[_values.length];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);
        System.arraycopy(_values, 0, values, 0, _size);

        return new MutableIntValueMap<>(keys, hashCodes, values, _size);
    }

    public boolean clear() {
        final boolean somethingRemoved = _size > 0;
        if (_size > GRANULARITY) {
            _hashCodes = new int[GRANULARITY];
        }

        if (somethingRemoved) {
            _keys = new Object[GRANULARITY];
            _values = new int[GRANULARITY];
        }

        _size = 0;
        return somethingRemoved;
    }

    public boolean put(T key, int value) {
        int index = findKey(_hashCodes, _keys, _size, key);
        if (index < 0) {
            final int hashCode = (key != null)? key.hashCode() : HASH_FOR_NULL;
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

    public void removeAt(int index) {
        if (index < 0 || index >= _size) {
            throw new IllegalArgumentException("Invalid index");
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

    public static class Builder<E> implements IntValueMapBuilder<E> {
        private final MutableIntValueMap<E> _map = MutableIntValueMap.empty();

        public Builder<E> put(E key, int value) {
            _map.put(key, value);
            return this;
        }

        public MutableIntValueMap<E> build() {
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
        if (object == null || !(object instanceof MutableIntValueMap)) {
            return false;
        }
        else if (this == object) {
            return true;
        }

        final MutableIntValueMap that = (MutableIntValueMap) object;
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
        final int[] thatValues = that._values;
        for (int i = 0; i < length; i++) {
            final Object thisKey = _keys[i];
            final int thisValue = _values[i];
            if (thisDuplicated.contains(i)) {
                boolean found = false;
                for (int pos : thatDuplicated) {
                    if ((thisKey == null && thatKeys[pos] == null || thisKey != null && thisKey.equals(thatKeys[pos])) &&
                            thisValue == thatValues[pos]) {
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
                final int thatValue = thatValues[i];

                if (thisKey == null && thatKey != null || thisKey != null && !thisKey.equals(thatKey) ||
                        thisValue != thatValue) {
                    return false;
                }
            }
        }

        return thatDuplicated.isEmpty();
    }
}

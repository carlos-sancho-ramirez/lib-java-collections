package sword.collections;

import static sword.collections.SortUtils.DEFAULT_GRANULARITY;
import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;

/**
 * Mutable small memory foot-print implementation for Map where keys are integers.
 * This implementation is inspired in the Android SparseArray, including extra features, and it is
 * independent from the Android system.
 *
 * This version implements Iterable as well, which means that it can be used in foreach expressions.
 * When iterating, the order is guaranteed to be in the key ascendant order of the elements.
 */
public final class MutableIntKeyMap<T> extends AbstractIntKeyMap<T> {

    private static final int GRANULARITY = DEFAULT_GRANULARITY;

    private int[] _keys;
    private Object[] _values;
    private int _size;

    public static <E> MutableIntKeyMap<E> empty() {
        return new MutableIntKeyMap<>();
    }

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    MutableIntKeyMap() {
        _keys = new int[GRANULARITY];
        _values = new Object[GRANULARITY];
    }

    MutableIntKeyMap(int[] keys, Object[] values, int size) {
        _keys = keys;
        _values = values;
        _size = size;
    }

    private void enlargeArrays() {
        int[] oldKeys = _keys;
        _keys = new int[_size + GRANULARITY];
        System.arraycopy(oldKeys, 0, _keys, 0, _size);

        Object[] oldValues = _values;
        _values = new Object[_size + GRANULARITY];
        System.arraycopy(oldValues, 0, _values, 0, _size);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int key) {
        final int index = findKey(_keys, _size, key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        return (T) _values[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int key, T defaultValue) {
        final int index = findKey(_keys, _size, key);
        return (index >= 0)? (T) _values[index] : defaultValue;
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    public int keyAt(int index) {
        return _keys[index];
    }

    @SuppressWarnings("unchecked")
    public T valueAt(int index) {
        return (T) _values[index];
    }

    @Override
    public int indexOfKey(int key) {
        return findKey(_keys, _size, key);
    }

    public void removeAt(int index) {
        if (index < 0 || index >= _size) {
            throw new IllegalArgumentException("Invalid index");
        }

        --_size;
        if (_size != 0 && (_size % GRANULARITY) == 0) {
            int[] oldKeys = _keys;
            _keys = new int[_size];

            Object[] oldValues = _values;
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
            for (int i = index; i < _size; i++) {
                _keys[i] = _keys[i + 1];
                _values[i] = _values[i + 1];
            }
        }
    }

    @Override
    public IntSet keySet() {
        ImmutableIntSetBuilder builder = new ImmutableIntSetBuilder();
        for (int i = 0; i < _size; i++) {
            builder.add(_keys[i]);
        }

        return builder.build();
    }

    @Override
    public List<T> valueList() {
        final int length = _size;
        final Object[] newValues = new Object[length];
        System.arraycopy(_values, 0, newValues, 0, length);
        return new ImmutableList<>(newValues);
    }

    @Override
    public ImmutableHashSet<Entry<T>> entries() {
        final int length = _size;
        final Entry[] entries = new Entry[length];
        final int[] hashCodes = new int[length];

        for (int index = 0; index < length; index++) {
            entries[index] = new Entry<>(index, _keys[index], _values[index]);
            hashCodes[index] = entries[index].hashCode();
        }

        return new ImmutableHashSet<>(entries, hashCodes);
    }

    @Override
    public ImmutableIntKeyMap<T> toImmutable() {
        final int[] keys = new int[_size];
        final Object[] values = new Object[_size];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_values, 0, values, 0, _size);

        return new ImmutableIntKeyMap<>(keys, values);
    }

    @Override
    public MutableIntKeyMap<T> mutate() {
        final int[] keys = new int[_keys.length];
        final Object[] values = new Object[_values.length];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_values, 0, values, 0, _size);

        return new MutableIntKeyMap<>(keys, values, _size);
    }

    public boolean clear() {
        final boolean somethingRemoved = _size > 0;
        if (_size > GRANULARITY) {
            _keys = new int[GRANULARITY];
        }

        if (somethingRemoved) {
            _values = new Object[GRANULARITY];
        }

        _size = 0;
        return somethingRemoved;
    }

    /**
     * Include the given key-value pair into the map.
     * This will override any previous value assigned to the given key, in case there was any.
     * This method will return true if this operation modified the map content.
     *
     * @param key Key to be included.
     * @param value Value to be included.
     * @return True if the key was not present or the value for that key was not equivalent to the
     *         one stored within the map, or false in any other case.
     */
    public boolean put(int key, T value) {
        int index = findKey(_keys, _size, key);
        if (index < 0) {
            if (_size != 0 && _size % GRANULARITY == 0) {
                enlargeArrays();
            }

            index = findSuitableIndex(_keys, _size, key);
            for (int i = _size; i > index; i--) {
                _keys[i] = _keys[i - 1];
                _values[i] = _values[i - 1];
            }

            _keys[index] = key;
            _values[index] = value;
            _size++;
        }
        else {
            T oldValue = valueAt(index);
            if (oldValue == null && value == null || oldValue != null && oldValue.equals(value)) {
                return false;
            }

            _values[index] = value;
        }

        return true;
    }

    /**
     * Remove the key-value pair matching the given key from the map.
     * @return True if something was removed, false otherwise.
     */
    public boolean remove(int key) {
        int index = findKey(_keys, _size, key);
        if (index >= 0) {
            removeAt(index);
            return true;
        }

        return false;
    }

    /**
     * Swaps keys and values in order to search keys given a value.
     *
     * This method do not check if its map is reversable, so if there is any duplicated values.
     */
    public ImmutableIntValueMap<T> reverse() {
        return new ImmutableIntValueMap<>(_values, _keys);
    }

    @Override
    public Transformer<T> iterator() {
        return new Iterator();
    }

    public static class Builder<E> implements IntKeyMapBuilder<E> {
        private final MutableIntKeyMap<E> _map = new MutableIntKeyMap<>();

        @Override
        public Builder<E> put(int key, E value) {
            _map.put(key, value);
            return this;
        }

        @Override
        public MutableIntKeyMap<E> build() {
            return _map;
        }
    }

    private class Iterator extends AbstractTransformer<T> {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            return (T) _values[_index++];
        }

        @Override
        public void remove() {
            removeAt(--_index);
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < _size; i++) {
            final Object value = _values[i];
            hash = (hash * 31 + _keys[i]) * 31 + SortUtils.hashCode(value);
        }

        return hash;
    }
}

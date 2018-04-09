package sword.collections;

import java.util.Arrays;

import static sword.collections.SortUtils.equal;
import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;

/**
 * Immutable small memory foot-print implementation for Map where keys are integers.
 * This implementation is inspired in the Android SparseArray but ensures that is immutable and
 * independent from the Android system.
 *
 * This version implements Iterable as well, which means that it can be used in foreach expressions.
 * When iterating, the order is guaranteed to be in the key ascendant order of the elements.
 */
public final class ImmutableIntKeyMap<T> extends AbstractIterable<T> implements IntKeyMap<T> {

    private static final ImmutableIntKeyMap<Object> EMPTY = new ImmutableIntKeyMap<>(new int[0], new Object[0]);

    @SuppressWarnings("unchecked")
    public static <E> ImmutableIntKeyMap<E> empty() {
        return (ImmutableIntKeyMap<E>) EMPTY;
    }

    /**
     * Sorted set of integers
     */
    private final int[] _keys;

    /**
     * These are the values for the keys in _keys, matching them by array index.
     * This array must match in length with _keys.
     */
    private final Object[] _values;

    ImmutableIntKeyMap(int[] keys, Object[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException();
        }

        _keys = keys;
        _values = values;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int key) {
        final int index = findKey(_keys, _keys.length, key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        return (T) _values[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int key, T defaultValue) {
        final int index = findKey(_keys, _keys.length, key);
        return (index >= 0)? (T) _values[index] : defaultValue;
    }

    @Override
    public int size() {
        return _keys.length;
    }

    @Override
    public int keyAt(int index) {
        return _keys[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public T valueAt(int index) {
        return (T) _values[index];
    }

    @Override
    public int indexOfKey(int key) {
        return findKey(_keys, _keys.length, key);
    }

    @Override
    public ImmutableIntSet keySet() {
        return (_keys.length != 0)? new ImmutableIntSetImpl(_keys) : ImmutableIntSetImpl.empty();
    }

    @Override
    public ImmutableSet<Entry<T>> entries() {
        final int length = _keys.length;
        final Entry[] entries = new Entry[length];
        final int[] hashCodes = new int[length];

        for (int index = 0; index < length; index++) {
            entries[index] = new Entry<>(index, _keys[index], _values[index]);
            hashCodes[index] = entries[index].hashCode();
        }

        return new ImmutableSet<>(entries, hashCodes);
    }

    /**
     * Return a new map instance where value has been transformed following the given function. Keys remain the same.
     * @param mapFunc Function the must be applied to values in order to modify them.
     */
    public ImmutableIntPairMap mapValues(IntResultFunction<T> mapFunc) {
        final int size = _keys.length;
        final int[] newValues = new int[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = mapFunc.apply((T) _values[i]);
        }

        return new ImmutableIntPairMap(_keys, newValues);
    }

    /**
     * Return a new map instance where value has been transformed following the given function. Keys remain the same.
     * @param mapFunc Function the must be applied to values in order to modify them.
     * @param <U> New type for values
     */
    public <U> ImmutableIntKeyMap<U> mapValues(Function<T, U> mapFunc) {
        final int size = _keys.length;
        final Object[] newValues = new Object[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = mapFunc.apply((T) _values[i]);
        }

        return new ImmutableIntKeyMap<>(_keys, newValues);
    }

    @Override
    public ImmutableIntKeyMap<T> toImmutable() {
        return this;
    }

    @Override
    public MutableIntKeyMap<T> mutate() {
        final int length = _keys.length;
        final int newLength = MutableIntKeyMap.suitableArrayLength(length);

        final int[] keys = new int[newLength];
        final Object[] values = new Object[newLength];

        System.arraycopy(_keys, 0, keys, 0, length);
        System.arraycopy(_values, 0, values, 0, length);

        return new MutableIntKeyMap<>(keys, values, length);
    }

    public ImmutableIntKeyMap<T> put(int key, T value) {
        int index = findKey(_keys, _keys.length, key);
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

                return new ImmutableIntKeyMap<T>(_keys, newValues);
            }
        }
        else {
            index = findSuitableIndex(_keys, _keys.length, key);

            final int newLength = _values.length + 1;
            final int[] newKeys = new int[newLength];
            final Object[] newValues = new Object[newLength];

            for (int i = 0; i < newLength; i++) {
                newKeys[i] = (i < index)? _keys[i] : (i == index)? key : _keys[i - 1];
                newValues[i] = (i < index)? _values[i] : (i == index)? value : _values[i - 1];
            }

            return new ImmutableIntKeyMap<T>(newKeys, newValues);
        }
    }

    /**
     * Swaps keys and values in order to search keys given a value.
     *
     * This method do not check if its map is reversable, so if there is any duplicated values.
     */
    @SuppressWarnings("unchecked")
    public ImmutableIntValueMap<T> reverse() {
        return new ImmutableIntValueMap<>(_values, _keys);
    }

    @Override
    public java.util.Iterator<T> iterator() {
        return new Iterator();
    }

    public static class Builder<E> implements IntKeyMapBuilder<E> {
        private final MutableIntKeyMap<E> _map = new MutableIntKeyMap<>();

        @Override
        public IntKeyMapBuilder<E> add(E element) {
            _map.add(element);
            return this;
        }

        @Override
        public Builder<E> put(int key, E value) {
            _map.put(key, value);
            return this;
        }

        @Override
        public ImmutableIntKeyMap<E> build() {
            final int length = _map.size();
            final int[] keys = new int[length];
            final Object[] values = new Object[length];

            for (int i = 0; i < length; i++) {
                keys[i] = _map.keyAt(i);
                values[i] = _map.valueAt(i);
            }

            return new ImmutableIntKeyMap<>(keys, values);
        }
    }

    private class Iterator extends IteratorForImmutable<T> {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _keys.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            return (T) _values[_index++];
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] {_keys, _values});
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ImmutableIntKeyMap)) {
            return false;
        }

        final ImmutableIntKeyMap that = (ImmutableIntKeyMap) other;
        return Arrays.equals(_keys, that._keys) && Arrays.equals(_values, that._values);
    }
}

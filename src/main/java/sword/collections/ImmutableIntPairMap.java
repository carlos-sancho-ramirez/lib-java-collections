package sword.collections;

import java.util.Arrays;

import sword.collections.SortUtils.SwapMethod;

import static sword.collections.SortUtils.equal;
import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;
import static sword.collections.SortUtils.quickSort;

/**
 * Immutable small memory foot-print implementation for Map where both keys and values are integers.
 * This implementation is inspired in the Android SparseIntArray but ensures that is immutable and
 * independent from the Android system.
 *
 * This version implements Iterable as well, which means that it can be used in foreach expressions.
 * When iterating, the order is guaranteed to be in the key ascendant order of the elements.
 */
public final class ImmutableIntPairMap extends AbstractSizable implements IntPairMap {

    private static final ImmutableIntPairMap EMPTY = new ImmutableIntPairMap(new int[0], new int[0]);

    public static ImmutableIntPairMap empty() {
        return EMPTY;
    }

    /**
     * Value returned when key is not found
     */
    private static final int DEFAULT_VALUE = 0;

    /**
     * Sorted set of integers
     */
    private final int[] _keys;

    /**
     * These are the values for the keys in _keys, matching them by array index.
     * This array must match in length with _keys.
     */
    private final int[] _values;

    ImmutableIntPairMap(int[] keys, int[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException();
        }

        _keys = keys;
        _values = values;
    }

    /**
     * Return the value assigned to the given key. Or 'valueIfKeyNotFound' if that key is not in the map.
     */
    @Override
    public int get(int key, int valueIfKeyNotFound) {
        final int index = findKey(_keys, _keys.length, key);
        return (index >= 0)? _values[index] : valueIfKeyNotFound;
    }

    @Override
    public int get(int key) {
        final int index = findKey(_keys, _keys.length, key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        return _values[index];
    }

    @Override
    public int size() {
        return _keys.length;
    }

    public int keyAt(int index) {
        return _keys[index];
    }

    public int valueAt(int index) {
        return _values[index];
    }

    @Override
    public int indexOfKey(int key) {
        return findKey(_keys, _keys.length, key);
    }

    public ImmutableIntSet keySet() {
        return (_keys.length != 0)? new ImmutableIntSetImpl(_keys) : ImmutableIntSetImpl.empty();
    }

    /**
     * Return a new map instance where values has been transformed following the given function. Keys remain the same.
     * @param mapFunc Function to be applied to each value.
     */
    public ImmutableIntPairMap mapValues(IntToIntFunction mapFunc) {
        final int size = _keys.length;
        final int[] newValues = new int[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = mapFunc.apply(_values[i]);
        }

        return new ImmutableIntPairMap(_keys, newValues);
    }

    /**
     * Return a new map instance where values has been transformed following the given function. Keys remain the same.
     * @param mapFunc Function to be applied to each value.
     * @param <U> New type for values
     */
    public <U> ImmutableIntKeyMap<U> mapValues(IntFunction<U> mapFunc) {
        final int size = _keys.length;
        final Object[] newValues = new Object[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = mapFunc.apply(_values[i]);
        }

        return new ImmutableIntKeyMap<>(_keys, newValues);
    }

    @Override
    public ImmutableIntPairMap toImmutable() {
        return this;
    }

    @Override
    public MutableIntPairMap mutate() {
        final int length = _keys.length;
        final int newLength = MutableIntPairMap.suitableArrayLength(length);

        final int[] keys = new int[newLength];
        final int[] values = new int[newLength];

        System.arraycopy(_keys, 0, keys, 0, length);
        System.arraycopy(_values, 0, values, 0, length);

        return new MutableIntPairMap(keys, values, length);
    }

    public ImmutableIntPairMap put(int key, int value) {
        int index = findKey(_keys, _keys.length, key);
        if (index >= 0) {
            if (equal(_values[index], value)) {
                return this;
            }
            else {
                final int length = _values.length;
                final int[] newValues = new int[length];
                for (int i = 0; i < length; i++) {
                    newValues[i] = (i == index)? value : _values[i];
                }

                return new ImmutableIntPairMap(_keys, newValues);
            }
        }
        else {
            index = findSuitableIndex(_keys, _keys.length, key);

            final int newLength = _values.length + 1;
            final int[] newKeys = new int[newLength];
            final int[] newValues = new int[newLength];

            for (int i = 0; i < newLength; i++) {
                newKeys[i] = (i < index)? _keys[i] : (i == index)? key : _keys[i - 1];
                newValues[i] = (i < index)? _values[i] : (i == index)? value : _values[i - 1];
            }

            return new ImmutableIntPairMap(newKeys, newValues);
        }
    }

    public ImmutableIntPairMap reverse() {
        // TODO: Ensure that no repeated keys are going inside the reversed version
        final int length = _values.length;
        if (length > 1) {
            final int[] values = new int[length];
            final int[] keys = new int[length];

            for (int i = 0; i < length; i++) {
                values[i] = _values[i];
                keys[i] = _keys[i];
            }

            quickSort(values, 0, length - 1, new SwapMethod() {
                @Override
                public void apply(int index1, int index2) {
                    int temp = values[index1];
                    values[index1] = values[index2];
                    values[index2] = temp;

                    temp = keys[index1];
                    keys[index1] = keys[index2];
                    keys[index2] = temp;
                }
            });
            return new ImmutableIntPairMap(values, keys);
        }
        else {
            return new ImmutableIntPairMap(_values, _keys);
        }
    }

    @Override
    public java.util.Iterator<Entry> iterator() {
        return new Iterator();
    }

    public static class Builder implements IntPairMapBuilder {
        private static final int GRANULARITY = 12;
        private int _size;

        private int[] _keys = new int[GRANULARITY];
        private int[] _values = new int[GRANULARITY];

        private void enlargeArrays() {
            int[] oldKeys = _keys;
            int[] oldValues = _values;

            _keys = new int[_size + GRANULARITY];
            _values = new int[_size + GRANULARITY];

            for (int i = 0; i < _size; i++) {
                _keys[i] = oldKeys[i];
                _values[i] = oldValues[i];
            }
        }

        public Builder put(int key, int value) {
            int index = findKey(_keys, _size, key);
            if (index >= 0) {
                _values[index] = value;
            }
            else {
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

            return this;
        }

        public ImmutableIntPairMap build() {
            final int length = _size;
            final int[] keys = new int[length];
            final int[] values = new int[length];

            for (int i = 0; i < length; i++) {
                keys[i] = _keys[i];
                values[i] = _values[i];
            }

            return new ImmutableIntPairMap(keys, values);
        }
    }

    private class Iterator extends IteratorForImmutable<Entry> {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _keys.length;
        }

        @Override
        public Entry next() {
            final Entry entry = new Entry(_index, _keys[_index], _values[_index]);
            _index++;
            return entry;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] {_keys, _values});
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ImmutableIntPairMap)) {
            return false;
        }

        final ImmutableIntPairMap that = (ImmutableIntPairMap) other;
        return Arrays.equals(_keys, that._keys) && Arrays.equals(_values, that._values);
    }
}

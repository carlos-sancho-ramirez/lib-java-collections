package sword.collections;

import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;

/**
 * Mutable small memory foot-print implementation for Map where both keys and values are integers.
 * This implementation is inspired in the Android SparseIntArray, including extra features, and it is
 * independent from the Android system.
 *
 * This version implements Iterable as well, which means that it can be used in foreach expressions.
 * When iterating, the order is guaranteed to be in the key ascendant order of the elements.
 */
public final class MutableIntPairMap extends AbstractIntPairMap implements MutableIntTransformable {

    private final ArrayLengthFunction _arrayLengthFunction;
    private int[] _keys;
    private int[] _values;
    private int _size;

    public static MutableIntPairMap empty(ArrayLengthFunction arrayLengthFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, 0);
        return new MutableIntPairMap(arrayLengthFunction, new int[length], new int[length], 0);
    }

    public static MutableIntPairMap empty() {
        return empty(GranularityBasedArrayLengthFunction.getInstance());
    }

    MutableIntPairMap(ArrayLengthFunction arrayLengthFunction, int[] keys, int[] values, int size) {
        _arrayLengthFunction = arrayLengthFunction;
        _keys = keys;
        _values = values;
        _size = size;
    }

    @Override
    public int get(int key) {
        final int index = findKey(_keys, _size, key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        return _values[index];
    }

    @Override
    public int get(int key, int defaultValue) {
        final int index = findKey(_keys, _size, key);
        return (index >= 0)? _values[index] : defaultValue;
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    public int keyAt(int index) {
        return _keys[index];
    }

    @Override
    public int valueAt(int index) {
        return _values[index];
    }

    @Override
    public int last() throws EmptyCollectionException {
        if (_size == 0) {
            throw new EmptyCollectionException();
        }

        return _values[_size - 1];
    }

    @Override
    public int indexOfKey(int key) {
        return findKey(_keys, _size, key);
    }

    @Override
    public Set<Entry> entries() {
        final int length = _size;
        final Entry[] entries = new Entry[length];
        final int[] hashCodes = new int[length];

        for (int index = 0; index < length; index++) {
            entries[index] = new Entry(index, _keys[index], _values[index]);
            hashCodes[index] = entries[index].hashCode();
        }

        return new ImmutableHashSet<>(entries, hashCodes);
    }

    @Override
    public IntPairMap mapToInt(IntToIntFunction mapFunc) {
        final int size = _size;
        final int[] newValues = new int[size];
        final int[] newKeys = new int[size];
        for (int i = 0; i < size; i++) {
            newKeys[i] = _keys[i];
            newValues[i] = mapFunc.apply(_values[i]);
        }

        return new ImmutableIntPairMap(newKeys, newValues);
    }

    @Override
    public <U> IntKeyMap<U> map(IntFunction<? extends U> mapFunc) {
        final int size = _size;
        final Object[] newValues = new Object[size];
        final int[] newKeys = new int[size];
        for (int i = 0; i < size; i++) {
            newKeys[i] = _keys[i];
            newValues[i] = mapFunc.apply(_values[i]);
        }

        return new ImmutableIntKeyMap<>(newKeys, newValues);
    }

    @Override
    public ImmutableIntPairMap toImmutable() {
        if (_size == 0) {
            return ImmutableIntPairMap.empty();
        }
        else {
            final int[] keys = new int[_size];
            final int[] values = new int[_size];

            System.arraycopy(_keys, 0, keys, 0, _size);
            System.arraycopy(_values, 0, values, 0, _size);

            return new ImmutableIntPairMap(keys, values);
        }
    }

    @Override
    public MutableIntPairMap mutate(ArrayLengthFunction arrayLengthFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, _size);
        final int[] keys = new int[length];
        final int[] values = new int[length];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_values, 0, values, 0, _size);

        return new MutableIntPairMap(arrayLengthFunction, keys, values, _size);
    }

    @Override
    public MutableIntPairMap mutate() {
        return mutate(_arrayLengthFunction);
    }

    @Override
    public void removeAt(int index) {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        final int desiredLength = _arrayLengthFunction.suitableArrayLength(_keys.length, --_size);
        if (desiredLength != _keys.length) {
            int[] oldKeys = _keys;
            int[] oldValues = _values;

            _keys = new int[desiredLength];
            _values = new int[desiredLength];

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
    public boolean clear() {
        final boolean somethingRemoved = _size > 0;
        final int length = _arrayLengthFunction.suitableArrayLength(_values.length, 0);
        if (length != _values.length) {
            _keys = new int[length];
            _values = new int[length];
        }

        _size = 0;
        return somethingRemoved;
    }

    @Override
    public IntSet keySet() {
        ImmutableIntSetCreator builder = new ImmutableIntSetCreator();
        for (int i = 0; i < _size; i++) {
            builder.add(_keys[i]);
        }

        return builder.build();
    }

    /**
     * Include the given key-value pair into the map.
     * This will override any previous value assigned to the given key, in case there was any.
     * This method will return true if this operation modified the map content.
     *
     * @param key Key to be included.
     * @param value Value to be included.
     * @return True if the key was not present or the value for that key was different from the
     *         one stored within the map, or false in any other case.
     */
    public boolean put(int key, int value) {
        int index = findKey(_keys, _size, key);
        if (index < 0) {
            final int desiredLength = _arrayLengthFunction.suitableArrayLength(_keys.length, _size + 1);
            index = findSuitableIndex(_keys, _size, key);
            if (desiredLength != _keys.length) {
                int[] oldKeys = _keys;
                int[] oldValues = _values;

                _keys = new int[desiredLength];
                _values = new int[desiredLength];

                if (index > 0) {
                    System.arraycopy(oldKeys, 0, _keys, 0, _size);
                    System.arraycopy(oldValues, 0, _values, 0, _size);
                }

                if (_size > index) {
                    System.arraycopy(oldKeys, index, _keys, index + 1, _size - index);
                    System.arraycopy(oldValues, index, _values, index + 1, _size - index);
                }
            }
            else {
                for (int i = _size; i > index; i--) {
                    _keys[i] = _keys[i - 1];
                    _values[i] = _values[i - 1];
                }
            }

            _keys[index] = key;
            _values[index] = value;
            _size++;
        }
        else {
            if (valueAt(index) == value) {
                return false;
            }

            _values[index] = value;
        }

        return true;
    }

    /**
     * Inserts into this map all the data found in the given map.
     *
     * As this is a map, duplicated keys will not be allowed.
     * That means that elements within the given map will replace any value in
     * this map if there is an equivalent key already included in this map.
     *
     * @param other Map from where new items will be added.
     * @return Whether this operation has increased the size of this map or not.
     */
    public boolean putAll(IntPairMap other) {
        boolean changed = false;
        for (IntPairMap.Entry entry : other.entries()) {
            changed |= put(entry.key(), entry.value());
        }

        return changed;
    }

    /**
     * Create a new mutable map instance and copy from this collection the actual data references. After it, this collection gets cleared.
     * <p>
     * This is a more efficient alternative to the following code:
     * <code>
     * <br>MutableIntPairMap newMap = map.mutate();
     * <br>map.clear();
     * </code>
     *
     * @return A new mutable map that contains the actual data of this map.
     */
    public MutableIntPairMap donate() {
        final MutableIntPairMap newMap = new MutableIntPairMap(_arrayLengthFunction, _keys, _values, _size);
        final int length = _arrayLengthFunction.suitableArrayLength(0, 0);
        _keys = new int[length];
        _values = new int[length];
        _size = 0;
        return newMap;
    }

    /**
     * Removes from this map the entry whose key matches the given key, returning its matching value.
     *
     * This method will shrink the size of this map in one, except if the given key is not present.
     * In case the given key is not present, an {@link UnmappedKeyException} will be thrown.
     *
     * This method is exactly the same as executing the following snippet:
     * <pre>
     *     int value = map.get(key);
     *     map.remove(key);
     *     return value;
     * </pre>
     *
     * @param key Key expected to be found within this map.
     * @return The value associated with the given key.
     * @throws UnmappedKeyException if the given key is not present in this map.
     */
    public int pick(int key) throws UnmappedKeyException {
        final int index = indexOfKey(key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        final int value = valueAt(index);
        removeAt(index);
        return value;
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

    @Override
    public IntTransformer iterator() {
        return new Iterator();
    }

    @Override
    public IntList toList() {
        final int length = _size;
        final int[] newValues = new int[length];
        System.arraycopy(_values, 0, newValues, 0, length);
        return new ImmutableIntList(newValues);
    }

    @Override
    public IntPairMap filter(IntPredicate predicate) {
        final ImmutableIntPairMap.Builder builder = new ImmutableIntPairMap.Builder();
        final int length = _size;
        for (int i = 0; i < length; i++) {
            final int value = _values[i];
            if (predicate.apply(value)) {
                builder.put(_keys[i], value);
            }
        }

        return builder.build();
    }

    @Override
    public IntPairMap filterNot(IntPredicate predicate) {
        final ImmutableIntPairMap.Builder builder = new ImmutableIntPairMap.Builder();
        final int length = _size;
        for (int i = 0; i < length; i++) {
            final int value = _values[i];
            if (!predicate.apply(value)) {
                builder.put(_keys[i], value);
            }
        }

        return builder.build();
    }

    public static class Builder implements IntPairMapBuilder {
        private final MutableIntPairMap _map;

        public Builder() {
            _map = empty();
        }

        public Builder(ArrayLengthFunction arrayLengthFunction) {
            _map = empty(arrayLengthFunction);
        }

        @Override
        public Builder put(int key, int value) {
            _map.put(key, value);
            return this;
        }

        @Override
        public MutableIntPairMap build() {
            return _map;
        }
    }

    private class Iterator extends AbstractIntTransformer {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _size;
        }

        @Override
        public Integer next() {
            return _values[_index++];
        }

        @Override
        public void remove() {
            removeAt(--_index);
        }
    }
}

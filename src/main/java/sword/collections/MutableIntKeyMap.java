package sword.collections;

import sword.collections.SortUtils.SwapMethod;

import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;
import static sword.collections.SortUtils.quickSort;

/**
 * Mutable small memory foot-print implementation for Map where keys are integers.
 * This implementation is inspired in the Android SparseArray, including extra features, and it is
 * independent from the Android system.
 *
 * This version implements Iterable as well, which means that it can be used in foreach expressions.
 * When iterating, the order is guaranteed to be in the key ascendant order of the elements.
 */
public final class MutableIntKeyMap<T> extends AbstractIntKeyMap<T> implements MutableTransformable<T> {

    private final ArrayLengthFunction _arrayLengthFunction;
    private int[] _keys;
    private Object[] _values;
    private int _size;

    public static <E> MutableIntKeyMap<E> empty(ArrayLengthFunction arrayLengthFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, 0);
        return new MutableIntKeyMap<>(arrayLengthFunction, new int[length], new Object[length], 0);
    }

    public static <E> MutableIntKeyMap<E> empty() {
        return empty(GranularityBasedArrayLengthFunction.getInstance());
    }

    MutableIntKeyMap(ArrayLengthFunction arrayLengthFunction, int[] keys, Object[] values, int size) {
        _arrayLengthFunction = arrayLengthFunction;
        _keys = keys;
        _values = values;
        _size = size;
    }

    @Override
    public IntKeyMap<T> filter(Predicate<? super T> predicate) {
        final ImmutableIntKeyMap.Builder<T> builder = new ImmutableIntKeyMap.Builder<>();
        for (int i = 0; i < _size; i++) {
            T value = valueAt(i);
            if (predicate.apply(value)) {
                builder.put(_keys[i], value);
            }
        }

        return builder.build();
    }

    @Override
    public IntKeyMap<T> filterNot(Predicate<? super T> predicate) {
        final ImmutableIntKeyMap.Builder<T> builder = new ImmutableIntKeyMap.Builder<>();
        for (int i = 0; i < _size; i++) {
            T value = valueAt(i);
            if (!predicate.apply(value)) {
                builder.put(_keys[i], value);
            }
        }

        return builder.build();
    }

    @Override
    public <E> IntKeyMap<E> map(Function<? super T, ? extends E> func) {
        final int[] newKeys = new int[_size];
        final Object[] newValues = new Object[_size];

        for (int i = 0; i < _size; i++) {
            newKeys[i] = _keys[i];
            newValues[i] = func.apply(valueAt(i));
        }

        return new ImmutableIntKeyMap<>(newKeys, newValues);
    }

    @Override
    public IntPairMap mapToInt(IntResultFunction<? super T> func) {
        final int[] newKeys = new int[_size];
        final int[] newValues = new int[_size];

        for (int i = 0; i < _size; i++) {
            newKeys[i] = _keys[i];
            newValues[i] = func.apply(valueAt(i));
        }

        return new ImmutableIntPairMap(newKeys, newValues);
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

    /**
     * Create a new mutable map instance and copy from this collection the actual data references. After it, this collection gets cleared.
     * <p>
     * This is a more efficient alternative to the following code:
     * <code>
     * <br>MutableIntKeyMap newMap = map.mutate();
     * <br>map.clear();
     * </code>
     *
     * @return A new mutable map that contains the actual data of this map.
     */
    public MutableIntKeyMap<T> donate() {
        final MutableIntKeyMap<T> newMap = new MutableIntKeyMap<>(_arrayLengthFunction, _keys, _values, _size);
        final int length = _arrayLengthFunction.suitableArrayLength(0, 0);
        _keys = new int[length];
        _values = new Object[length];
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
     *     V value = map.get(key);
     *     map.remove(key);
     *     return value;
     * </pre>
     *
     * @param key Key expected to be found within this map.
     * @return The value associated with the given key.
     * @throws UnmappedKeyException if the given key is not present in this map.
     */
    public T pick(int key) throws UnmappedKeyException {
        final int index = indexOfKey(key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        final T value = valueAt(index);
        removeAt(index);
        return value;
    }

    @Override
    public void removeAt(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        final int desiredLength = _arrayLengthFunction.suitableArrayLength(_values.length, --_size);
        if (desiredLength != _values.length) {
            int[] oldKeys = _keys;
            Object[] oldValues = _values;

            _keys = new int[desiredLength];
            _values = new Object[desiredLength];

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
        ImmutableIntSetCreator builder = new ImmutableIntSetCreator();
        for (int i = 0; i < _size; i++) {
            builder.add(_keys[i]);
        }

        return builder.build();
    }

    @Override
    public List<T> toList() {
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
        if (_size == 0) {
            return ImmutableIntKeyMap.empty();
        }

        final int[] keys = new int[_size];
        final Object[] values = new Object[_size];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_values, 0, values, 0, _size);

        return new ImmutableIntKeyMap<>(keys, values);
    }

    @Override
    public MutableIntKeyMap<T> mutate(ArrayLengthFunction arrayLengthFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, _size);
        final int[] keys = new int[length];
        final Object[] values = new Object[length];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_values, 0, values, 0, _size);

        return new MutableIntKeyMap<>(arrayLengthFunction, keys, values, _size);
    }

    @Override
    public MutableIntKeyMap<T> mutate() {
        return mutate(_arrayLengthFunction);
    }

    @Override
    public boolean clear() {
        final boolean somethingRemoved = _size > 0;
        final int desiredLength = _arrayLengthFunction.suitableArrayLength(_values.length, 0);
        if (desiredLength != _keys.length) {
            _keys = new int[desiredLength];
            _values = new Object[desiredLength];
        }
        else if (somethingRemoved) {
            for (int i = 0; i < _size; i++) {
                _values[i] = null;
            }
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
            final int desiredLength = _arrayLengthFunction.suitableArrayLength(_values.length, _size + 1);
            index = findSuitableIndex(_keys, _size, key);
            if (desiredLength != _values.length) {
                int[] oldKeys = _keys;
                Object[] oldValues = _values;

                _keys = new int[desiredLength];
                _values = new Object[desiredLength];

                if (index > 0) {
                    System.arraycopy(oldKeys, 0, _keys, 0, index);
                    System.arraycopy(oldValues, 0, _values, 0, index);
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
    public IntValueMap<T> invert() {
        final int length = _values.length;
        final Object[] newKeys = new Object[length];
        final int[] newHashCodes = new int[length];
        final int[] newValues = new int[length];

        System.arraycopy(_values, 0, newKeys, 0, length);
        System.arraycopy(_keys, 0, newValues, 0, length);

        for (int i = 0; i < length; i++) {
            newHashCodes[i] = SortUtils.hashCode(_values[i]);
        }

        quickSort(newHashCodes, 0, length - 1, new SwapMethod() {
            @Override
            public void apply(int index1, int index2) {
                int temp = newValues[index1];
                newValues[index1] = newValues[index2];
                newValues[index2] = temp;

                Object aux = newKeys[index1];
                newKeys[index1] = newKeys[index2];
                newKeys[index2] = aux;

                temp = newHashCodes[index1];
                newHashCodes[index1] = newHashCodes[index2];
                newHashCodes[index2] = temp;
            }
        });

        return new ImmutableIntValueHashMap<>(newKeys, newHashCodes, newValues);
    }

    @Override
    public Transformer<T> iterator() {
        return new Iterator();
    }

    public static class Builder<E> implements IntKeyMapBuilder<E> {
        private final MutableIntKeyMap<E> _map;

        public Builder(ArrayLengthFunction arrayLengthFunction) {
            _map = empty(arrayLengthFunction);
        }

        public Builder() {
            _map = empty();
        }

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

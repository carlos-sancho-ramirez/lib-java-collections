package sword.collections;

import static sword.collections.SortUtils.*;

/**
 * Immutable small memory foot-print implementation for Map where keys are integers.
 * This implementation is inspired in the Android SparseArray but ensures that is immutable and
 * independent from the Android system.
 *
 * This version implements Iterable as well, which means that it can be used in foreach expressions.
 * When iterating, the order is guaranteed to be in the key ascendant order of the elements.
 */
public final class ImmutableIntKeyMap<T> extends AbstractIntKeyMap<T> implements IterableImmutableCollection<T> {

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
    public ImmutableList<T> valueList() {
        return new ImmutableList<>(_values);
    }

    @Override
    public ImmutableHashSet<Entry<T>> entries() {
        final int length = _keys.length;
        final Entry[] entries = new Entry[length];
        final int[] hashCodes = new int[length];

        for (int index = 0; index < length; index++) {
            entries[index] = new Entry<>(index, _keys[index], _values[index]);
            hashCodes[index] = entries[index].hashCode();
        }

        return new ImmutableHashSet<>(entries, hashCodes);
    }

    @Override
    public ImmutableIntKeyMap<T> filter(Predicate<T> predicate) {
        final Builder<T> builder = new Builder<>();
        final int length = _values.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            final T value = valueAt(i);
            if (predicate.apply(value)) {
                builder.put(_keys[i], value);
            }
            else {
                changed = true;
            }
        }

        return changed? builder.build() : this;
    }

    @Override
    public ImmutableIntKeyMap<T> filterNot(Predicate<T> predicate) {
        final Builder<T> builder = new Builder<>();
        final int length = _values.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            final T value = valueAt(i);
            if (!predicate.apply(value)) {
                builder.put(_keys[i], value);
            }
            else {
                changed = true;
            }
        }

        return changed? builder.build() : this;
    }

    /**
     * Return a new map instance where value has been transformed following the given function. Keys remain the same.
     * @param mapFunc Function the must be applied to values in order to modify them.
     */
    public ImmutableIntPairMap map(IntResultFunction<T> mapFunc) {
        final int size = _keys.length;
        final int[] newValues = new int[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = mapFunc.apply((T) _values[i]);
        }

        return new ImmutableIntPairMap(_keys, newValues);
    }

    @Override
    public <U> ImmutableIntKeyMap<U> map(Function<T, U> mapFunc) {
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
     * This method do not check if its map is invertible, so if there is any duplicated values.
     */
    public ImmutableIntValueMap<T> invert() {
        final int length = _values.length;
        final Object[] newKeys = new Object[length];
        final int[] newHashCodes = new int[length];
        final int[] newValues = new int[length];

        System.arraycopy(_values, 0, newKeys, 0, length);
        System.arraycopy(_keys, 0, newValues, 0, length);

        for (int i = 0; i < length; i++) {
            newHashCodes[i] = SortUtils.hashCode(_values[i]);
        }

        if (length > 1) {
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
        }

        return new ImmutableIntValueHashMap<>(newKeys, newHashCodes, newValues);
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
        public ImmutableIntKeyMap<E> build() {
            final int length = _map.size();
            if (length == 0) {
                return empty();
            }
            else {
                final int[] keys = new int[length];
                final Object[] values = new Object[length];

                for (int i = 0; i < length; i++) {
                    keys[i] = _map.keyAt(i);
                    values[i] = _map.valueAt(i);
                }

                return new ImmutableIntKeyMap<>(keys, values);
            }
        }
    }

    private class Iterator extends AbstractTransformer<T> {

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
        final int length = _keys.length;
        int hash = 0;
        for (int i = 0; i < length; i++) {
            final Object value = _values[i];
            hash = (hash * 31 + _keys[i]) * 31 + (SortUtils.hashCode(value));
        }

        return hash;
    }
}

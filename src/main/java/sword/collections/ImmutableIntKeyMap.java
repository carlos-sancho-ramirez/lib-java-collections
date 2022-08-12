package sword.collections;

import sword.collections.SortUtils.SwapMethod;

import static sword.collections.SortUtils.equal;
import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;
import static sword.collections.SortUtils.quickSort;

/**
 * Immutable small memory foot-print implementation for Map where keys are integers.
 * This implementation is inspired in the Android SparseArray but ensures that is immutable and
 * independent from the Android system.
 *
 * This version implements Iterable as well, which means that it can be used in foreach expressions.
 * When iterating, the order is guaranteed to be in the key ascendant order of the elements.
 */
public final class ImmutableIntKeyMap<T> extends AbstractIntKeyMap<T> implements ImmutableTransformable<T> {

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
    public T last() throws EmptyCollectionException {
        final int size = _keys.length;
        if (size == 0) {
            throw new EmptyCollectionException();
        }

        return valueAt(size - 1);
    }

    @Override
    public int indexOfKey(int key) {
        return findKey(_keys, _keys.length, key);
    }

    @Override
    public ImmutableIntSet keySet() {
        return (_keys.length != 0)? new ImmutableIntArraySet(_keys) : ImmutableIntArraySet.empty();
    }

    @Override
    public ImmutableList<T> toList() {
        return new ImmutableList<>(_values);
    }

    @Override
    public ImmutableSet<T> toSet() {
        return toList().toSet();
    }

    @Override
    public ImmutableIntSet indexes() {
        final int size = size();
        return (size == 0)? ImmutableIntArraySet.empty() : new ImmutableIntRange(0, size - 1);
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
    public ImmutableIntKeyMap<T> filter(Predicate<? super T> predicate) {
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
    public ImmutableIntKeyMap<T> filterNot(Predicate<? super T> predicate) {
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

    @Override
    public ImmutableIntKeyMap<T> filterByKey(IntPredicate predicate) {
        final Builder<T> builder = new Builder<>();
        final int length = _values.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            final int key = _keys[i];
            if (predicate.apply(key)) {
                builder.put(key, valueAt(i));
            }
            else {
                changed = true;
            }
        }

        return changed? builder.build() : this;
    }

    @Override
    public ImmutableIntKeyMap<T> filterByEntry(Predicate<IntKeyMapEntry<T>> predicate) {
        final Builder<T> builder = new Builder<>();
        final int length = _values.length;
        boolean changed = false;
        if (length > 0) {
            final ReusableIntKeyMapEntry<T> entry = new ReusableIntKeyMapEntry<>();
            for (int i = 0; i < length; i++) {
                final int key = _keys[i];
                final T value = valueAt(i);
                entry.set(key, value);
                if (predicate.apply(entry)) {
                    builder.put(key, value);
                }
                else {
                    changed = true;
                }
            }
        }

        return changed? builder.build() : this;
    }

    /**
     * Return a new map instance where value has been transformed following the given function. Keys remain the same.
     * @param mapFunc Function the must be applied to values in order to modify them.
     */
    @Override
    public ImmutableIntPairMap mapToInt(IntResultFunction<? super T> mapFunc) {
        final int size = _keys.length;
        final int[] newValues = new int[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = mapFunc.apply((T) _values[i]);
        }

        return new ImmutableIntPairMap(_keys, newValues);
    }

    @Override
    public <U> ImmutableIntKeyMap<U> map(Function<? super T, ? extends U> mapFunc) {
        final int size = _keys.length;
        final Object[] newValues = new Object[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = mapFunc.apply((T) _values[i]);
        }

        return new ImmutableIntKeyMap<>(_keys, newValues);
    }

    @Override
    public ImmutableIntKeyMap<T> removeAt(int index) {
        final int size = _values.length;
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        final int[] newKeys = new int[size - 1];
        final Object[] newValues = new Object[size - 1];
        if (index > 0) {
            System.arraycopy(_values, 0, newValues, 0, index);
            System.arraycopy(_keys, 0, newKeys, 0, index);
        }

        final int remaining = size - index - 1;
        if (remaining > 0) {
            System.arraycopy(_values, index + 1, newValues, index, remaining);
            System.arraycopy(_keys, index + 1, newKeys, index, remaining);
        }

        return new ImmutableIntKeyMap<>(newKeys, newValues);
    }

    @Override
    public ImmutableIntKeyMap<T> toImmutable() {
        return this;
    }

    @Override
    public MutableIntKeyMap<T> mutate(ArrayLengthFunction arrayLengthFunction) {
        final int size = _keys.length;
        final int length = arrayLengthFunction.suitableArrayLength(0, size);
        final int[] keys = new int[length];
        final Object[] values = new Object[length];

        System.arraycopy(_keys, 0, keys, 0, size);
        System.arraycopy(_values, 0, values, 0, size);

        return new MutableIntKeyMap<>(arrayLengthFunction, keys, values, size);
    }

    @Override
    public MutableIntKeyMap<T> mutate() {
        return mutate(GranularityBasedArrayLengthFunction.getInstance());
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
     * Creates a new map containing all the current elements and the ones given in the map.
     *
     * As this is a map, duplicated keys will not be allowed.
     * Than means that elements within the given map will replace any value in this map if
     * there is an equivalent key already included in this map.
     *
     * @param other Map from where new items will be added.
     */
    public ImmutableIntKeyMap<T> putAll(IntKeyMap<? extends T> other) {
        ImmutableIntKeyMap<T> result = this;
        for (IntKeyMap.Entry<? extends T> entry : other.entries()) {
            result = result.put(entry.key(), entry.value());
        }

        return result;
    }

    @Override
    public ImmutableIntValueMap<T> count() {
        final MutableIntValueMap<T> result = MutableIntValueHashMap.empty();
        for (T value : this) {
            final int amount = result.get(value, 0);
            result.put(value, amount + 1);
        }

        return result.toImmutable();
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
    public ImmutableIntKeyMap<T> slice(ImmutableIntRange range) {
        final int size = _values.length;
        final int min = range.min();
        final int max = range.max();
        if (min >= size || max < 0) {
            return ImmutableIntKeyMap.empty();
        }

        if (range.min() <= 0 && range.max() >= size - 1) {
            return this;
        }

        final int newSize = Math.min(max, size - 1) - min + 1;
        final int[] newKeys = new int[newSize];
        final Object[] newValues = new Object[newSize];
        System.arraycopy(_keys, min, newKeys, 0, newSize);
        System.arraycopy(_values, min, newValues, 0, newSize);

        return new ImmutableIntKeyMap<>(newKeys, newValues);
    }

    @Override
    public ImmutableIntKeyMap<T> skip(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Unable to skip a negative number of elements");
        }
        else if (length == 0) {
            return this;
        }
        else if (length >= _values.length) {
            return empty();
        }

        final int remain = _values.length - length;
        final int[] newKeys = new int[remain];
        final Object[] newValues = new Object[remain];
        System.arraycopy(_keys, length, newKeys, 0, remain);
        System.arraycopy(_values, length, newValues, 0, remain);
        return new ImmutableIntKeyMap<>(newKeys, newValues);
    }

    /**
     * Returns a new ImmutableIntKeyMap where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of this map in iteration order.
     * @return A new ImmutableIntKeyMap instance just including the first elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length is equal or greater than the
     *         actual size of this collection.
     */
    @Override
    public ImmutableIntKeyMap<T> take(int length) {
        final int size = _values.length;
        if (length >= size) {
            return this;
        }

        if (length == 0) {
            return ImmutableIntKeyMap.empty();
        }

        final int[] newKeys = new int[length];
        final Object[] newValues = new Object[length];
        System.arraycopy(_keys, 0, newKeys, 0, length);
        System.arraycopy(_values, 0, newValues, 0, length);
        return new ImmutableIntKeyMap<>(newKeys, newValues);
    }

    @Override
    public TransformerWithIntKey<T> iterator() {
        return new Iterator();
    }

    public static class Builder<E> implements IntKeyMapBuilder<E> {
        private final MutableIntKeyMap<E> _map;

        public Builder() {
            _map = MutableIntKeyMap.empty();
        }

        public Builder(ArrayLengthFunction arrayLengthFunction) {
            _map = MutableIntKeyMap.empty(arrayLengthFunction);
        }

        @Override
        public Builder<E> put(int key, E value) {
            _map.put(key, value);
            return this;
        }

        @Override
        public ImmutableIntKeyMap<E> build() {
            return _map.toImmutable();
        }
    }

    private final class Iterator extends AbstractTransformer<T> implements TransformerWithIntKey<T> {

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

        @Override
        public int key() {
            return _keys[_index - 1];
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

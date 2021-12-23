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
public final class ImmutableIntPairMap extends AbstractIntPairMap implements IntPairMap, ImmutableIntTransformable {

    private static final ImmutableIntPairMap EMPTY = new ImmutableIntPairMap(new int[0], new int[0]);

    public static ImmutableIntPairMap empty() {
        return EMPTY;
    }

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
        return (_keys.length != 0)? new ImmutableIntArraySet(_keys) : ImmutableIntArraySet.empty();
    }

    @Override
    public ImmutableHashSet<Entry> entries() {
        final int length = _keys.length;
        final Entry[] entries = new Entry[length];
        final int[] hashCodes = new int[length];

        for (int index = 0; index < length; index++) {
            entries[index] = new Entry(index, _keys[index], _values[index]);
            hashCodes[index] = entries[index].hashCode();
        }

        return new ImmutableHashSet<>(entries, hashCodes);
    }

    @Override
    public ImmutableIntList toList() {
        return new ImmutableIntList(_values);
    }

    @Override
    public ImmutableIntSet toSet() {
        return toList().toSet();
    }

    @Override
    public ImmutableIntPairMap filter(IntPredicate predicate) {
        final Builder builder = new Builder();
        final int length = _values.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            final int value = _values[i];
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
    public ImmutableIntPairMap filterNot(IntPredicate predicate) {
        final Builder builder = new Builder();
        final int length = _values.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            final int value = _values[i];
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
    public ImmutableIntPairMap filterByKey(IntPredicate predicate) {
        final ImmutableIntPairMap.Builder builder = new ImmutableIntPairMap.Builder();
        final int length = _values.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            final int key = _keys[i];
            if (predicate.apply(key)) {
                builder.put(key, _values[i]);
            }
            else {
                changed = true;
            }
        }

        return changed? builder.build() : this;
    }

    @Override
    public ImmutableIntPairMap filterByEntry(Predicate<IntPairMapEntry> predicate) {
        final ImmutableIntPairMap.Builder builder = new ImmutableIntPairMap.Builder();
        final int length = _values.length;
        boolean changed = false;
        if (length > 0) {
            final ReusableIntPairMapEntry entry = new ReusableIntPairMapEntry();
            for (int i = 0; i < length; i++) {
                final int key = _keys[i];
                final int value = _values[i];
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

    @Override
    public ImmutableIntPairMap mapToInt(IntToIntFunction mapFunc) {
        final int size = _keys.length;
        final int[] newValues = new int[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = mapFunc.apply(_values[i]);
        }

        return new ImmutableIntPairMap(_keys, newValues);
    }

    @Override
    public <U> ImmutableIntKeyMap<U> map(IntFunction<? extends U> mapFunc) {
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
    public MutableIntPairMap mutate(ArrayLengthFunction arrayLengthFunction) {
        final int size = _keys.length;
        final int length = arrayLengthFunction.suitableArrayLength(0, size);

        final int[] keys = new int[length];
        final int[] values = new int[length];

        System.arraycopy(_keys, 0, keys, 0, size);
        System.arraycopy(_values, 0, values, 0, size);

        return new MutableIntPairMap(arrayLengthFunction, keys, values, size);
    }

    @Override
    public MutableIntPairMap mutate() {
        return mutate(GranularityBasedArrayLengthFunction.getInstance());
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

    /**
     * Creates a new map containing all the current elements and the ones given in the map.
     *
     * As this is a map, duplicated keys will not be allowed.
     * Than means that elements within the given map will replace any value in this map if
     * there is an equivalent key already included in this map.
     *
     * @param other Map from where new items will be added.
     */
    public ImmutableIntPairMap putAll(IntPairMap other) {
        ImmutableIntPairMap result = this;
        for (IntPairMap.Entry entry : other.entries()) {
            result = result.put(entry.key(), entry.value());
        }

        return result;
    }

    @Override
    public ImmutableIntPairMap removeAt(int index) {
        final int size = _values.length;
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        final int[] newKeys = new int[size - 1];
        final int[] newValues = new int[size - 1];
        if (index > 0) {
            System.arraycopy(_values, 0, newValues, 0, index);
            System.arraycopy(_keys, 0, newKeys, 0, index);
        }

        final int remaining = size - index - 1;
        if (remaining > 0) {
            System.arraycopy(_values, index + 1, newValues, index, remaining);
            System.arraycopy(_keys, index + 1, newKeys, index, remaining);
        }

        return new ImmutableIntPairMap(newKeys, newValues);
    }

    @Override
    public ImmutableIntPairMap count() {
        MutableIntPairMap result = MutableIntPairMap.empty();
        for (int value : this) {
            final int amount = result.get(value, 0);
            result.put(value, amount + 1);
        }

        return result.toImmutable();
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
    public IntTransformer iterator() {
        return new Iterator();
    }

    public static class Builder implements IntPairMapBuilder {
        private final MutableIntPairMap _map;

        public Builder() {
            _map = MutableIntPairMap.empty();
        }

        public Builder(ArrayLengthFunction arrayLengthFunction) {
            _map = MutableIntPairMap.empty(arrayLengthFunction);
        }

        public Builder put(int key, int value) {
            _map.put(key, value);
            return this;
        }

        public ImmutableIntPairMap build() {
            return _map.toImmutable();
        }
    }

    private class Iterator extends AbstractIntTransformer {
        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _keys.length;
        }

        @Override
        public Integer next() {
            return _values[_index++];
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ImmutableIntPairMap)) {
            return super.equals(other);
        }

        final ImmutableIntPairMap that = (ImmutableIntPairMap) other;
        return Arrays.equals(_keys, that._keys) && Arrays.equals(_values, that._values);
    }
}

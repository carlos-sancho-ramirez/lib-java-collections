package sword.collections;

import static sword.collections.SortUtils.equal;
import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;

/**
 * Efficient implementation for map where keys are internally
 * sorted according to their hash codes in ascending order.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should create a builder in order to obtain
 * an instance of it.
 *
 * This implementation assumes that keys inserted are also immutable.
 * Mutation of the contained keys may result in duplicates within
 * the keys or wrong sorting of keys.
 * It is not guaranteed to work if keys are mutable.
 * This does no apply to values of the map, that can mutate without risk.
 *
 * @param <K> Type for the key elements within the Map
 * @param <V> Type for the value elements within the Map
 */
public final class ImmutableHashMap<K, V> extends AbstractImmutableMap<K, V> {

    private static final ImmutableHashMap<Object, Object> EMPTY = new ImmutableHashMap<>(new Object[0], new int[0], new Object[0]);

    @SuppressWarnings("unchecked")
    public static <K, V> ImmutableHashMap<K, V> empty() {
        return (ImmutableHashMap<K, V>) EMPTY;
    }

    private final int[] _hashCodes;

    ImmutableHashMap(Object[] keys, int[] hashCodes, Object[] values) {
        super(keys, values);
        _hashCodes = hashCodes;
    }

    @Override
    public int indexOfKey(K key) {
        return findKey(_hashCodes, _keys, _keys.length, key);
    }

    @Override
    public ImmutableHashSet<K> keySet() {
        return new ImmutableHashSet<>(_keys, _hashCodes);
    }

    @Override
    boolean entryLessThan(Entry<K, V> a, Entry<K, V> b) {
        return b != null && (a == null || SortUtils.compareByHashCode(a.key(), b.key()));
    }

    @Override
    public ImmutableHashMap<K, V> toImmutable() {
        return this;
    }

    @Override
    public MutableHashMap<K, V> mutate(ArrayLengthFunction arrayLengthFunction) {
        final int size = _keys.length;
        final int length = arrayLengthFunction.suitableArrayLength(0, size);

        Object[] keys = new Object[length];
        int[] hashCodes = new int[length];
        Object[] values = new Object[length];

        System.arraycopy(_keys, 0, keys, 0, size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, size);
        System.arraycopy(_values, 0, values, 0, size);

        return new MutableHashMap<>(arrayLengthFunction, keys, hashCodes, values, size);
    }

    @Override
    public MutableHashMap<K, V> mutate() {
        return mutate(GranularityBasedArrayLengthFunction.getInstance());
    }

    @Override
    public ImmutableHashMap<K, V> put(K key, V value) {
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

                return new ImmutableHashMap<>(_keys, _hashCodes, newValues);
            }
        }
        else {
            final int hashCode = SortUtils.hashCode(key);
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

            return new ImmutableHashMap<>(newKeys, newHashCodes, newValues);
        }
    }

    @Override
    public ImmutableHashMap<K, V> putAll(Map<? extends K, ? extends V> other) {
        return (ImmutableHashMap<K, V>) super.putAll(other);
    }

    @Override
    public ImmutableSet<V> toSet() {
        return toList().toSet();
    }

    @Override
    public ImmutableIntSet indexes() {
        final int size = size();
        return (size == 0)? ImmutableIntArraySet.empty() : new ImmutableIntRange(0, size - 1);
    }

    @Override
    public ImmutableHashMap<K, V> filter(Predicate<? super V> predicate) {
        final Builder<K, V> builder = new Builder<>();
        final int length = _keys.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            V value = valueAt(i);
            if (predicate.apply(value)) {
                builder.put(keyAt(i), value);
            }
            else {
                changed = true;
            }
        }

        return changed? builder.build() : this;
    }

    @Override
    public ImmutableHashMap<K, V> filterNot(Predicate<? super V> predicate) {
        final Builder<K, V> builder = new Builder<>();
        final int length = _keys.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            V value = valueAt(i);
            if (predicate.apply(value)) {
                changed = true;
            }
            else {
                builder.put(keyAt(i), value);
            }
        }

        return changed? builder.build() : this;
    }

    @Override
    public ImmutableHashMap<K, V> filterByKey(Predicate<? super K> predicate) {
        final Builder<K, V> builder = new Builder<>();
        final int length = _keys.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            K key = keyAt(i);
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
    public ImmutableHashMap<K, V> filterByKeyNot(Predicate<? super K> predicate) {
        final Builder<K, V> builder = new Builder<>();
        final int length = _keys.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            K key = keyAt(i);
            if (!predicate.apply(key)) {
                builder.put(key, valueAt(i));
            }
            else {
                changed = true;
            }
        }

        return changed? builder.build() : this;
    }

    @Override
    public ImmutableHashMap<K, V> filterByEntry(Predicate<MapEntry<K, V>> predicate) {
        final Builder<K, V> builder = new Builder<>();
        final int length = _keys.length;
        boolean changed = false;
        if (length > 0) {
            final ReusableMapEntry<K, V> entry = new ReusableMapEntry<>();
            for (int i = 0; i < length; i++) {
                K key = keyAt(i);
                V value = valueAt(i);
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
    public ImmutableIntValueHashMap<K> mapToInt(IntResultFunction<? super V> mapFunc) {
        final int itemCount = _keys.length;
        final int[] newValues = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableIntValueHashMap<>(_keys, _hashCodes, newValues);
    }

    @Override
    public <U> ImmutableHashMap<K, U> map(Function<? super V, ? extends U> mapFunc) {
        final int itemCount = _keys.length;
        final Object[] newValues = new Object[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableHashMap<>(_keys, _hashCodes, newValues);
    }

    @Override
    public ImmutableHashMap<K, V> removeAt(int index) {
        final int size = _values.length;
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        final Object[] newKeys = new Object[size - 1];
        final int[] newHashCodes = new int[size - 1];
        final Object[] newValues = new Object[size - 1];
        if (index > 0) {
            System.arraycopy(_values, 0, newValues, 0, index);
            System.arraycopy(_keys, 0, newKeys, 0, index);
            System.arraycopy(_hashCodes, 0, newHashCodes, 0, index);
        }

        final int remaining = size - index - 1;
        if (remaining > 0) {
            System.arraycopy(_values, index + 1, newValues, index, remaining);
            System.arraycopy(_keys, index + 1, newKeys, index, remaining);
            System.arraycopy(_hashCodes, index + 1, newHashCodes, index, remaining);
        }

        return new ImmutableHashMap<>(newKeys, newHashCodes, newValues);
    }

    @Override
    public ImmutableHashMap<K, V> slice(ImmutableIntRange range) {
        final int size = _values.length;
        final int min = range.min();
        final int max = range.max();
        if (min >= size || max < 0) {
            return ImmutableHashMap.empty();
        }

        if (range.min() <= 0 && range.max() >= size - 1) {
            return this;
        }

        final int newSize = Math.min(max, size - 1) - min + 1;
        final Object[] newKeys = new Object[newSize];
        final int[] newHashCodes = new int[newSize];
        final Object[] newValues = new Object[newSize];
        System.arraycopy(_keys, min, newKeys, 0, newSize);
        System.arraycopy(_hashCodes, min, newHashCodes, 0, newSize);
        System.arraycopy(_values, min, newValues, 0, newSize);

        return new ImmutableHashMap<>(newKeys, newHashCodes, newValues);
    }

    private ImmutableHashMap<K, V> skip(int index, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Unable to skip a negative number of elements");
        }
        else if (length == 0 || _values.length == 0) {
            return this;
        }
        else if (length >= _values.length) {
            return empty();
        }

        final int remain = _values.length - length;
        final Object[] newKeys = new Object[remain];
        final Object[] newValues = new Object[remain];
        final int[] newHashCodes = new int[remain];
        System.arraycopy(_keys, index, newKeys, 0, remain);
        System.arraycopy(_hashCodes, index, newHashCodes, 0, remain);
        System.arraycopy(_values, index, newValues, 0, remain);
        return new ImmutableHashMap<>(newKeys, newHashCodes, newValues);
    }

    private ImmutableHashMap<K, V> take(int index, int length) {
        final int size = _values.length;
        if (length >= size) {
            return this;
        }

        if (length == 0) {
            return ImmutableHashMap.empty();
        }

        final Object[] newKeys = new Object[length];
        final int[] newHashCodes = new int[length];
        final Object[] newValues = new Object[length];
        System.arraycopy(_keys, index, newKeys, 0, length);
        System.arraycopy(_hashCodes, index, newHashCodes, 0, length);
        System.arraycopy(_values, index, newValues, 0, length);
        return new ImmutableHashMap<>(newKeys, newHashCodes, newValues);
    }

    @Override
    public ImmutableHashMap<K, V> skip(int length) {
        return skip(length, length);
    }

    /**
     * Returns a new ImmutableHashMap where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of this map in iteration order.
     * @return A new ImmutableHashMap instance just including the first elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length is equal or greater than the
     *         actual size of this collection.
     */
    @Override
    public ImmutableHashMap<K, V> take(int length) {
        return take(0, length);
    }

    /**
     * Returns a new ImmutableHashMap where the <code>length</code> amount of
     * last elements has been removed.
     * <p>
     * This will return the empty instance if the given parameter matches
     * or exceeds the length of this map.
     *
     * @param length the amount of elements to be removed from the end of the map.
     * @return A new ImmutableHashMap instance without the last elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance if the given length is equal or greater
     *         than the actual length of the set.
     */
    @Override
    public ImmutableHashMap<K, V> skipLast(int length) {
        return skip(0, length);
    }

    /**
     * Returns a new ImmutableHashMap where only the <code>length</code> amount of
     * last elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the end of this map.
     * @return A new ImmutableHashMap instance just including the last elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length equals or greater than the
     *         actual size of this collection.
     */
    public ImmutableHashMap<K, V> takeLast(int length) {
        return take(_values.length - length, length);
    }

    public static class Builder<K, V> implements ImmutableMap.Builder<K, V> {
        private final MutableHashMap<K, V> _map;

        public Builder() {
            _map = MutableHashMap.empty();
        }

        public Builder(ArrayLengthFunction arrayLengthFunction) {
            _map = MutableHashMap.empty(arrayLengthFunction);
        }

        @Override
        public Builder<K, V> put(K key, V value) {
            _map.put(key, value);
            return this;
        }

        @Override
        public ImmutableHashMap<K, V> build() {
            return _map.toImmutable();
        }
    }
}

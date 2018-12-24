package sword.collections;

import static sword.collections.SortUtils.*;

/**
 * Efficient implementation for an immutable Map when few elements are included.
 * 'Map' must be understood as a key-value pair collection where its key elements cannot be repeated.
 * 2 key elements are considered to be the same, so they would be duplicated, if both
 * return the same hash code and calling equals returns true.
 *
 * This Map is immutable, that means that its content cannot be modified once
 * it is created. This also means that, as no algorithms to insert, modify and
 * remove are required, its memory layout can be simplified and its footprint
 * can be reduced in a more optimal way.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should create a builder in order to obtain
 * an instance of it.
 *
 * This implementation assumes that elements inserted are also immutable.
 * It is not guaranteed to work if any of the elements is mutable.
 *
 * This class also implements the {@link Iterable} interface, which
 * ensures that the for-each construction can be used.
 *
 * @param <K> Type for the key elements within the Map
 * @param <V> Type for the value elements within the Map
 */
public final class ImmutableHashMap<K, V> extends ImmutableMap<K, V> {

    private static final ImmutableHashMap<Object, Object> EMPTY = new ImmutableHashMap<>(new Object[0], new int[0], new Object[0]);

    @SuppressWarnings("unchecked")
    public static <K, V> ImmutableHashMap<K, V> empty() {
        return (ImmutableHashMap<K, V>) EMPTY;
    }

    private final int[] _hashCodes;

    ImmutableHashMap(Object[] keys, int[] hashCodes, Object[] values) {
        super(SortUtils::compareByHashCode, keys, values);
        _hashCodes = hashCodes;
    }

    @Override
    public boolean containsKey(K key) {
        return findKey(_hashCodes, _keys, _keys.length, key) >= 0;
    }

    @Override
    public V get(K key) {
        final int index = findKey(_hashCodes, _keys, _keys.length, key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        return valueAt(index);
    }

    @Override
    public V get(K key, V defaultValue) {
        final int index = findKey(_hashCodes, _keys, _keys.length, key);
        return (index >= 0)? valueAt(index) : defaultValue;
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
    public ImmutableHashMap<K, V> toImmutable() {
        return this;
    }

    @Override
    public MutableHashMap<K, V> mutate() {
        final int length = _keys.length;
        final int newLength = MutableMap.suitableArrayLength(length);

        Object[] keys = new Object[newLength];
        int[] hashCodes = new int[newLength];
        Object[] values = new Object[newLength];

        System.arraycopy(_keys, 0, keys, 0, length);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, length);
        System.arraycopy(_values, 0, values, 0, length);

        return new MutableHashMap<>(keys, hashCodes, values, length);
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
    public ImmutableHashMap<K, V> filter(Predicate<V> predicate) {
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
    public ImmutableHashMap<K, V> filterNot(Predicate<V> predicate) {
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
    public ImmutableIntValueMap<K> map(IntResultFunction<V> mapFunc) {
        final int itemCount = _keys.length;
        final int[] newValues = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableIntValueMap<>(_keys, _hashCodes, newValues);
    }

    @Override
    public <U> ImmutableHashMap<K, U> map(Function<V, U> mapFunc) {
        final int itemCount = _keys.length;
        final Object[] newValues = new Object[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableHashMap<>(_keys, _hashCodes, newValues);
    }

    public static class Builder<K, V> implements MapBuilder<K, V> {
        private final MutableHashMap<K, V> _map = MutableHashMap.empty();

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

    @Override
    public int hashCode() {
        final int length = _hashCodes.length;
        int hash = length;

        for (int i = 0; i < length; i++) {
            hash = hash * 31 + _hashCodes[i];
        }

        return hash;
    }
}

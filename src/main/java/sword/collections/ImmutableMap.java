package sword.collections;

import static sword.collections.SortUtils.equal;
import static sword.collections.SortUtils.findValue;
import static sword.collections.SortUtils.findSuitableIndex;

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
public class ImmutableMap<K, V> extends AbstractMap<K, V> implements IterableImmutableCollection<V> {

    private final SortFunction<K> _sortFunction;
    final Object[] _keys;
    final Object[] _values;

    ImmutableMap(SortFunction<K> sortFunction, Object[] keys, Object[] values) {
        _sortFunction = sortFunction;
        _keys = keys;
        _values = values;
    }

    @Override
    public boolean containsKey(K key) {
        return findValue(_sortFunction, _keys, _keys.length, key) >= 0;
    }

    @Override
    public V get(K key) {
        final int index = findValue(_sortFunction, _keys, _keys.length, key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        return valueAt(index);
    }

    @Override
    public V get(K key, V defaultValue) {
        final int index = findValue(_sortFunction, _keys, _keys.length, key);
        return (index >= 0)? valueAt(index) : defaultValue;
    }

    @Override
    public int size() {
        return _keys.length;
    }

    @SuppressWarnings("unchecked")
    public K keyAt(int index) {
        return (K) _keys[index];
    }

    @SuppressWarnings("unchecked")
    public V valueAt(int index) {
        return (V) _values[index];
    }

    @Override
    public int indexOfKey(K key) {
        return findValue(_sortFunction, _keys, _keys.length, key);
    }

    @Override
    public ImmutableSet<K> keySet() {
        return new ImmutableSortedSet<>(_sortFunction, _keys);
    }

    @Override
    public ImmutableList<V> valueList() {
        return new ImmutableList<>(_values);
    }

    @Override
    public ImmutableSet<Entry<K, V>> entries() {
        final int length = _keys.length;
        final Entry[] entries = new Entry[length];

        for (int index = 0; index < length; index++) {
            entries[index] = new Entry<>(index, _keys[index], _values[index]);
        }

        final SortFunction<Entry<K, V>> entrySortFunction = (a, b) ->
                b != null && (a == null || _sortFunction.lessThan(a.key(), b.key()));
        return new ImmutableSortedSet<>(entrySortFunction, entries);
    }

    @Override
    public ImmutableMap<K, V> toImmutable() {
        return this;
    }

    @Override
    public MutableMap<K, V> mutate() {
        final int length = _keys.length;
        final int newLength = MutableMap.suitableArrayLength(length);

        Object[] keys = new Object[newLength];
        Object[] values = new Object[newLength];

        System.arraycopy(_keys, 0, keys, 0, length);
        System.arraycopy(_values, 0, values, 0, length);

        return new MutableMap<>(_sortFunction, keys, values, length);
    }

    public ImmutableMap<K, V> put(K key, V value) {
        int index = findValue(_sortFunction, _keys, _keys.length, key);
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

                return new ImmutableMap<>(_sortFunction, _keys, newValues);
            }
        }
        else {
            index = findSuitableIndex(_sortFunction, _keys, _keys.length, key);

            final int newLength = _values.length + 1;
            final Object[] newKeys = new Object[newLength];
            final Object[] newValues = new Object[newLength];

            for (int i = 0; i < newLength; i++) {
                newKeys[i] = (i < index)? _keys[i] : (i == index)? key : _keys[i - 1];
                newValues[i] = (i < index)? _values[i] : (i == index)? value : _values[i - 1];
            }

            return new ImmutableMap<>(_sortFunction, newKeys, newValues);
        }
    }

    @Override
    public ImmutableMap<K, V> filter(Predicate<V> predicate) {
        final Builder<K, V> builder = new Builder<>(_sortFunction);
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
    public ImmutableMap<K, V> filterNot(Predicate<V> predicate) {
        final Builder<K, V> builder = new Builder<>(_sortFunction);
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
        final int[] hashCodes = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
            hashCodes[i] = SortUtils.hashCode(_keys[i]);
        }

        // TODO: Change this whenever ImmutableIntValueMap can be sorted through a SortFunction
        return new ImmutableIntValueMap<>(_keys, hashCodes, newValues);
    }

    @Override
    public <U> ImmutableMap<K, U> map(Function<V, U> mapFunc) {
        final int itemCount = _keys.length;
        final Object[] newValues = new Object[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableMap<>(_sortFunction, _keys, newValues);
    }

    private class Iterator extends AbstractTransformer<V> {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _keys.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V next() {
            return (V) _values[_index++];
        }
    }

    @Override
    public Iterator iterator() {
        return new Iterator();
    }

    public static class Builder<K, V> implements MapBuilder<K, V> {
        private final MutableMap<K, V> _map;

        Builder(SortFunction<K> sortFunction) {
            final int length = MutableMap.suitableArrayLength(0);
            _map = new MutableMap<>(sortFunction, new Object[length], new Object[length], 0);
        }

        @Override
        public Builder<K, V> put(K key, V value) {
            _map.put(key, value);
            return this;
        }

        @Override
        public ImmutableMap<K, V> build() {
            return _map.toImmutable();
        }
    }

    @Override
    public int hashCode() {
        final int length = _keys.length;
        int hash = length;

        for (int i = 0; i < length; i++) {
            hash = hash * 31 + SortUtils.hashCode(_keys[i]);
        }

        return hash;
    }
}

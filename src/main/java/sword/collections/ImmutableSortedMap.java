package sword.collections;

import static sword.collections.SortUtils.equal;
import static sword.collections.SortUtils.findSuitableIndex;
import static sword.collections.SortUtils.findValue;

/**
 * Efficient implementation for map where keys are internally
 * sorted using a {@link SortFunction} given in construction time.
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
public final class ImmutableSortedMap<K, V> extends AbstractImmutableMap<K, V> {

    private final SortFunction<? super K> _sortFunction;

    ImmutableSortedMap(SortFunction<? super K> sortFunction, Object[] keys, Object[] values) {
        super(keys, values);
        _sortFunction = sortFunction;
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
    boolean entryLessThan(Entry<K, V> a, Entry<K, V> b) {
        return b != null && (a == null || _sortFunction.lessThan(a.key(), b.key()));
    }

    @Override
    public ImmutableSortedMap<K, V> toImmutable() {
        return this;
    }

    @Override
    public MutableMap<K, V> mutate(ArrayLengthFunction arrayLengthFunction) {
        final int size = _keys.length;
        final int length = arrayLengthFunction.suitableArrayLength(0, size);

        Object[] keys = new Object[length];
        Object[] values = new Object[length];

        System.arraycopy(_keys, 0, keys, 0, size);
        System.arraycopy(_values, 0, values, 0, size);

        return new MutableSortedMap<>(arrayLengthFunction, _sortFunction, keys, values, size);
    }

    @Override
    public MutableMap<K, V> mutate() {
        return mutate(GranularityBasedArrayLengthFunction.getInstance());
    }

    @Override
    public ImmutableSortedMap<K, V> put(K key, V value) {
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

                return new ImmutableSortedMap<>(_sortFunction, _keys, newValues);
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

            return new ImmutableSortedMap<>(_sortFunction, newKeys, newValues);
        }
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
    public ImmutableSortedMap<K, V> filter(Predicate<? super V> predicate) {
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
    public ImmutableSortedMap<K, V> filterNot(Predicate<? super V> predicate) {
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
    public ImmutableIntValueSortedMap<K> mapToInt(IntResultFunction<? super V> mapFunc) {
        final int itemCount = _keys.length;
        final int[] newValues = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableIntValueSortedMap<>(_sortFunction, _keys, newValues);
    }

    @Override
    public <U> ImmutableSortedMap<K, U> map(Function<? super V, ? extends U> mapFunc) {
        final int itemCount = _keys.length;
        final Object[] newValues = new Object[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableSortedMap<>(_sortFunction, _keys, newValues);
    }

    @Override
    public ImmutableSortedMap<K, V> removeAt(int index) {
        final int size = _values.length;
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        final Object[] newKeys = new Object[size - 1];
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

        return new ImmutableSortedMap<>(_sortFunction, newKeys, newValues);
    }

    public static class Builder<K, V> implements ImmutableMap.Builder<K, V> {
        private final MutableSortedMap<K, V> _map;

        public Builder(SortFunction<? super K> sortFunction) {
            _map = MutableSortedMap.empty(sortFunction);
        }

        public Builder(ArrayLengthFunction arrayLengthFunction, SortFunction<? super K> sortFunction) {
            _map = MutableSortedMap.empty(arrayLengthFunction, sortFunction);
        }

        @Override
        public Builder<K, V> put(K key, V value) {
            _map.put(key, value);
            return this;
        }

        @Override
        public ImmutableSortedMap<K, V> build() {
            return _map.toImmutable();
        }
    }
}

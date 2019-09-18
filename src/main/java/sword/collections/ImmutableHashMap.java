package sword.collections;

import static sword.collections.SortUtils.*;

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
    public MutableHashMap<K, V> mutate() {
        final int length = _keys.length;
        final int newLength = MutableHashMap.suitableArrayLength(length);

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
    public ImmutableSet<V> toSet() {
        return toList().toSet();
    }

    @Override
    public ImmutableIntSet indexes() {
        final int size = size();
        return (size == 0)? ImmutableIntArraySet.empty() : new ImmutableIntRange(0, size - 1);
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
    public ImmutableIntValueHashMap<K> mapToInt(IntResultFunction<V> mapFunc) {
        final int itemCount = _keys.length;
        final int[] newValues = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableIntValueHashMap<>(_keys, _hashCodes, newValues);
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

    public static class Builder<K, V> implements ImmutableMap.Builder<K, V> {
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
}

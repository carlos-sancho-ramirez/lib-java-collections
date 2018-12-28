package sword.collections;

import static sword.collections.SortUtils.findKey;

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
 * @param <T> Type for the key elements within the Map
 */
public final class ImmutableIntValueHashMap<T> extends AbstractImmutableIntValueMap<T> {

    private static final ImmutableIntValueHashMap<Object> EMPTY = new ImmutableIntValueHashMap<>(new Object[0], new int[0], new int[0]);

    @SuppressWarnings("unchecked")
    public static <U> ImmutableIntValueHashMap<U> empty() {
        return (ImmutableIntValueHashMap<U>) EMPTY;
    }

    private final int[] _hashCodes;

    ImmutableIntValueHashMap(Object[] keys, int[] hashCodes, int[] values) {
        super(keys, values);
        _hashCodes = hashCodes;
    }

    @Override
    public int indexOfKey(T key) {
        return findKey(_hashCodes, _keys, _keys.length, key);
    }

    @Override
    public ImmutableHashSet<T> keySet() {
        return new ImmutableHashSet<>(_keys, _hashCodes);
    }

    @Override
    boolean entryLessThan(Entry<T> a, Entry<T> b) {
        return b != null && (a == null || SortUtils.compareByHashCode(a.key(), b.key()));
    }

    @Override
    public ImmutableIntValueHashMap<T> toImmutable() {
        return this;
    }

    @Override
    public MutableIntValueHashMap<T> mutate() {
        final int newLength = MutableIntValueHashMap.suitableArrayLength(_keys.length);
        Object[] newKeys = new Object[newLength];
        int[] newHashCodes = new int[newLength];
        int[] newValues = new int[newLength];

        System.arraycopy(_keys, 0, newKeys, 0, _keys.length);
        System.arraycopy(_hashCodes, 0, newHashCodes, 0, _hashCodes.length);
        System.arraycopy(_values, 0, newValues, 0, _values.length);

        return new MutableIntValueHashMap<>(newKeys, newHashCodes, newValues, _keys.length);
    }

    @Override
    public ImmutableIntValueHashMap<T> filter(IntPredicate predicate) {
        final Builder<T> builder = new Builder<>();
        final int length = _keys.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            int value = _values[i];
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
    public ImmutableIntValueHashMap<T> filterNot(IntPredicate predicate) {
        final Builder<T> builder = new Builder<>();
        final int length = _keys.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            int value = _values[i];
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
    public ImmutableIntValueHashMap<T> map(IntToIntFunction mapFunc) {
        final int itemCount = _keys.length;
        final int[] newValues = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableIntValueHashMap<>(_keys, _hashCodes, newValues);
    }

    @Override
    public <U> ImmutableHashMap<T, U> map(IntFunction<U> mapFunc) {
        final int itemCount = _keys.length;
        final Object[] newValues = new Object[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableHashMap<>(_keys, _hashCodes, newValues);
    }

    public static class Builder<E> implements ImmutableIntValueMap.Builder<E> {
        final MutableIntValueHashMap<E> _map = MutableIntValueHashMap.empty();

        public Builder<E> put(E key, int value) {
            _map.put(key, value);
            return this;
        }

        public Builder<E> putAll(IntValueMap<E> map) {
            final int length = map.size();
            for (int i = 0; i < length; i++) {
                put(map.keyAt(i), map.valueAt(i));
            }

            return this;
        }

        public ImmutableIntValueHashMap<E> build() {
            return _map.toImmutable();
        }
    }
}

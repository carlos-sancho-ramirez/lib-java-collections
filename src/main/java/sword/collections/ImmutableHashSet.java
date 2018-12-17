package sword.collections;

import static sword.collections.SortUtils.findKey;

/**
 * Efficient implementation for an immutable Set when few elements are included.
 * 'Set' must be understood as a collection where its elements cannot be repeated.
 * 2 elements are considered to be the same, so they would be duplicated, if both
 * return the same hash code and calling equals returns true.
 *
 * This Set is immutable, that means that its content cannot be modified once
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
 * This class also implements the {@link java.lang.Iterable} interface, which
 * ensures that the for-each construction can be used.
 *
 * @param <T> Type for the elements within the Set
 */
public final class ImmutableHashSet<T> extends ImmutableSet<T> implements Set<T> {

    private static final ImmutableHashSet<Object> EMPTY = new ImmutableHashSet<>(new Object[0], new int[0]);

    @SuppressWarnings("unchecked")
    public static <E> ImmutableHashSet<E> empty() {
        return (ImmutableHashSet<E>) EMPTY;
    }

    private final int[] _hashCodes;

    ImmutableHashSet(Object[] keys, int[] hashCodes) {
        super(null, keys);
        _hashCodes = hashCodes;
    }

    @Override
    ImmutableIntSetBuilder newIntBuilder() {
        return new ImmutableIntSetBuilder();
    }

    @Override
    <U> Builder<U> newBuilder() {
        return new Builder<>();
    }

    @Override
    public int indexOf(T value) {
        return findKey(_hashCodes, _keys, _keys.length, value);
    }

    @Override
    public ImmutableHashSet<T> toImmutable() {
        return this;
    }

    @Override
    public MutableHashSet<T> mutate() {
        final int length = _keys.length;
        final int newLength = MutableHashSet.suitableArrayLength(length);

        Object[] keys = new Object[newLength];
        int[] hashCodes = new int[newLength];

        System.arraycopy(_keys, 0, keys, 0, length);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, length);

        return new MutableHashSet<>(keys, hashCodes, length);
    }

    @Override
    public ImmutableHashSet<T> add(T value) {
        if (contains(value)) {
            return this;
        }

        final int length = _keys.length;
        final int newHash = SortUtils.hashCode(value);
        final int index = SortUtils.findSuitableIndex(_hashCodes, length, newHash);

        final int[] newHashes = new int[length + 1];
        final Object[] newKeys = new Object[length + 1];
        if (index > 0) {
            System.arraycopy(_hashCodes, 0, newHashes, 0, index);
            System.arraycopy(_keys, 0, newKeys, 0, index);
        }
        newHashes[index] = newHash;
        newKeys[index] = value;
        if (index < length) {
            System.arraycopy(_hashCodes, index, newHashes, index + 1, length - index);
            System.arraycopy(_keys, index, newKeys, index + 1, length - index);
        }

        return new ImmutableHashSet<>(newKeys, newHashes);
    }

    /**
     * Composes a new map traversing this set, applying the given function to each item.
     *
     * This method will compose a new set for all items that the given function does
     * return an equivalent value. The resulting set will be the value within the new map,
     * and the returned value will be the key within the map for that set.
     *
     * Example:
     * Set(1,2,3,4,5) grouped by func (item % 2) will create Map(0 -&gt; Set(2,4), 1 -&gt; Set(1,3,5))
     *
     * @param function Function to be applied to each item within the set to determine its group.
     * @param <K> Type for the new key within the returned map.
     * @return A new map where items have been grouped into different set according with the function given.
     */
    public <K> ImmutableMap<K, ImmutableHashSet<T>> groupBy(Function<T, K> function) {
        MutableMap<K, ImmutableHashSet<T>> map = MutableMap.empty();
        final int length = size();
        for (int i = 0; i < length; i++) {
            final T value = valueAt(i);
            final K group = function.apply(value);
            final ImmutableHashSet<T> current = map.get(group, ImmutableHashSet.empty());
            map.put(group, current.add(value));
        }

        return (map.size() != 1)? map.toImmutable() :
                new ImmutableMap.Builder<K, ImmutableHashSet<T>>().put(map.keyAt(0), this).build();
    }

    /**
     * Composes a new map traversing this set, applying the given function to each item.
     *
     * This method will compose a new set for all items that the given function does
     * return the same integer value. The resulting set will be the value within the new map,
     * and the returned value will be the key within the map for that set.
     *
     * Example:
     * Set(1,2,3,4,5) grouped by func (item % 2) will create Map(0 -&gt; Set(2,4), 1 -&gt; Set(1,3,5))
     *
     * @param function Function to be applied to each item within the set to determine its group.
     * @return A new map where items have been grouped into different set according with the function given.
     */
    public ImmutableIntKeyMap<ImmutableHashSet<T>> groupByInt(IntResultFunction<T> function) {
        MutableIntKeyMap<ImmutableHashSet<T>> map = MutableIntKeyMap.empty();
        final int length = size();
        for (int i = 0; i < length; i++) {
            final T value = valueAt(i);
            final int group = function.apply(value);
            final ImmutableHashSet<T> current = map.get(group, ImmutableHashSet.empty());
            map.put(group, current.add(value));
        }

        return (map.size() != 1)? map.toImmutable() :
                new ImmutableIntKeyMap.Builder<ImmutableHashSet<T>>().put(map.keyAt(0), this).build();
    }

    static <E> ImmutableHashSet<E> fromMutableSet(MutableHashSet<E> set) {
        final int length = set.size();
        if (length == 0) {
            return empty();
        }

        final Object[] keys = new Object[length];
        final int[] hashCodes = new int[length];

        for (int i = 0; i < length; i++) {
            final E key = set.keyAt(i);
            keys[i] = key;
            hashCodes[i] = SortUtils.hashCode(key);
        }

        return new ImmutableHashSet<>(keys, hashCodes);
    }

    public static class Builder<E> extends ImmutableSet.Builder<E> {
        private final MutableHashSet<E> _set = MutableHashSet.empty();

        public Builder() {
            super(null);
        }

        @Override
        public Builder<E> add(E key) {
            _set.add(key);
            return this;
        }

        @Override
        public ImmutableHashSet<E> build() {
            return fromMutableSet(_set);
        }
    }

    @Override
    int itemHashCode(int index) {
        return _hashCodes[index];
    }
}

package sword.collections;

import static sword.collections.SortUtils.HASH_FOR_NULL;
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
public class ImmutableHashSet<T> extends ImmutableSet<T> implements Set<T> {

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
            hashCodes[i] = (key != null)? key.hashCode() : HASH_FOR_NULL;
        }

        return new ImmutableHashSet<>(keys, hashCodes);
    }

    public static class Builder<E> implements ImmutableCollectionBuilder<E> {
        private final MutableHashSet<E> _set = MutableHashSet.empty();

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
    public int hashCode() {
        final int length = _hashCodes.length;
        int hash = length;

        for (int i = 0; i < length; i++) {
            hash = hash * 31 + _hashCodes[i];
        }

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof ImmutableHashSet)) {
            return false;
        }
        else if (this == object) {
            return true;
        }

        final ImmutableHashSet that = (ImmutableHashSet) object;
        final int[] thatHashCodes = that._hashCodes;
        final int length = _hashCodes.length;
        if (length != thatHashCodes.length) {
            return false;
        }

        final ImmutableBitSetImpl.Builder builder = new ImmutableBitSetImpl.Builder();
        for (int i = 0; i < length; i++) {
            final int thisHash = _hashCodes[i];
            if (thisHash != thatHashCodes[i]) {
                return false;
            }

            if (i > 0 && _hashCodes[i - 1] == thisHash) {
                builder.add(i - 1);
                builder.add(i);
            }
        }
        final ImmutableBitSetImpl thisDuplicated = builder.build();
        ImmutableBitSetImpl thatDuplicated = thisDuplicated;

        final Object[] thatKeys = that._keys;
        for (int i = 0; i < length; i++) {
            final Object thisKey = _keys[i];
            if (thisDuplicated.contains(i)) {
                boolean found = false;
                for (int pos : thatDuplicated) {
                    if (thisKey == null && thatKeys[pos] == null || thisKey != null && thisKey.equals(thatKeys[pos])) {
                        thatDuplicated = thatDuplicated.remove(pos);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    return false;
                }
            }
            else {
                final Object thatKey = thatKeys[i];

                if (thisKey == null && thatKey != null || thisKey != null && !thisKey.equals(thatKey)) {
                    return false;
                }
            }
        }

        return thatDuplicated.isEmpty();
    }
}

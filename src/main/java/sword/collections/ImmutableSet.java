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
public class ImmutableSet<T> extends AbstractImmutableIterable<T> implements Set<T> {

    private static final ImmutableSet<Object> EMPTY = new ImmutableSet<>(new Object[0], new int[0]);

    @SuppressWarnings("unchecked")
    public static <E> ImmutableSet<E> empty() {
        return (ImmutableSet<E>) EMPTY;
    }

    private final Object[] _keys;
    private final int[] _hashCodes;

    ImmutableSet(Object[] keys, int[] hashCodes) {
        _keys = keys;
        _hashCodes = hashCodes;
    }

    @Override
    <U> Builder<U> newBuilder() {
        return new Builder<>();
    }

    @Override
    public boolean contains(T key) {
        return findKey(_hashCodes, _keys, _keys.length, key) >= 0;
    }

    @Override
    public int size() {
        return _keys.length;
    }

    @SuppressWarnings("unchecked")
    public T keyAt(int index) {
        return (T) _keys[index];
    }

    @Override
    public ImmutableSet<T> filter(Predicate<T> predicate) {
        return (ImmutableSet<T>) super.filter(predicate);
    }

    public ImmutableSet<T> filterNot(Predicate<T> predicate) {
        return (ImmutableSet<T>) super.filterNot(predicate);
    }

    public <E> ImmutableSet<E> map(Function<T, E> predicate) {
        return (ImmutableSet<E>) super.map(predicate);
    }

    @Override
    public ImmutableList<T> toList() {
        return new ImmutableList<>(_keys);
    }

    @Override
    public ImmutableSet<T> toImmutable() {
        return this;
    }

    @Override
    public MutableSet<T> mutate() {
        final int length = _keys.length;
        final int newLength = MutableSet.suitableArrayLength(length);

        Object[] keys = new Object[newLength];
        int[] hashCodes = new int[newLength];

        System.arraycopy(_keys, 0, keys, 0, length);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, length);

        return new MutableSet<>(keys, hashCodes, length);
    }

    private class Iterator extends IteratorForImmutable<T> {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _keys.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            return (T) _keys[_index++];
        }
    }

    @Override
    public Iterator iterator() {
        return new Iterator();
    }

    static <E> ImmutableSet<E> fromMutableSet(MutableSet<E> set) {
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

        return new ImmutableSet<>(keys, hashCodes);
    }

    public static class Builder<E> implements ImmutableCollectionBuilder<E> {
        private final MutableSet<E> _set = MutableSet.empty();

        @Override
        public Builder<E> add(E key) {
            _set.add(key);
            return this;
        }

        @Override
        public ImmutableSet<E> build() {
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
        if (object == null || !(object instanceof ImmutableSet)) {
            return false;
        }
        else if (this == object) {
            return true;
        }

        final ImmutableSet that = (ImmutableSet) object;
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

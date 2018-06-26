package sword.collections;

import static sword.collections.SortUtils.DEFAULT_GRANULARITY;
import static sword.collections.SortUtils.HASH_FOR_NULL;
import static sword.collections.SortUtils.findKey;

/**
 * Efficient implementation for a mutable Set when few elements are included.
 * 'Set' must be understood as a collection where its elements cannot be repeated.
 * 2 elements are considered to be the same, so they would be duplicated, if both
 * return the same hash code and calling equals returns true.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should get a new instance through the empty method.
 *
 * This implementation assumes that elements inserted are immutable, then its hashCode will not change.
 * It is not guaranteed to work if any of the elements is mutable.
 *
 * This class also implements the {@link Iterable} interface, which
 * ensures that the for-each construction can be used.
 *
 * @param <T> Type for the elements within the Set
 */
public class MutableHashSet<T> extends MutableSet<T> implements Set<T> {

    private static final int GRANULARITY = DEFAULT_GRANULARITY;

    public static <E> MutableHashSet<E> empty() {
        return new MutableHashSet<>(new Object[GRANULARITY], new int[GRANULARITY], 0);
    }

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    private int[] _hashCodes;

    MutableHashSet(Object[] keys, int[] hashCodes, int size) {
        super(null, keys, size);
        _hashCodes = hashCodes;
    }

    @Override
    public int indexOf(T value) {
        return findKey(_hashCodes, _keys, _size, value);
    }

    @Override
    public ImmutableHashSet<T> toImmutable() {
        Object[] keys = new Object[_size];
        int[] hashCodes = new int[_size];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);

        return new ImmutableHashSet<>(keys, hashCodes);
    }

    @Override
    public MutableHashSet<T> mutate() {
        Object[] keys = new Object[_keys.length];
        int[] hashCodes = new int[_hashCodes.length];

        System.arraycopy(_keys, 0, keys, 0, _size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);

        return new MutableHashSet<>(keys, hashCodes, _size);
    }

    private void enlargeArrays() {
        Object[] oldKeys = _keys;
        int[] oldHashCodes = _hashCodes;

        _keys = new Object[_size + GRANULARITY];
        _hashCodes = new int[_size + GRANULARITY];

        for (int i = 0; i < _size; i++) {
            _keys[i] = oldKeys[i];
            _hashCodes[i] = oldHashCodes[i];
        }
    }

    @Override
    int findSuitableIndex(T key) {
        return SortUtils.findSuitableIndex(_hashCodes, _size, (key != null)? key.hashCode() : HASH_FOR_NULL);
    }

    @Override
    void insertAt(int index, T value) {
        if (_size != 0 && _size % GRANULARITY == 0) {
            enlargeArrays();
        }

        for (int i = _size; i > index; i--) {
            _keys[i] = _keys[i - 1];
            _hashCodes[i] = _hashCodes[i - 1];
        }

        _keys[index] = value;
        _hashCodes[index] = (value != null)? value.hashCode() : HASH_FOR_NULL;
        _size++;
    }

    @Override
    void removeAt(int index) {
        if (_size != 1 && (_size % GRANULARITY) == 1) {
            Object[] oldKeys = _keys;
            int[] oldHashCodes = _hashCodes;

            _keys = new Object[--_size];
            _hashCodes = new int[_size];

            if (index > 0) {
                System.arraycopy(oldKeys, 0, _keys, 0, index);
                System.arraycopy(oldHashCodes, 0, _hashCodes, 0, index);
            }

            if (_size > index) {
                System.arraycopy(oldKeys, index + 1, _keys, index, _size - index);
                System.arraycopy(oldHashCodes, index + 1, _hashCodes, index, _size - index);
            }
        }
        else {
            --_size;
            for (int i = index; i < _size; i++) {
                _keys[i] = _keys[i + 1];
                _hashCodes[i] = _hashCodes[i + 1];
            }
        }
    }

    public static class Builder<E> extends MutableSet.Builder<E> {
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
        public MutableHashSet<E> build() {
            return _set;
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
        if (object == null || !(object instanceof MutableHashSet)) {
            return false;
        }
        else if (this == object) {
            return true;
        }

        final MutableHashSet that = (MutableHashSet) object;
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

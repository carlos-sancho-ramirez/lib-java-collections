package sword.collections;

import static sword.collections.SortUtils.DEFAULT_GRANULARITY;
import static sword.collections.SortUtils.HASH_FOR_NULL;
import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;

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
public class MutableHashSet<T> extends AbstractIterable<T> implements Set<T> {

    private static final int GRANULARITY = DEFAULT_GRANULARITY;

    public static <E> MutableHashSet<E> empty() {
        return new MutableHashSet<>();
    }

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    private Object[] _keys;
    private int[] _hashCodes;
    private int _size;

    private MutableHashSet() {
        _keys = new Object[GRANULARITY];
        _hashCodes = new int[GRANULARITY];
    }

    MutableHashSet(Object[] keys, int[] hashCodes, int size) {
        _keys = keys;
        _hashCodes = hashCodes;
        _size = size;
    }

    @Override
    public boolean contains(T key) {
        return findKey(_hashCodes, _keys, _size, key) >= 0;
    }

    @Override
    public int size() {
        return _size;
    }

    @SuppressWarnings("unchecked")
    public T keyAt(int index) {
        return (T) _keys[index];
    }

    private class Iterator implements java.util.Iterator<T> {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            return (T) _keys[_index++];
        }

        @Override
        public void remove() {
            removeAt(--_index);
        }
    }

    @Override
    public Iterator iterator() {
        return new Iterator();
    }

    @Override
    public List<T> toList() {
        final Object[] values = new Object[_size];
        for (int i = 0; i < _size; i++) {
            values[i] = _keys[i];
        }
        return new ImmutableList<>(values);
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

    public boolean add(T key) {
        int index = findKey(_hashCodes, _keys, _size, key);
        if (index < 0) {
            if (_size != 0 && _size % GRANULARITY == 0) {
                enlargeArrays();
            }

            final int hashCode = (key != null)? key.hashCode() : HASH_FOR_NULL;
            index = findSuitableIndex(_hashCodes, _size, hashCode);
            for (int i = _size; i > index; i--) {
                _keys[i] = _keys[i - 1];
                _hashCodes[i] = _hashCodes[i - 1];
            }

            _keys[index] = key;
            _hashCodes[index] = hashCode;

            _size++;
            return true;
        }

        return false;
    }

    private void removeAt(int index) {
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

    public boolean remove(T key) {
        int index = findKey(_hashCodes, _keys, _size, key);
        if (index >= 0) {
            removeAt(index);
            return true;
        }

        return false;
    }

    public static class Builder<E> implements CollectionBuilder<E> {
        private final MutableHashSet<E> _set = MutableHashSet.empty();

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

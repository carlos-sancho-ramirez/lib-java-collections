package sword.collections;

import static sword.collections.SortUtils.DEFAULT_GRANULARITY;
import static sword.collections.SortUtils.equal;
import static sword.collections.SortUtils.findValue;

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
public class MutableSet<T> extends AbstractIterable<T> implements Set<T> {

    private static final int GRANULARITY = DEFAULT_GRANULARITY;

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    private final SortFunction<T> _sortFunction;
    Object[] _keys;
    int _size;

    MutableSet(SortFunction<T> sortFunction, Object[] keys, int size) {
        _sortFunction = sortFunction;
        _keys = keys;
        _size = size;
    }

    @Override
    public int indexOf(T value) {
        return findValue(_sortFunction, _keys, _size, value);
    }

    @Override
    public boolean contains(T value) {
        return indexOf(value) >= 0;
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
    public ImmutableSet<T> toImmutable() {
        Object[] keys = new Object[_size];
        System.arraycopy(_keys, 0, keys, 0, _size);
        return new ImmutableSet<>(_sortFunction, keys);
    }

    @Override
    public MutableSet<T> mutate() {
        Object[] keys = new Object[_keys.length];
        System.arraycopy(_keys, 0, keys, 0, _size);
        return new MutableSet<>(_sortFunction, keys, _size);
    }

    private void enlargeArrays() {
        Object[] oldKeys = _keys;

        _keys = new Object[_size + GRANULARITY];

        for (int i = 0; i < _size; i++) {
            _keys[i] = oldKeys[i];
        }
    }

    int findSuitableIndex(T key) {
        return SortUtils.findSuitableIndex(_sortFunction, _keys, _size, key);
    }

    void insertAt(int index, T value) {
        if (_size != 0 && _size % GRANULARITY == 0) {
            enlargeArrays();
        }

        for (int i = _size; i > index; i--) {
            _keys[i] = _keys[i - 1];
        }

        _keys[index] = value;
        _size++;
    }

    public boolean add(T key) {
        int index = indexOf(key);
        if (index < 0) {
            insertAt(findSuitableIndex(key), key);
            return true;
        }

        return false;
    }

    void removeAt(int index) {
        if (_size != 1 && (_size % GRANULARITY) == 1) {
            Object[] oldKeys = _keys;
            _keys = new Object[--_size];

            if (index > 0) {
                System.arraycopy(oldKeys, 0, _keys, 0, index);
            }

            if (_size > index) {
                System.arraycopy(oldKeys, index + 1, _keys, index, _size - index);
            }
        }
        else {
            --_size;
            for (int i = index; i < _size; i++) {
                _keys[i] = _keys[i + 1];
            }
        }
    }

    public boolean remove(T key) {
        int index = indexOf(key);
        if (index >= 0) {
            removeAt(index);
            return true;
        }

        return false;
    }

    public static class Builder<E> implements CollectionBuilder<E> {
        private final MutableSet<E> _set;

        Builder(SortFunction<E> sortFunction) {
            _set = new MutableSet<>(sortFunction, new Object[suitableArrayLength(0)], 0);
        }

        @Override
        public Builder<E> add(E key) {
            _set.add(key);
            return this;
        }

        @Override
        public MutableSet<E> build() {
            return _set;
        }
    }

    @Override
    public int hashCode() {
        return _size;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof MutableSet)) {
            return false;
        }
        else if (this == object) {
            return true;
        }

        final MutableSet that = (MutableSet) object;
        if (_size != that._size) {
            return false;
        }

        for (int index = 0; index < _size; index++) {
            if (!equal(_keys[index], that._keys)) {
                return false;
            }
        }

        return true;
    }
}

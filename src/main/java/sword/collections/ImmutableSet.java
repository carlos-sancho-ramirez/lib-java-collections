package sword.collections;

import static sword.collections.SortUtils.HASH_FOR_NULL;
import static sword.collections.SortUtils.equal;
import static sword.collections.SortUtils.findValue;

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
 * This class also implements the {@link Iterable} interface, which
 * ensures that the for-each construction can be used.
 *
 * @param <T> Type for the elements within the Set
 */
public class ImmutableSet<T> extends AbstractImmutableIterable<T> implements Set<T> {

    private SortFunction<T> _sortFunction;
    final Object[] _keys;

    ImmutableSet(SortFunction<T> sortFunction, Object[] keys) {
        _sortFunction = sortFunction;
        _keys = keys;
    }

    @Override
    ImmutableIntSetBuilder newIntBuilder() {
        return new ImmutableIntSetBuilder();
    }

    @Override
    <U> ImmutableCollectionBuilder<U> newBuilder() {
        return new ImmutableHashSet.Builder<>();
    }

    @Override
    public int indexOf(T value) {
        return findValue(_sortFunction, _keys, _keys.length, value);
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

    public ImmutableIntSet map(IntResultFunction<T> func) {
        return (ImmutableIntSet) super.map(func);
    }

    public <E> ImmutableHashSet<E> map(Function<T, E> func) {
        return (ImmutableHashSet<E>) super.map(func);
    }

    /**
     * Creates a new set containing all the current elements and the ones given in the iterable.
     *
     * As this is a set, duplicated elements will not be allowed.
     * Than means that elements within the given iterable will be ignored if
     * there is an equivalent element already included in this set.
     *
     * @param iterable Collection from where new items will be added.
     */
    public ImmutableSet<T> addAll(Iterable<T> iterable) {
        final MutableSet<T> result = mutate();
        for (T item : iterable) {
            result.add(item);
        }

        return (result.size() == _keys.length)? this : result.toImmutable();
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
        final int newLength = MutableHashSet.suitableArrayLength(length);

        Object[] keys = new Object[newLength];
        System.arraycopy(_keys, 0, keys, 0, length);
        return new MutableSet<>(_sortFunction, keys, length);
    }

    @Override
    public ImmutableSet<T> sort(SortFunction<T> function) {
        if (equal(_sortFunction, function)) {
            return this;
        }

        final Builder<T> builder = new Builder<>(function);
        for (T value : this) {
            builder.add(value);
        }

        return builder.build();
    }

    class Iterator extends IteratorForImmutable<T> {

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

    public static class Builder<E> implements ImmutableCollectionBuilder<E> {
        private final MutableSet<E> _set;

        Builder(SortFunction<E> sortFunction) {
            _set = new MutableSet<>(sortFunction, new Object[MutableSet.suitableArrayLength(0)], 0);
        }

        @Override
        public Builder<E> add(E key) {
            _set.add(key);
            return this;
        }

        @Override
        public ImmutableSet<E> build() {
            return _set.toImmutable();
        }
    }

    int itemHashCode(int index) {
        return (_keys[index] != null)? _keys[index].hashCode() : HASH_FOR_NULL;
    }

    @Override
    public int hashCode() {
        final int length = _keys.length;
        int hash = length * 11069;

        for (int i = 0; i < length; i++) {
            hash ^= itemHashCode(i);
        }

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof Set)) {
            return false;
        }
        else if (this == object) {
            return true;
        }

        final Set that = (Set) object;
        if (_keys.length != that.size()) {
            return false;
        }

        for (int index = 0; index < _keys.length; index++) {
            if (!that.contains(_keys[index])) {
                return false;
            }
        }

        return true;
    }
}

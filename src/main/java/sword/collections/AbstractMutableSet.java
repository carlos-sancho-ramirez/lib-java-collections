package sword.collections;

import static sword.collections.SortUtils.DEFAULT_GRANULARITY;

abstract class AbstractMutableSet<T> extends AbstractTraversable<T> implements MutableSet<T> {

    static final int GRANULARITY = DEFAULT_GRANULARITY;

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    Object[] _keys;
    int _size;

    AbstractMutableSet(Object[] keys, int size) {
        _keys = keys;
        _size = size;
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

    private class Iterator extends AbstractTransformer<T> {

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
    public Transformer<T> iterator() {
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
    public Set<T> sort(SortFunction<T> function) {
        final ImmutableSortedSet.Builder<T> builder = new ImmutableSortedSet.Builder<>(function);
        for (T value : this) {
            builder.add(value);
        }

        return builder.build();
    }

    @Override
    public <E> List<E> map(Function<T, E> func) {
        return new MapResultList<>(this, func);
    }

    abstract int findSuitableIndex(T key);
    abstract void insertAt(int index, T value);

    public boolean add(T key) {
        int index = indexOf(key);
        if (index < 0) {
            insertAt(findSuitableIndex(key), key);
            return true;
        }

        return false;
    }

    public boolean remove(T key) {
        int index = indexOf(key);
        if (index >= 0) {
            removeAt(index);
            return true;
        }

        return false;
    }

    int itemHashCode(int index) {
        return SortUtils.hashCode(_keys[index]);
    }

    @Override
    public int hashCode() {
        final int length = _size;
        int hash = length * 11069;

        for (int i = 0; i < length; i++) {
            hash ^= itemHashCode(i);
        }

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Set)) {
            return false;
        }
        else if (this == object) {
            return true;
        }

        final Set that = (Set) object;
        if (_size != that.size()) {
            return false;
        }

        for (int index = 0; index < _size; index++) {
            if (!that.contains(_keys[index])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equalsInItems(Set that) {
        if (_size != that.size()) {
            return false;
        }

        for (int index = 0; index < _size; index++) {
            if (!that.contains(_keys[index])) {
                return false;
            }
        }

        return true;
    }
}

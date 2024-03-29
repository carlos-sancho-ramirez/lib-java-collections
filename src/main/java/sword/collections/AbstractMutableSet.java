package sword.collections;

abstract class AbstractMutableSet<T> extends AbstractTraversable<T> implements MutableSet<T> {

    ArrayLengthFunction _arrayLengthFunction;
    Object[] _values;
    int _size;

    AbstractMutableSet(ArrayLengthFunction arrayLengthFunction, Object[] keys, int size) {
        _arrayLengthFunction = arrayLengthFunction;
        _values = keys;
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

    @Override
    @SuppressWarnings("unchecked")
    public T valueAt(int index) {
        if (index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        return (T) _values[index];
    }

    @Override
    public T last() throws EmptyCollectionException {
        if (_size == 0) {
            throw new EmptyCollectionException();
        }

        return valueAt(_size - 1);
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
            return (T) _values[_index++];
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
            values[i] = this._values[i];
        }
        return new ImmutableList<>(values);
    }

    @Override
    public Set<T> sort(SortFunction<? super T> function) {
        final ImmutableSortedSet.Builder<T> builder = new ImmutableSortedSet.Builder<>(function);
        for (T value : this) {
            builder.add(value);
        }

        return builder.build();
    }

    @Override
    public <E> List<E> map(Function<? super T, ? extends E> func) {
        return new MapResultList<>(this, func);
    }

    @Override
    public IntValueMap<T> assignToInt(IntResultFunction<? super T> function) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    abstract int findSuitableIndex(T key);
    abstract void insertAt(int index, T value);

    @Override
    public boolean add(T key) {
        int index = indexOf(key);
        if (index < 0) {
            insertAt(findSuitableIndex(key), key);
            return true;
        }

        return false;
    }

    @Override
    public boolean remove(T key) {
        int index = indexOf(key);
        if (index >= 0) {
            removeAt(index);
            return true;
        }

        return false;
    }

    int itemHashCode(int index) {
        return SortUtils.hashCode(_values[index]);
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
    public boolean equalSet(Set that) {
        if (_size != that.size()) {
            return false;
        }

        for (int index = 0; index < _size; index++) {
            if (!that.contains(_values[index])) {
                return false;
            }
        }

        return true;
    }
}

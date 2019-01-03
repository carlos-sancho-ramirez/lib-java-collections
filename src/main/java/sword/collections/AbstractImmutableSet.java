package sword.collections;

abstract class AbstractImmutableSet<T> extends AbstractImmutableIterable<T> implements ImmutableSet<T> {

    final Object[] _keys;

    AbstractImmutableSet(Object[] keys) {
        _keys = keys;
    }

    @Override
    public int size() {
        return _keys.length;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T keyAt(int index) {
        return (T) _keys[index];
    }

    @Override
    public AbstractImmutableSet<T> filter(Predicate<T> predicate) {
        return (AbstractImmutableSet<T>) super.filter(predicate);
    }

    @Override
    public AbstractImmutableSet<T> filterNot(Predicate<T> predicate) {
        return (AbstractImmutableSet<T>) super.filterNot(predicate);
    }

    @Override
    public ImmutableIntList mapToInt(IntResultFunction<T> func) {
        final int length = _keys.length;
        final int[] newValues = new int[length];
        for (int i = 0; i < length; i++) {
            newValues[i] = func.apply(keyAt(i));
        }

        return new ImmutableIntList(newValues);
    }

    @Override
    public <E> ImmutableList<E> map(Function<T, E> func) {
        final int length = _keys.length;
        final Object[] newValues = new Object[length];
        for (int i = 0; i < length; i++) {
            newValues[i] = func.apply(keyAt(i));
        }

        return new ImmutableList<>(newValues);
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
    public ImmutableSortedSet<T> sort(SortFunction<T> function) {
        final ImmutableSortedSet.Builder<T> builder = new ImmutableSortedSet.Builder<>(function);
        for (T value : this) {
            builder.add(value);
        }

        return builder.build();
    }

    class Iterator extends AbstractTransformer<T> {

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

    int itemHashCode(int index) {
        return SortUtils.hashCode(_keys[index]);
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

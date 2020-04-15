package sword.collections;

abstract class AbstractImmutableSet<T> extends AbstractImmutableTransformable<T> implements ImmutableSet<T> {

    final Object[] _values;

    AbstractImmutableSet(Object[] keys) {
        _values = keys;
    }

    @Override
    public int size() {
        return _values.length;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T valueAt(int index) {
        return (T) _values[index];
    }

    @Override
    public AbstractImmutableSet<T> filter(Predicate<? super T> predicate) {
        return (AbstractImmutableSet<T>) super.filter(predicate);
    }

    @Override
    public AbstractImmutableSet<T> filterNot(Predicate<? super T> predicate) {
        return (AbstractImmutableSet<T>) super.filterNot(predicate);
    }

    @Override
    public ImmutableIntList mapToInt(IntResultFunction<? super T> func) {
        final int length = _values.length;
        final int[] newValues = new int[length];
        for (int i = 0; i < length; i++) {
            newValues[i] = func.apply(valueAt(i));
        }

        return new ImmutableIntList(newValues);
    }

    @Override
    public <E> ImmutableList<E> map(Function<? super T, ? extends E> func) {
        final int length = _values.length;
        final Object[] newValues = new Object[length];
        for (int i = 0; i < length; i++) {
            newValues[i] = func.apply(valueAt(i));
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

        return (result.size() == _values.length)? this : result.toImmutable();
    }

    @Override
    public ImmutableList<T> toList() {
        return new ImmutableList<>(_values);
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
            return _index < _values.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            return (T) _values[_index++];
        }
    }

    @Override
    public Iterator iterator() {
        return new Iterator();
    }

    int itemHashCode(int index) {
        return SortUtils.hashCode(_values[index]);
    }

    @Override
    public int hashCode() {
        final int length = _values.length;
        int hash = length * 11069;

        for (int i = 0; i < length; i++) {
            hash ^= itemHashCode(i);
        }

        return hash;
    }

    @Override
    public boolean equalSet(Set that) {
        if (_values.length != that.size()) {
            return false;
        }

        for (int index = 0; index < _values.length; index++) {
            if (!that.contains(_values[index])) {
                return false;
            }
        }

        return true;
    }
}

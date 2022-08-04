package sword.collections;

/**
 * Root for both variants of List, immutable and mutable.
 * @param <T> Type of items within the collection.
 */
public interface List<T> extends Transformable<T> {

    @Override
    default List<T> filter(Predicate<? super T> predicate) {
        return iterator().filter(predicate).toList();
    }

    @Override
    default List<T> filterNot(Predicate<? super T> predicate) {
        return iterator().filterNot(predicate).toList();
    }

    /**
     * Applies the given function to each element on the collection and composes a
     * new {@link List} of the same size with the results of each
     * function execution in the same index.
     *
     * @param func Function to be applied to each element in the list
     * @param <E> New type for the elements in the new created collection.
     */
    @Override
    default <E> List<E> map(Function<? super T, ? extends E> func) {
        return iterator().<E>map(func).toList();
    }

    /**
     * Applies the given function to each element on the collection and composes a
     * new {@link IntList} of the same size with the results of each
     * function execution in the same index.
     *
     * @param func Function to be applied to each element in this collection.
     */
    @Override
    default IntList mapToInt(IntResultFunction<? super T> func) {
        return iterator().mapToInt(func).toList();
    }

    /**
     * Return the item in the given position
     * @param index Position within the list, starting from 0.
     * @return the item in the given position.
     * @throws java.lang.IndexOutOfBoundsException if index is negative or index is not lower than the size of this collection.
     */
    default T get(int index) {
        return valueAt(index);
    }

    /**
     * Composes a new collection where the elements are extracted from this one
     * according to the positions given in the range.
     * <p>
     * The size of the resulting collection should be at most the size of the given
     * range. It can be less if the actual collection does not have enough elements.
     *
     * @param range Positions to be extracted from the original collection.
     *              Negative numbers are not expected.
     * @return A new collection where the elements are extracted from this collection.
     * @throws IllegalArgumentException in case the range is invalid.
     */
    default List<T> slice(ImmutableIntRange range) {
        final int size = size();
        final int min = range.min();
        final int max = range.max();
        if (min >= size || max < 0) {
            return ImmutableList.empty();
        }

        if (range.min() <= 0 && range.max() >= size - 1) {
            return this;
        }

        final ImmutableList.Builder<T> builder = new ImmutableList.Builder<>();
        for (int position = min; position <= max && position < size; position++) {
            builder.append(valueAt(position));
        }

        return builder.build();
    }

    /**
     * Return an immutable list from the values contained in this collection.
     * The same instance will be returned in case of being already immutable.
     */
    ImmutableList<T> toImmutable();

    /**
     * Return a new mutable list.
     * This method will always generate a new instance in order to avoid affecting the state of its original collection.
     */
    MutableList<T> mutate();

    /**
     * Return a new mutable list with the given {@link ArrayLengthFunction}.
     * This method will always generate a new instance in order to avoid affecting the state of its original collection.
     */
    MutableList<T> mutate(ArrayLengthFunction arrayLengthFunction);

    default List<T> sort(SortFunction<? super T> function) {
        return new SortedList<>(this, function);
    }
}

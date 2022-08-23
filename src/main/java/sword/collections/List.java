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

    @Override
    default List<T> slice(ImmutableIntRange range) {
        final int size = size();
        final int min = range.min();
        final int max = range.max();
        if (min <= 0 && max >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableList.empty();
        }

        final ImmutableList.Builder<T> builder = new ImmutableList.Builder<>();
        for (int position = min; position <= max && position < size; position++) {
            builder.append(valueAt(position));
        }

        return builder.build();
    }

    /**
     * Returns a new List of the same type where the
     * <code>length</code> amount of first elements has been removed.
     * <p>
     * This will return an empty list if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the start of the list.
     * @return A new List instance without the first elements.
     */
    @Override
    default List<T> skip(int length) {
        return slice(new ImmutableIntRange(length, Integer.MAX_VALUE));
    }

    /**
     * Returns a new List where only the <code>length</code> amount of
     * first elements are included, and the rest are discarded if any.
     *
     * @param length the maximum number of elements to be included from the
     *               start of the list.
     * @return A new List instance just including the first elements,
     *         or the empty instance in case the given length is 0.
     */
    @Override
    default List<T> take(int length) {
        return (length == 0)? ImmutableList.empty() : slice(new ImmutableIntRange(0, length - 1));
    }

    /**
     * Returns a new List where the <code>length</code> amount of last elements
     * has been removed.
     * <p>
     * This will return an empty list if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the end of the list.
     * @return A new List instance without the last elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance of the given length is equal or greater
     *         than the actual length of the list.
     */
    default List<T> skipLast(int length) {
        final int size = size();
        if (size == 0) {
            return this;
        }

        final int max = size - length - 1;
        return (max < 0)? ImmutableList.empty() : slice(new ImmutableIntRange(0, max));
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

package sword.collections;

public interface IterableCollection<T> extends Iterable<T> {

    Traverser<T> iterator();

    /**
     * Return true if an equivalent item is found in the collection, this means
     * that it will be true if calling {@link Object#equals(Object)} with
     * this value returns true on any of the elements.
     *
     * @param value Value to check
     */
    boolean contains(T value);

    /**
     * Returns true if the given predicate returns true for any of the items
     * in this collection.
     *
     * @param predicate Predicate to be evaluated.
     */
    boolean anyMatch(Predicate<T> predicate);

    /**
     * Returns the index within the collection for the first element matching the given value.
     * Or -1 if none matches.
     * @param value Value to be matched. {@link java.lang.Object#equals(Object)} will be called for this purpose.
     */
    int indexOf(T value);

    /**
     * Return the value in the given index position.
     *
     * @param index Index within the array of values.
     *              It must be zero or a positive number that must be below the number of elements
     *              returned when this collection is iterated.
     * @return The value in the given position.
     * @throws IndexOutOfBoundsException if the given index is invalid for this collection.
     */
    T valueAt(int index);

    /**
     * Returns the first item matching the predicate or the default value if none matches.
     */
    T findFirst(Predicate<T> predicate, T defaultValue);

    /**
     * Reduces the collection to a single element by applying the given function on each pair of values.
     * @param func Associate function to be applied on each pair of elements.
     * @return The resulting value of applying the given function to each value pair.
     * @throws EmptyCollectionException in case the collection is empty.
     */
    T reduce(ReduceFunction<T> func);

    /**
     * Reduces the collection to a single element by applying the given function on each pair of values,
     * or return the default value if the collection is empty.
     * @param func Associate function to be applied on each pair of elements.
     * @return The resulting value of applying the given function to each value pair, or the default value if empty.
     */
    T reduce(ReduceFunction<T> func, T defaultValue);
}

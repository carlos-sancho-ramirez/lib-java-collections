package sword.collections;

public interface Traversable<T> extends Iterable<T>, Sizable {

    Traverser<T> iterator();

    @Override
    default int size() {
        return iterator().size();
    }

    /**
     * Return true if an equivalent item is found in the collection, this means
     * that it will be true if calling {@link Object#equals(Object)} with
     * this value returns true on any of the elements.
     *
     * @param value Value to check
     */
    default boolean contains(T value) {
        return iterator().contains(value);
    }

    /**
     * Returns true if the given predicate returns true for any of the items
     * in this collection.
     *
     * @param predicate Predicate to be evaluated.
     */
    default boolean anyMatch(Predicate<? super T> predicate) {
        return iterator().anyMatch(predicate);
    }

    /**
     * Returns the index within the collection for the first element matching the given value.
     * Or -1 if none matches.
     * @param value Value to be matched. {@link java.lang.Object#equals(Object)} will be called for this purpose.
     */
    default int indexOf(T value) {
        return iterator().indexOf(value);
    }

    /**
     * Returns the index within this collection for the first element matching the given predicate. Or -1 if none matches.
     * @param predicate Condition to be satisfied for the element that we are looking for.
     * @return The index of the first element matching the predicate, or -1 if none matches.
     */
    default int indexWhere(Predicate<? super T> predicate) {
        return iterator().indexWhere(predicate);
    }

    /**
     * Return the value in the given index position.
     *
     * @param index Index within the array of values.
     *              It must be zero or a positive number that must be below the number of elements
     *              returned when this collection is iterated.
     * @return The value in the given position.
     * @throws IndexOutOfBoundsException if the given index is invalid for this collection.
     */
    default T valueAt(int index) {
        return iterator().valueAt(index);
    }

    /**
     * Returns the first item matching the predicate or the default value if none matches.
     */
    default T findFirst(Predicate<? super T> predicate, T defaultValue) {
        return iterator().findFirst(predicate, defaultValue);
    }

    /**
     * Reduces the collection to a single element by applying the given function on each pair of values.
     * @param func Associate function to be applied on each pair of elements.
     * @return The resulting value of applying the given function to each value pair.
     * @throws EmptyCollectionException in case the collection is empty.
     */
    default T reduce(ReduceFunction<T> func) {
        return iterator().reduce(func);
    }

    /**
     * Reduces the collection to a single element by applying the given function on each pair of values,
     * or return the default value if the collection is empty.
     * @param func Associate function to be applied on each pair of elements.
     * @return The resulting value of applying the given function to each value pair, or the default value if empty.
     */
    default T reduce(ReduceFunction<T> func, T defaultValue) {
        return iterator().reduce(func, defaultValue);
    }

    /**
     * Whether both collections have equivalent values in the same order.
     * @param traversable Traversable to contrast.
     * @return Whether both collections have equivalent values in the same order.
     */
    default boolean equalTraversable(Traversable traversable) {
        return traversable != null && iterator().equalTraverser(traversable.iterator());
    }
}

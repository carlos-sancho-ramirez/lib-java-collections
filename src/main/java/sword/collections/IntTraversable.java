package sword.collections;

public interface IntTraversable extends Iterable<Integer>, Sizable {

    @Override
    IntTraverser iterator();

    /**
     * Return true if the given value is found in the collection.
     * @param value Value to check
     */
    default boolean contains(int value) {
        return iterator().contains(value);
    }

    /**
     * Returns true if the given predicate returns true for any of the items
     * in this collection.
     *
     * @param predicate Predicate to be evaluated.
     */
    default boolean anyMatch(IntPredicate predicate) {
        return iterator().anyMatch(predicate);
    }

    /**
     * Returns the index within the collection for the first element matching the given value.
     * Or -1 if none matches.
     * @param value Value to be matched.
     */
    default int indexOf(int value) {
        return iterator().indexOf(value);
    }

    /**
     * Returns the index within this collection for the first element matching the given predicate. Or -1 if none matches.
     * @param predicate Condition to be satisfied for the element that we are looking for.
     * @return The index of the first element matching the predicate, or -1 if none matches.
     */
    default int indexWhere(IntPredicate predicate) {
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
    default int valueAt(int index) {
        return iterator().valueAt(index);
    }

    /**
     * Returns the first item matching the predicate or the default value if none matches.
     */
    default int findFirst(IntPredicate predicate, int defaultValue) {
        return iterator().findFirst(predicate, defaultValue);
    }

    /**
     * Reduces the collection to a single element by applying the given function on each pair of values.
     * @param func Associate function to be applied on each pair of elements.
     * @return The resulting value of applying the given function to each value pair.
     * @throws EmptyCollectionException in case the collection is empty.
     */
    default int reduce(IntReduceFunction func) {
        return iterator().reduce(func);
    }

    /**
     * Reduces the collection to a single element by applying the given function on each pair of values,
     * or return the defaultValue if the collection is empty.
     * @param func Associate function to be applied on each pair of elements.
     * @return The resulting value of applying the given function to each value pair, or the defaultValue if empty.
     */
    default int reduce(IntReduceFunction func, int defaultValue) {
        return iterator().reduce(func, defaultValue);
    }

    /**
     * Returns the minimum value within the collection.
     * @throws EmptyCollectionException if the collection is empty.
     */
    default int min() throws EmptyCollectionException {
        return iterator().min();
    }

    /**
     * Returns the maximum value within the collection.
     * @throws EmptyCollectionException if the collection is empty.
     */
    default int max() throws EmptyCollectionException {
        return iterator().max();
    }

    /**
     * Returns the sum of all values within this collection.
     */
    default int sum() {
        return iterator().sum();
    }

    @Override
    default int size() {
        int count = 0;
        final IntTraverser it = iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }

        return count;
    }
}

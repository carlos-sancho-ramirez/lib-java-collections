package sword.collections;

interface IterableIntCollection extends Iterable<Integer> {

    /**
     * Return true if the given value is found in the collection.
     * @param value Value to check
     */
    boolean contains(int value);

    /**
     * Returns true if the given predicate returns true for any of the items
     * in this collection.
     *
     * @param predicate Predicate to be evaluated.
     */
    boolean anyMatch(IntPredicate predicate);

    /**
     * Return the value in the given index position.
     *
     * @param index Index within the array of values.
     *              It must be zero or a positive number that must be below the number of elements
     *              returned when this collection is iterated.
     * @return The value in the given position.
     * @throws IndexOutOfBoundsException if the given index is invalid for this collection.
     */
    int valueAt(int index);
}

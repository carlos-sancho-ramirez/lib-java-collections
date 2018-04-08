package sword.collections;

public interface IterableCollection<T> extends Iterable<T> {

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
}

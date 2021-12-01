package sword.collections;

public interface MutableTraversable<T> extends Traversable<T> {

    /**
     * Removes from this collection the item in the given position.
     *
     * If no exception is thrown because of a wrong index, this method will always
     * shrink the size of this collection by 1.
     * @param index Index within this collection for the element to be removed.
     *              It must be zero or a positive number that must be below the number of elements
     *              returned when this collection is iterated.
     * @throws IndexOutOfBoundsException if the given index is invalid for this collection.
     */
    void removeAt(int index) throws IndexOutOfBoundsException;

    /**
     * Remove all the content of this collection.
     * @return Whether the collection has changed, false if it was already empty.
     */
    boolean clear();

    /**
     * Remove the last element in this traversable and returns it.
     * @return The last element in this traversable.
     * @throws EmptyCollectionException in case this traversable is empty.
     */
    default T pickLast() throws EmptyCollectionException {
        final int size = size();
        if (size == 0) {
            throw new EmptyCollectionException();
        }

        final T result = valueAt(size - 1);
        removeAt(size - 1);
        return result;
    }
}

package sword.collections;

public interface MutableIterableIntCollection extends IterableIntCollection {

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
}

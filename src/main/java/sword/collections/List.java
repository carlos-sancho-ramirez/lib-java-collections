package sword.collections;

/**
 * Root for both variants of List, immutable and mutable.
 * @param <T> Type of items within the collection.
 */
public interface List<T> extends IterableCollection<T>, Sizable {

    /**
     * Return the item in the given position
     * @param index Position within the list, starting from 0.
     * @return the item in the given position.
     * @throws java.lang.IndexOutOfBoundsException if index is negative or index is not lower than the size of this collection.
     */
    T get(int index);

    /**
     * Returns the index within the list for the first element matching the given value. Or -1 if none matches.
     * @param value Value to be matched. {@link java.lang.Object#equals(Object)} will be called for this purpose.
     */
    int indexOf(T value);

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
}

package sword.collections;

/**
 * Root for both variants of List, immutable and mutable.
 * @param <T> Type of items within the collection.
 */
public interface List<T> extends Transformable<T>, Sizable {

    /**
     * Return the item in the given position
     * @param index Position within the list, starting from 0.
     * @return the item in the given position.
     * @throws java.lang.IndexOutOfBoundsException if index is negative or index is not lower than the size of this collection.
     */
    T get(int index);

    /**
     * Converts this list into a set.
     *
     * All duplicated elements within the list will be removed as sets does not allow duplicating values.
     * Because of that, the amount of elements in the resulting set may be less
     * than the amount of elements in the original list, but never more.
     *
     * The iteration order of elements in the resulting set is not guaranteed
     * to be the same that was in the list even if no elements are removed for duplication.
     */
    Set<T> toSet();

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

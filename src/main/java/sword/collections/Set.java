package sword.collections;

/**
 * Root for both variants of Sets, immutable and mutable.
 * @param <T> Type of items within the set.
 */
public interface Set<T> extends IterableCollection<T>, Sizable {

    /**
     * Converts this set to a list.
     *
     * The order of iteration is guaranteed to be the same in the new list.
     */
    List<T> toList();

    /**
     * Return an immutable set from the values contained in this set.
     * The same instance will be returned in case of being already immutable.
     */
    ImmutableSet<T> toImmutable();

    /**
     * Return a new mutable set.
     * This method will always generate a new instance in order to avoid affecting the state of its original set.
     */
    MutableSet<T> mutate();

    /**
     * Creates a new set where all current elements and future elements will be
     * sorted following the given function.
     *
     * @param function Function the sort the elements within this collection.
     * @return A new set where all current elements and future newly added will
     * be sorted following the the given function.
     */
    Set<T> sort(SortFunction<T> function);

    /**
     * Returns a hash code calculated from the hashcode of any of the elements.
     *
     * The resulting hashcode is guaranteed to be the same independently of the
     * order of the elements within this set, and its mutability state.
     */
    @Override
    int hashCode();

    /**
     * Check if 2 set instances contain equivalent elements.
     *
     * This method will call {@link Object#equals(Object)} on the elements
     * within the sets in order to check if they are equivalent.
     * Depending on the set implementation, {@link Object#hashCode()} may
     * also be queried.
     *
     * Note that this method will return true even if the elements within the 2
     * sets are not sorted in the same way.
     *
     * @param other set to be compared with this instance.
     * @return whether the given set contains equivalent values to this one.
     */
    @Override
    boolean equals(Object other);
}

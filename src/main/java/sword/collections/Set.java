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
}

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
}

package sword.collections;

public interface Transformable<T> extends Traversable<T> {

    Transformer<T> iterator();

    /**
     * Converts this collection to a list.
     *
     * The order of iteration is guaranteed to be the same in the new list.
     */
    default List<T> toList() {
        return iterator().toList();
    }

    /**
     * Converts this collection into a set.
     *
     * All duplicated elements within the collection will be removed as sets does not allow duplicating values.
     * Because of that, the amount of elements in the resulting set may be less
     * than the amount of elements in the original collection, but never more.
     *
     * The iteration order of elements in the resulting set is not guaranteed
     * to be the same that was in the collection even if no elements are removed for duplication.
     */
    default Set<T> toSet() {
        return iterator().toSet();
    }

    /**
     * Return all indexes within this collection in ascending order.
     */
    default IntSet indexes() {
        return iterator().indexes().toSet();
    }

    /**
     * Composes a new Transformable that filters this one by applying the given predicate to the
     * elements within this collection.
     * @param predicate Only value returning true for the given predicate will be present
     *                  in the resulting Transformable.
     */
    Transformable<T> filter(Predicate<T> predicate);

    /**
     * Composes a new Transformer that filters this one by applying the given predicate to the
     * elements within this collection and collecting all elements where the predicate does not hold.
     * @param predicate Only value returning false for the given predicate will be present
     *                  in the resulting Transformable.
     */
    Transformable<T> filterNot(Predicate<T> predicate);

    default IntTransformable mapToInt(IntResultFunction<T> func) {
        return iterator().mapToInt(func).toList();
    }

    default <E> Transformable<E> map(Function<T, E> func) {
        return iterator().map(func).toList();
    }
}

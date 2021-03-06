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
    Transformable<T> filter(Predicate<? super T> predicate);

    /**
     * Composes a new Transformer that filters this one by applying the given predicate to the
     * elements within this collection and collecting all elements where the predicate does not hold.
     * @param predicate Only value returning false for the given predicate will be present
     *                  in the resulting Transformable.
     */
    Transformable<T> filterNot(Predicate<? super T> predicate);

    /**
     * Applies the given function to each element on the collection and composes a
     * new {@link IntTransformable} of the same size with the results of each
     * function execution in the same index.
     *
     * @param func Function to be applied to each element in this collection.
     */
    default IntTransformable mapToInt(IntResultFunction<? super T> func) {
        return iterator().mapToInt(func).toList();
    }

    /**
     * Applies the given function to each element on the collection and composes a
     * new {@link Transformable} of the same size with the results of each
     * function execution in the same index.
     *
     * @param func Function to be applied to each element in this collection.
     * @param <E> New type for the elements in the new created collection.
     */
    default <E> Transformable<E> map(Function<? super T, ? extends E> func) {
        return iterator().<E>map(func).toList();
    }

    /**
     * Composes a map by traversing the collection and counting how many times an element is found.
     *
     * The resulting map will have the elements from the collection as keys,
     * and the number of times that each element is found as its value.
     *
     * The {@link Object#equals(Object)} method will be used to compare among the elements.
     *
     * It is expected that the size of the resulting map will never be longer that the original one,
     * and the sum of all resulting values should match the original size. Note that the values within
     * the resulting map will never be negative nor 0.
     */
    default IntValueMap<T> count() {
        return iterator().count();
    }
}

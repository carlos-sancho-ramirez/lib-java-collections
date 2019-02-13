package sword.collections;

public interface Transformable<T> extends Traversable<T> {

    Transformer<T> iterator();

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

    default IntTraversable mapToInt(IntResultFunction<T> func) {
        return iterator().mapToInt(func).toList();
    }

    default <E> Traversable<E> map(Function<T, E> func) {
        return iterator().map(func).toList();
    }
}

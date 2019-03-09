package sword.collections;

public interface IntTransformable extends IntTraversable {
    IntTransformer iterator();

    /**
     * Converts collection to a list.
     *
     * The order of iteration is guaranteed to be the same in the new list.
     */
    IntList toList();

    /**
     * Return all indexes within this collection in ascending order.
     */
    default IntSet indexes() {
        return iterator().indexes().toSet();
    }

    /**
     * Applies the given predicate to each of the values within the set and
     * composes a new set including only the values whose predicate returned
     * true.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    IntTransformable filter(IntPredicate predicate);

    /**
     * Applies the given predicate to each of the values within the set and
     * composes a new set including only the values whose predicate returned
     * false.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    IntTransformable filterNot(IntPredicate predicate);

    default IntTransformable mapToInt(IntToIntFunction func) {
        return iterator().mapToInt(func).toList();
    }

    default <U> Transformable<U> map(IntFunction<U> func) {
        return iterator().map(func).toList();
    }
}

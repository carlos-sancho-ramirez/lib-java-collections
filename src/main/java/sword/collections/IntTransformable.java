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
     * Converts this collection into a set.
     *
     * All duplicated elements within the collection will be removed as sets does not allow duplicating values.
     * Because of that, the amount of elements in the resulting set may be less
     * than the amount of elements in the original list, but never more.
     *
     * The iteration order of elements in the resulting set is not guaranteed
     * to be the same that was in the collection even if no elements are removed for duplication.
     */
    default IntSet toSet() {
        return iterator().toSet();
    }

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

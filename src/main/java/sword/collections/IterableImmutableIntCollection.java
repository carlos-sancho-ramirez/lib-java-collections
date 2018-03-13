package sword.collections;

interface IterableImmutableIntCollection extends IterableIntCollection {

    /**
     * Applies the given predicate to each of the values within the collection and
     * composes a new collection including only the values whose predicate returned
     * true.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    IterableImmutableIntCollection filter(IntPredicate predicate);

    /**
     * Applies the given predicate to each of the values within the collection and
     * composes a new collection including only the values whose predicate returned
     * false.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    IterableImmutableIntCollection filterNot(IntPredicate predicate);

    /**
     * Applies the given function to each element on the collection and composes a
     * new collection with the results of each function execution in the same iteration order.
     *
     * @param func Function to be applied to each value in the collection.
     */
    IterableImmutableIntCollection map(IntToIntFunction func);

    /**
     * Applies the given function to each element on the collection and composes a
     * new {@link IterableCollection} of the same size with the results of each
     * function execution in the same iteration order.
     *
     * @param func Function to be applied to each value in the collection.
     * @param <U> New type for the elements in the new created collection.
     */
    <U> IterableImmutableCollection<U> map(IntFunction<U> func);
}

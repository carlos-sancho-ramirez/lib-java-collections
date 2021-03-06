package sword.collections;

interface ImmutableIntTransformable extends IntTransformable {

    @Override
    ImmutableIntList toList();

    @Override
    ImmutableIntSet toSet();

    /**
     * Applies the given predicate to each of the values within the collection and
     * composes a new collection including only the values whose predicate returned
     * true.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    ImmutableIntTransformable filter(IntPredicate predicate);

    /**
     * Applies the given predicate to each of the values within the collection and
     * composes a new collection including only the values whose predicate returned
     * false.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    ImmutableIntTransformable filterNot(IntPredicate predicate);

    /**
     * Applies the given function to each element on the collection and composes a
     * new collection with the results of each function execution in the same iteration order.
     *
     * @param func Function to be applied to each value in the collection.
     */
    ImmutableIntTransformable mapToInt(IntToIntFunction func);

    /**
     * Applies the given function to each element on the collection and composes a
     * new {@link Traversable} of the same size with the results of each
     * function execution in the same iteration order.
     *
     * @param func Function to be applied to each value in the collection.
     * @param <U> New type for the elements in the new created collection.
     */
    <U> ImmutableTransformable<U> map(IntFunction<? extends U> func);

    /**
     * Returns a new {@link ImmutableIntTransformable} where the item in the given position has been removed.
     * Calling this method will always result in a new instance whose size is 1 less than the original one.
     * @param index Valid index within the collection. This must be between 0 (included) and the value returned by {@link #size()} (excluded)
     * @return a new {@link ImmutableIntTransformable} where the item in the given position has been removed.
     * @throws java.lang.IndexOutOfBoundsException when an invalid index is provided.
     */
    ImmutableIntTransformable removeAt(int index);

    ImmutableIntPairMap count();
}

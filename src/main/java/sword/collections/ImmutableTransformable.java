package sword.collections;

public interface ImmutableTransformable<T> extends Transformable<T> {

    @Override
    ImmutableList<T> toList();

    @Override
    ImmutableSet<T> toSet();

    @Override
    ImmutableIntSet indexes();

    @Override
    ImmutableIntValueMap<T> count();

    /**
     * Applies the given predicate to each of the items within the list and
     * composes a new list including only the items whose predicate returned
     * true.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    ImmutableTransformable<T> filter(Predicate<? super T> predicate);

    /**
     * Applies the given predicate to each of the items within the list and
     * composes a new list including only the items whose predicate returned
     * false.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    ImmutableTransformable<T> filterNot(Predicate<? super T> predicate);

    /**
     * Applies the given function to each element on the collection and composes a
     * new {@link ImmutableIntTransformable} with the results of each
     * function execution in the same list index.
     *
     * @param func Function to be applied to each element in the list
     */
    ImmutableIntTransformable mapToInt(IntResultFunction<T> func);

    /**
     * Applies the given function to each element on the collection and composes a
     * new {@link ImmutableTransformable} with the results of each
     * function execution in the same list index.
     *
     * @param func Function to be applied to each element in the list
     * @param <U> New type for the elements in the new created collection.
     */
    <U> ImmutableTransformable<U> map(Function<T, U> func);

    /**
     * Returns a new {@link ImmutableTransformable} where the item in the given position has been removed.
     * Calling this method will always result in a new instance whose size is 1 less than the original one.
     * @param index Valid index within the collection. This must be between 0 (included) and the value returned by {@link #size()} (excluded)
     * @return a new {@link ImmutableTransformable} where the item in the given position has been removed.
     * @throws java.lang.IndexOutOfBoundsException when an invalid index is provided.
     */
    ImmutableTransformable<T> removeAt(int index);
}

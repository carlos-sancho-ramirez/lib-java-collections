package sword.collections;

public interface ImmutableTransformable<T> extends Transformable<T> {

    /**
     * Applies the given predicate to each of the items within the list and
     * composes a new list including only the items whose predicate returned
     * true.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    ImmutableTransformable<T> filter(Predicate<T> predicate);

    /**
     * Applies the given predicate to each of the items within the list and
     * composes a new list including only the items whose predicate returned
     * false.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    ImmutableTransformable<T> filterNot(Predicate<T> predicate);

    /**
     * Applies the given function to each element on the collection and composes a
     * new {@link ImmutableIntTraversable} with the results of each
     * function execution in the same list index.
     *
     * @param func Function to be applied to each element in the list
     */
    ImmutableIntTraversable mapToInt(IntResultFunction<T> func);

    /**
     * Applies the given function to each element on the collection and composes a
     * new {@link ImmutableTransformable} with the results of each
     * function execution in the same list index.
     *
     * @param func Function to be applied to each element in the list
     * @param <U> New type for the elements in the new created collection.
     */
    <U> ImmutableTransformable<U> map(Function<T, U> func);
}

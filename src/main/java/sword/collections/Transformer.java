package sword.collections;

/**
 * {@link Traverser} with extra method to create new collections from the values of this collection.
 *
 * Implementation of all methods within this interface requires returning actual collections,
 * that means that implementations are strongly linked with a collection implementation.
 * In order to isolate interfaces with actual collection implementations, this interface
 * cannot provide default implementations.
 *
 * @param <T> Type for the values to be traversed.
 */
public interface Transformer<T> extends Traverser<T> {

    /**
     * Build a new list containing all elements given on traversing this collection
     */
    List<T> toList();

    /**
     * Build a new set containing all elements given on traversing this collection.
     * The resulting set will not contain any duplicated value.
     */
    Set<T> toSet();

    /**
     * Return all indexes from the current position ignoring the actual values of this collection.
     */
    IntTransformer indexes();

    /**
     * Composes a new Transformer that filters this one by applying the given predicate to the
     * elements within this collection.
     * @param predicate Only value returning true for the given this predicate will be present
     *                  in the resulting Transformer.
     */
    Transformer<T> filter(Predicate<? super T> predicate);

    /**
     * Composes a new Transformer that filters this one by applying the given predicate to the
     * elements within this collection and collecting all elements where the predicate does not hold.
     * @param predicate Only value returning false for the given this predicate will be present
     *                  in the resulting Transformer.
     */
    Transformer<T> filterNot(Predicate<? super T> predicate);

    /**
     * Applies the given function to each of the
     * values within this collection keeping its order.
     * @param mapFunc Function to be applied to each of the values within the collection.
     * @return A transformer that applies the given function just in time.
     */
    IntTransformer mapToInt(IntResultFunction<? super T> mapFunc);

    /**
     * Applies the given function to each of the
     * values within this collection keeping its order.
     * @param mapFunc Function to be applied to each of the values within the collection.
     * @return A transformer that applies the given function just in time.
     */
    <U> Transformer<U> map(Function<? super T, ? extends U> mapFunc);

    /**
     * Composes a map by traversing the collection and counting how many times an element is found.
     *
     * The resulting map will have the elements from this transformer as keys,
     * and the number of times that each element is found as its value.
     *
     * The {@link Object#equals(Object)} method will be used to compare among the elements.
     *
     * It is expected that the size of the resulting map will never be longer that the original one,
     * and the sum of all resulting values should match the original size. Note that the values within
     * the resulting map will never be negative nor 0.
     */
    IntValueMap<T> count();
}

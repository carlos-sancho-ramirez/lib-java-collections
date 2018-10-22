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
    Transformer<T> filter(Predicate<T> predicate);

    /**
     * Applies the given function to each of the
     * values within this collection keeping its order.
     * @param mapFunc Function to be applied to each of the values within the collection.
     * @return A transformer that applies the given function just in time.
     */
    IntTransformer mapToInt(IntResultFunction<T> mapFunc);

    /**
     * Applies the given function to each of the
     * values within this collection keeping its order.
     * @param mapFunc Function to be applied to each of the values within the collection.
     * @return A transformer that applies the given function just in time.
     */
    <U> Transformer<U> map(Function<T, U> mapFunc);
}

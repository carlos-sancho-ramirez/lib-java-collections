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
     * Return all indexes from the current position ignoring the actual values of this collection.
     */
    IntTransformer indexes();

    /**
     * Applies the given function to each of the
     * values within this collection keeping its order.
     * @param mapFunc Function to be applied to each of the values within the collection.
     * @return A transformer that applies the given function just in time.
     */
    IntTransformer mapToInt(IntResultFunction<T> mapFunc);
}

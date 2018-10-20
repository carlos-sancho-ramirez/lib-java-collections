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
     * Traverses the collection and return all indexes from the current position.
     */
    IntSet indexes();

    /**
     * Builds a new list of integers after applying the given function to each of the
     * values within this collection keeping its order.
     * @param mapFunc Function to be applied to each of the values within the collection.
     * @return A new list of integers containing all the results of applying the given function.
     */
    IntList mapToInt(IntResultFunction<T> mapFunc);
}

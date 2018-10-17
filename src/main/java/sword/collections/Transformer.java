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
}

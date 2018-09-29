package sword.collections;

/**
 * Base class for all iterators for immutable collections.
 * All immutable collections should not allow modifications, so calling remove should be considered an error.
 * @param <E> Generic type holding this Iterator
 */
public abstract class IteratorForImmutable<E> implements Traverser<E> {

    @Override
    public final void remove() {
        throw new UnsupportedOperationException("This class is immutable");
    }
}

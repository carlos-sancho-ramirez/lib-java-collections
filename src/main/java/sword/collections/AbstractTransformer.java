package sword.collections;

/**
 * Base class for all iterators for immutable collections.
 * All immutable collections should not allow modifications, so calling remove should be considered an error.
 * @param <E> Generic type holding this Iterator
 */
public abstract class AbstractTransformer<E> implements Transformer<E> {

    @Override
    public IntSet indexes() {
        int length = 0;
        while (hasNext()) {
            length++;
            next();
        }

        return (length == 0)? ImmutableIntSetImpl.empty() : new ImmutableIntRange(0, length - 1);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("This class is immutable");
    }
}

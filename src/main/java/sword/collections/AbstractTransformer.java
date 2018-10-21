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
    public IntList mapToInt(IntResultFunction<E> mapFunc) {
        final ImmutableIntList.Builder builder = new ImmutableIntList.Builder();
        while (hasNext()) {
            builder.add(mapFunc.apply(next()));
        }

        return builder.build();
    }

    @Override
    public List<E> toList() {
        final ImmutableList.Builder<E> builder = new ImmutableList.Builder<>();
        while (hasNext()) {
            builder.add(next());
        }

        return builder.build();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("This class is immutable");
    }
}

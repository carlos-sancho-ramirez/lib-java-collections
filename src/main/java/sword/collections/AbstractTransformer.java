package sword.collections;

/**
 * Base class for all iterators for immutable collections.
 * All immutable collections should not allow modifications, so calling remove should be considered an error.
 * @param <E> Generic type holding this Iterator
 */
public abstract class AbstractTransformer<E> implements Transformer<E> {

    @Override
    public IntTransformer indexes() {
        return new IndexesTransformer<>(this);
    }

    @Override
    public Transformer<E> filter(Predicate<? super E> predicate) {
        return new FilterTransformer<>(this, predicate);
    }

    @Override
    public Transformer<E> filterNot(Predicate<? super E> predicate) {
        return new FilterTransformer<>(this, v -> !predicate.apply(v));
    }

    @Override
    public IntTransformer mapToInt(IntResultFunction<E> mapFunc) {
        return new MapToIntTransformer<>(this, mapFunc);
    }

    @Override
    public <U> Transformer<U> map(Function<E, U> mapFunc) {
        return new MapTransformer<>(this, mapFunc);
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
    public Set<E> toSet() {
        final ImmutableHashSet.Builder<E> builder = new ImmutableHashSet.Builder<>();
        while (hasNext()) {
            builder.add(next());
        }

        return builder.build();
    }

    @Override
    public IntValueMap<E> count() {
        final MutableIntValueMap<E> result = MutableIntValueHashMap.empty();
        while (hasNext()) {
            final E value = next();
            final int amount = result.get(value, 0);
            result.put(value, amount + 1);
        }

        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("This class is immutable");
    }
}

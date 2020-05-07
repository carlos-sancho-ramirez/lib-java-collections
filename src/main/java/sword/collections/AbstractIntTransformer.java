package sword.collections;

/**
 * Base class for all iterators for immutable int collections.
 * All immutable collections should not allow modifications, so calling remove should be considered an error.
 */
public abstract class AbstractIntTransformer implements IntTransformer {

    @Override
    public IntList toList() {
        final ImmutableIntList.Builder builder = new ImmutableIntList.Builder();
        while (hasNext()) {
            builder.add(next());
        }

        return builder.build();
    }

    @Override
    public IntSet toSet() {
        final ImmutableIntSetCreator builder = new ImmutableIntSetCreator();
        while (hasNext()) {
            builder.add(next());
        }

        return builder.build();
    }

    @Override
    public IntTransformer indexes() {
        return new IndexesTransformer<>(this);
    }

    @Override
    public IntTransformer filter(IntPredicate predicate) {
        return new FilterIntTransformer(this, predicate);
    }

    @Override
    public IntTransformer filterNot(IntPredicate predicate) {
        return new FilterIntTransformer(this, v -> !predicate.apply(v));
    }

    @Override
    public IntTransformer mapToInt(IntToIntFunction mapFunc) {
        return new MapToIntIntTransformer<>(this, mapFunc);
    }

    @Override
    public <U> Transformer<U> map(IntFunction<? extends U> mapFunc) {
        return new MapIntTransformer<>(this, mapFunc);
    }

    @Override
    public IntPairMap count() {
        final MutableIntPairMap result = MutableIntPairMap.empty();
        while (hasNext()) {
            final int value = next();
            final int amount = result.get(value, 0);
            result.put(value, amount + 1);
        }

        return result;
    }
}

package sword.collections;

abstract class AbstractIntTransformer implements IntTransformer {

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
        final ImmutableIntSetBuilder builder = new ImmutableIntSetBuilder();
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
    public <U> Transformer<U> map(IntFunction<U> mapFunc) {
        return new MapIntTransformer<>(this, mapFunc);
    }
}

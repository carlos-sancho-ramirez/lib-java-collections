package sword.collections;

abstract class AbstractImmutableIntIterable extends AbstractIntIterable implements IterableImmutableIntCollection {

    abstract ImmutableIntCollectionBuilder newIntBuilder();
    abstract <U> ImmutableCollectionBuilder<U> newBuilder();

    @Override
    public IterableImmutableIntCollection filter(IntPredicate predicate) {
        boolean somethingRemoved = false;
        ImmutableIntCollectionBuilder builder = newIntBuilder();
        for (int item : this) {
            if (predicate.apply(item)) {
                builder.add(item);
            }
            else {
                somethingRemoved = true;
            }
        }

        return somethingRemoved? builder.build() : this;
    }

    @Override
    public IterableImmutableIntCollection filterNot(IntPredicate predicate) {
        boolean somethingRemoved = false;
        ImmutableIntCollectionBuilder builder = newIntBuilder();
        for (int item : this) {
            if (predicate.apply(item)) {
                somethingRemoved = true;
            }
            else {
                builder.add(item);
            }
        }

        return somethingRemoved? builder.build() : this;
    }

    @Override
    public IterableImmutableIntCollection mapToInt(IntToIntFunction func) {
        final ImmutableIntCollectionBuilder builder = newIntBuilder();

        for (int item : this) {
            builder.add(func.apply(item));
        }

        return builder.build();
    }

    @Override
    public <U> IterableImmutableCollection<U> map(IntFunction<U> func) {
        final ImmutableCollectionBuilder<U> builder = newBuilder();

        for (int item : this) {
            builder.add(func.apply(item));
        }

        return builder.build();
    }
}

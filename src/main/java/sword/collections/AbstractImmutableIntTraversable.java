package sword.collections;

abstract class AbstractImmutableIntTraversable extends AbstractIntTraversable implements ImmutableIntTraversable {

    abstract ImmutableIntCollectionBuilder newIntBuilder();
    abstract <U> ImmutableTransformableBuilder<U> newBuilder();

    @Override
    public ImmutableIntTraversable filter(IntPredicate predicate) {
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
    public ImmutableIntTraversable filterNot(IntPredicate predicate) {
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
    public ImmutableIntTraversable mapToInt(IntToIntFunction func) {
        final ImmutableIntCollectionBuilder builder = newIntBuilder();

        for (int item : this) {
            builder.add(func.apply(item));
        }

        return builder.build();
    }

    @Override
    public <U> ImmutableTransformable<U> map(IntFunction<U> func) {
        final ImmutableTransformableBuilder<U> builder = newBuilder();

        for (int item : this) {
            builder.add(func.apply(item));
        }

        return builder.build();
    }
}

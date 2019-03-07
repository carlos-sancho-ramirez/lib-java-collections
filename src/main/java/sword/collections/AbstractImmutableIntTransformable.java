package sword.collections;

abstract class AbstractImmutableIntTransformable extends AbstractIntTraversable implements ImmutableIntTransformable {

    abstract ImmutableIntTransformableBuilder newIntBuilder();
    abstract <U> ImmutableTransformableBuilder<U> newBuilder();

    @Override
    public ImmutableIntTransformable filter(IntPredicate predicate) {
        boolean somethingRemoved = false;
        ImmutableIntTransformableBuilder builder = newIntBuilder();
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
    public ImmutableIntTransformable filterNot(IntPredicate predicate) {
        boolean somethingRemoved = false;
        ImmutableIntTransformableBuilder builder = newIntBuilder();
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
    public ImmutableIntTransformable mapToInt(IntToIntFunction func) {
        final ImmutableIntTransformableBuilder builder = newIntBuilder();

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

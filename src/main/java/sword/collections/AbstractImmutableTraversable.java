package sword.collections;

abstract class AbstractImmutableTraversable<T> extends AbstractTraversable<T> implements ImmutableTransformable<T> {

    abstract <U> ImmutableTransformableBuilder<U> newBuilder();

    @Override
    public ImmutableTransformable<T> filter(Predicate<T> predicate) {
        boolean somethingRemoved = false;
        ImmutableTransformableBuilder<T> builder = newBuilder();
        for (T item : this) {
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
    public ImmutableTransformable<T> filterNot(Predicate<T> predicate) {
        boolean somethingRemoved = false;
        ImmutableTransformableBuilder<T> builder = newBuilder();
        for (T item : this) {
            if (predicate.apply(item)) {
                somethingRemoved = true;
            }
            else {
                builder.add(item);
            }
        }

        return somethingRemoved? builder.build() : this;
    }
}

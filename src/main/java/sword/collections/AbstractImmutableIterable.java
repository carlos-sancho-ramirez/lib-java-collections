package sword.collections;

abstract class AbstractImmutableIterable<T> extends AbstractIterable<T> implements IterableImmutableCollection<T> {

    abstract <U> ImmutableCollectionBuilder<U> newBuilder();

    @Override
    public IterableImmutableCollection<T> filter(Predicate<T> predicate) {
        boolean somethingRemoved = false;
        ImmutableCollectionBuilder<T> builder = newBuilder();
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
    public IterableImmutableCollection<T> filterNot(Predicate<T> predicate) {
        boolean somethingRemoved = false;
        ImmutableCollectionBuilder<T> builder = newBuilder();
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

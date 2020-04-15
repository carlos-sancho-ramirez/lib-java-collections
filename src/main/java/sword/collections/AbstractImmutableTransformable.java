package sword.collections;

abstract class AbstractImmutableTransformable<T> extends AbstractTraversable<T> implements ImmutableTransformable<T> {

    abstract <U> ImmutableTransformableBuilder<U> newBuilder();

    @Override
    public ImmutableTransformable<T> filter(Predicate<? super T> predicate) {
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
    public ImmutableTransformable<T> filterNot(Predicate<? super T> predicate) {
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

    @Override
    public ImmutableIntValueMap<T> count() {
        final MutableIntValueMap<T> result = MutableIntValueHashMap.empty();
        for (T value : this) {
            final int amount = result.get(value, 0);
            result.put(value, amount + 1);
        }

        return result.toImmutable();
    }
}

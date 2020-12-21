package sword.collections;

/**
 * Mutable version of a Set.
 *
 * This Set is mutable, that means that, in contrast with the immutable version,
 * algorithms to insert, modify and remove are required. This may affect its
 * internal memory footprint to ensure easy modification.
 *
 * This implementation assumes that elements inserted are immutable, or at least
 * its hashCode will not change and equals method logic will no depend on any change.
 * It is not guaranteed to work if any of the elements is mutable.
 *
 * @param <T> Type for the elements within the Set
 */
public interface MutableSet<T> extends Set<T>, MutableTraversable<T> {

    boolean add(T key);
    boolean remove(T key);

    /**
     * Add all the items from the given iterable into this set.
     *
     * As this is a set, any item that is already included, or repeated in the
     * given collection will be ignored. Thus, the resulting collection size
     * can be increased by less than the iterable size.
     *
     * @param iterable Iterable collection from where items will be read.
     * @return Whether this operation performed any change in this set or not.
     *         This will be true if at least one of the items has been included.
     */
    default boolean addAll(Iterable<? extends T> iterable) {
        boolean somethingChanged = false;
        for (T item : iterable) {
            somethingChanged |= add(item);
        }

        return somethingChanged;
    }

    interface Builder<E> extends Set.Builder<E>, MutableTraversableBuilder<E> {
        @Override
        Builder<E> add(E element);

        @Override
        MutableSet<E> build();
    }
}

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
public interface MutableSet<T> extends Set<T>, MutableIterableCollection<T> {

    @SuppressWarnings("unchecked")
    T keyAt(int index);

    boolean add(T key);
    boolean remove(T key);

    interface Builder<E> extends TransformableBuilder<E> {
        @Override
        Builder<E> add(E element);

        @Override
        MutableSet<E> build();
    }
}

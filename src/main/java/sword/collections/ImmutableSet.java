package sword.collections;

/**
 * Immutable version of a Set.
 *
 * This Set is immutable, that means that its content cannot be modified once
 * it is created. This also means that, as no algorithms to insert, modify and
 * remove are required, its memory layout can be simplified and its footprint
 * can be reduced in a more optimal way.
 *
 * @param <T> Type for the elements within the Set
 */
public interface ImmutableSet<T> extends Set<T>, ImmutableTransformable<T> {

    @SuppressWarnings("unchecked")
    T keyAt(int index);

    @Override
    ImmutableSet<T> filter(Predicate<T> predicate);

    @Override
    ImmutableSet<T> filterNot(Predicate<T> predicate);

    @Override
    ImmutableIntList mapToInt(IntResultFunction<T> func);

    @Override
    <E> ImmutableList<E> map(Function<T, E> func);

    /**
     * Creates a new {@link ImmutableSet} of the same type where the given
     * value is included.
     *
     * Note that no repeated value are allowed in a Set. If the given value is already in the set,
     * the same instance will be returned instead.
     *
     * @param value item to be included.
     */
    ImmutableSet<T> add(T value);

    /**
     * Creates a new set containing all the current elements and the ones given in the iterable.
     *
     * As this is a set, duplicated elements will not be allowed.
     * Than means that elements within the given iterable will be ignored if
     * there is an equivalent element already included in this set.
     *
     * @param iterable Collection from where new items will be added.
     */
    ImmutableSet<T> addAll(Iterable<T> iterable);

    @Override
    ImmutableList<T> toList();

    @Override
    ImmutableSet<T> sort(SortFunction<T> function);

    /**
     * Check if 2 set instances contain equivalent elements.
     *
     * This method will call {@link Object#equals(Object)} on the elements
     * within the sets in order to check if they are equivalent.
     *
     * Note that this method will return true even if the elements within the 2
     * sets are not sorted in the same way. In contrast with {@link Object#equals(Object)},
     * that will return true only if all items are sorted in the same way.
     *
     * @param set set to be compared with this instance.
     * @return whether the given set contains equivalent values to this one.
     */
    boolean equalsInItems(Set set);

    interface Builder<E> extends ImmutableTransformableBuilder<E> {

        @Override
        Builder<E> add(E element);

        @Override
        ImmutableSet<E> build();
    }
}

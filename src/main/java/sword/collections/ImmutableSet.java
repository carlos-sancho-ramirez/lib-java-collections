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

    @Override
    ImmutableSet<T> filter(Predicate<? super T> predicate);

    @Override
    ImmutableSet<T> filterNot(Predicate<? super T> predicate);

    @Override
    ImmutableIntList mapToInt(IntResultFunction<? super T> func);

    @Override
    <E> ImmutableList<E> map(Function<? super T, ? extends E> func);

    @Override
    <V> ImmutableMap<T, V> assign(Function<T, V> function);

    @Override
    ImmutableIntValueMap<T> assignToInt(IntResultFunction<T> function);

    @Override
    ImmutableSet<T> removeAt(int index);

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
    ImmutableSet<T> sort(SortFunction<T> function);

    interface Builder<E> extends Set.Builder<E>, ImmutableTransformableBuilder<E> {

        @Override
        Builder<E> add(E element);

        @Override
        ImmutableSet<E> build();
    }
}

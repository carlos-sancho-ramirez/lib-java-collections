package sword.collections;

/**
 * Root for both variants of Sets, immutable and mutable.
 *
 * 'Set' must be understood as a collection where its elements cannot be repeated.
 * 2 elements are considered to be the same, so they would be duplicated, if both
 * return the same hash code and calling equals returns true.
 *
 * This interface also extends the {@link Iterable} interface, which
 * ensures that the for-each construction can be used. This implies that there is
 * always a consistent order within the elements, even if it is implicit.
 *
 * @param <T> Type of items within the set.
 */
public interface Set<T> extends Transformable<T> {

    @Override
    default Set<T> filter(Predicate<? super T> predicate) {
        return iterator().filter(predicate).toSet();
    }

    @Override
    default Set<T> filterNot(Predicate<? super T> predicate) {
        return iterator().filterNot(predicate).toSet();
    }

    @Override
    default IntList mapToInt(IntResultFunction<? super T> func) {
        return iterator().mapToInt(func).toList();
    }

    @Override
    <E> List<E> map(Function<? super T, ? extends E> func);

    /**
     * Assign a value calculated though the given function to each value of this set,
     * resulting in a map where the values of this set become the keys of the new map.
     *
     * @param function Function to calculate the suitable value for the new map.
     * @param <V> Type of the value in the resulting map.
     * @return A new map where value of this set becomes its keys,
     *         and values are calculated through the given function.
     */
    <V> Map<T, V> assign(Function<? super T, ? extends V> function);

    /**
     * Assign a value calculated though the given function to each value of this set,
     * resulting in a map where the values of this set become that keys of the new map.
     *
     * @param function Function to calculate the suitable value for the new map.
     * @return A new map where value of this set becomes its keys,
     *         and values are calculated through the given function.
     */
    IntValueMap<T> assignToInt(IntResultFunction<? super T> function);

    /**
     * Return an immutable set from the values contained in this set.
     * The same instance will be returned in case of being already immutable.
     */
    ImmutableSet<T> toImmutable();

    /**
     * Return a new mutable set.
     * This method will always generate a new instance in order to avoid affecting the state of its original set.
     */
    MutableSet<T> mutate();

    /**
     * Return a new mutable set with the given {@link ArrayLengthFunction}.
     * This method will always generate a new instance in order to avoid affecting the state of its original set.
     */
    MutableSet<T> mutate(ArrayLengthFunction arrayLengthFunction);

    /**
     * Creates a new set where all current elements and future elements will be
     * sorted following the given function.
     *
     * @param function Function the sort the elements within this collection.
     * @return A new set where all current elements and future newly added will
     * be sorted following the given function.
     */
    Set<T> sort(SortFunction<? super T> function);

    @Override
    default Set<T> slice(ImmutableIntRange range) {
        final int size = size();
        final int min = range.min();
        final int max = range.max();
        if (min >= size || max < 0) {
            return ImmutableHashSet.empty();
        }

        if (range.min() <= 0 && range.max() >= size - 1) {
            return this;
        }

        final ImmutableHashSet.Builder<T> builder = new ImmutableHashSet.Builder<>();
        final int maxPosition = Math.min(max, size - 1);
        for (int position = min; position <= maxPosition; position++) {
            builder.add(valueAt(position));
        }

        return builder.build();
    }

    /**
     * Returns a new ImmutableSet of the same type where the
     * <code>length</code> amount of first elements has been removed.
     * <p>
     * This will return an empty list if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the start of the list.
     * @return A new ImmutableSet instance without the first elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance of the given length is equal or greater
     *         than the actual length of the set.
     */
    default Set<T> skip(int length) {
        return slice(new ImmutableIntRange(length, Integer.MAX_VALUE));
    }

    /**
     * Returns a hash code calculated from the hashcode of any of the elements.
     *
     * The resulting hashcode is guaranteed to be the same independently of the
     * order of the elements within this set, and its mutability state.
     */
    @Override
    int hashCode();

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
    boolean equalSet(Set set);

    interface Builder<E> extends TransformableBuilder<E> {
        @Override
        Builder<E> add(E value);

        @Override
        Set<E> build();
    }
}

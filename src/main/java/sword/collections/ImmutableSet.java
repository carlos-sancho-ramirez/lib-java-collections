package sword.collections;

import sword.annotations.ToBeAbstract;

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
    default ImmutableSet<T> filterNot(Predicate<? super T> predicate) {
        return filter(v -> !predicate.apply(v));
    }

    @Override
    ImmutableIntList mapToInt(IntResultFunction<? super T> func);

    @Override
    <E> ImmutableList<E> map(Function<? super T, ? extends E> func);

    @Override
    <V> ImmutableMap<T, V> assign(Function<? super T, ? extends V> function);

    @Override
    ImmutableIntValueMap<T> assignToInt(IntResultFunction<? super T> function);

    @Override
    ImmutableSet<T> removeAt(int index);

    /**
     * Remove the given value from the current set if included.
     *
     * As this class is immutable, this method do not affect in the
     * current values of this set, but a new set is returned instead.
     *
     * @param value Value to be removed.
     * @return A new set containing all elements included in this set
     *         less the one given, or this same instance if the value
     *         was not already present.
     */
    ImmutableSet<T> remove(T value);

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
    default ImmutableSet<T> addAll(Iterable<T> iterable) {
        ImmutableSet<T> acc = this;
        for (T item : iterable) {
            acc = acc.add(item);
        }

        return acc;
    }

    @Override
    ImmutableSet<T> sort(SortFunction<? super T> function);

    /**
     * Composes a new collection where the elements are extracted from this one
     * according to the positions given in the range.
     * <p>
     * The size of the resulting collection should be at most the size of the given
     * range. It can be less if the actual collection does not have enough elements.
     *
     * @param range Positions to be extracted from the original collection.
     *              Negative numbers are not expected.
     * @return A new collection where the elements are extracted from this collection.
     * @throws IllegalArgumentException in case the range is invalid.
     */
    @ToBeAbstract("This implementation is unable to provide the proper set type and iteration order for all its subtypes. Sorted sets will become hash sets, and that will modify the order of iteration")
    default ImmutableSet<T> slice(ImmutableIntRange range) {
        final int size = size();
        final int min = range.min();
        final int max = range.max();
        if (min >= size || max < 0) {
            return ImmutableHashSet.empty();
        }

        if (min <= 0 && max >= size - 1) {
            return this;
        }

        final ImmutableHashSet.Builder<T> builder = new ImmutableHashSet.Builder<>();
        final int maxPosition = Math.min(max, size - 1);
        for (int position = min; position <= maxPosition; position++) {
            builder.add(valueAt(position));
        }

        return builder.build();
    }

    @Override
    default ImmutableSet<T> skip(int length) {
        return isEmpty()? this : slice(new ImmutableIntRange(length, Integer.MAX_VALUE));
    }

    /**
     * Returns a new ImmutableSet where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of the set in iteration order.
     * @return A new ImmutableSet instance just including the first elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length is equal or greater than the
     *         actual size of this set.
     */
    @ToBeAbstract("This implementation is unable to provide the proper set type in case of sorted set. So the iteration order gets broken")
    default ImmutableSet<T> take(int length) {
        return isEmpty()? this : (length == 0)? ImmutableHashSet.empty() : slice(new ImmutableIntRange(0, length - 1));
    }

    /**
     * Returns a new ImmutableSet where the <code>length</code> amount of last
     * elements has been removed.
     * <p>
     * This will return an empty set if the given parameter matches or exceeds
     * the length of this collection.
     *
     * @param length the amount of elements to be removed from the end of the set.
     * @return A new ImmutableSet instance without the last elements,
     *         the same instance in case the given length is 0 or this set is already empty,
     *         or the empty instance if the given length is equal or greater
     *         than the actual length of the set.
     */
    @Override
    @ToBeAbstract("Unable to provide the proper type. If it was a sorted set, it sortFunction is lost")
    default ImmutableSet<T> skipLast(int length) {
        final int size = size();
        if (size == 0) {
            return this;
        }

        final int max = size - length - 1;
        return (max < 0)? ImmutableHashSet.empty() : slice(new ImmutableIntRange(0, max));
    }

    /**
     * Returns a new ImmutableSet where only the <code>length</code> amount of
     * last elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the end of this set.
     * @return A new ImmutableSet instance just including the last elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length equals or greater than the
     *         actual size of this collection.
     */
    @Override
    @ToBeAbstract("Unable to provide the proper type. If it was a sorted set, it sortFunction is lost")
    default ImmutableSet<T> takeLast(int length) {
        final int size = size();
        return (size == 0)? this : (length == 0)? ImmutableHashSet.empty() : slice(new ImmutableIntRange(size - length, size - 1));
    }

    interface Builder<E> extends Set.Builder<E>, ImmutableTransformableBuilder<E> {

        @Override
        Builder<E> add(E element);

        @Override
        ImmutableSet<E> build();
    }
}

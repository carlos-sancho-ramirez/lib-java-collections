package sword.collections;

import sword.annotations.ToBeAbstract;

public interface Transformable<T> extends Traversable<T> {

    Transformer<T> iterator();

    /**
     * Converts this collection to a list.
     *
     * The order of iteration is guaranteed to be the same in the new list.
     */
    default List<T> toList() {
        return iterator().toList();
    }

    /**
     * Converts this collection into a set.
     *
     * All duplicated elements within the collection will be removed as sets does not allow duplicating values.
     * Because of that, the amount of elements in the resulting set may be less
     * than the amount of elements in the original collection, but never more.
     *
     * The iteration order of elements in the resulting set is not guaranteed
     * to be the same that was in the collection even if no elements are removed for duplication.
     */
    default Set<T> toSet() {
        return iterator().toSet();
    }

    /**
     * Return all indexes within this collection in ascending order.
     */
    default IntSet indexes() {
        return iterator().indexes().toSet();
    }

    /**
     * Composes a new Transformable that filters this one by applying the given predicate to the
     * elements within this collection.
     * @param predicate Only value returning true for the given predicate will be present
     *                  in the resulting Transformable.
     */
    Transformable<T> filter(Predicate<? super T> predicate);

    /**
     * Composes a new Transformer that filters this one by applying the given predicate to the
     * elements within this collection and collecting all elements where the predicate does not hold.
     * @param predicate Only value returning false for the given predicate will be present
     *                  in the resulting Transformable.
     */
    default Transformable<T> filterNot(Predicate<? super T> predicate) {
        return filter(v -> !predicate.apply(v));
    }

    /**
     * Applies the given function to each element on the collection and composes a
     * new {@link IntTransformable} of the same size with the results of each
     * function execution in the same index.
     *
     * @param func Function to be applied to each element in this collection.
     */
    default IntTransformable mapToInt(IntResultFunction<? super T> func) {
        return iterator().mapToInt(func).toList();
    }

    /**
     * Applies the given function to each element on the collection and composes a
     * new {@link Transformable} of the same size with the results of each
     * function execution in the same index.
     *
     * @param func Function to be applied to each element in this collection.
     * @param <E> New type for the elements in the new created collection.
     */
    default <E> Transformable<E> map(Function<? super T, ? extends E> func) {
        return iterator().<E>map(func).toList();
    }

    /**
     * Composes a map by traversing the collection and counting how many times an element is found.
     *
     * The resulting map will have the elements from the collection as keys,
     * and the number of times that each element is found as its value.
     *
     * The {@link Object#equals(Object)} method will be used to compare among the elements.
     *
     * It is expected that the size of the resulting map will never be longer that the original one,
     * and the sum of all resulting values should match the original size. Note that the values within
     * the resulting map will never be negative nor 0.
     */
    default IntValueMap<T> count() {
        return iterator().count();
    }

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
    @ToBeAbstract("This implementation is unable to return the proper transformable type")
    default Transformable<T> slice(ImmutableIntRange range) {
        final int size = size();
        final int min = range.min();
        final int max = range.max();
        if (min <= 0 && max >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableList.empty();
        }

        final ImmutableList.Builder<T> builder = new ImmutableList.Builder<>();
        for (int position = min; position <= max && position < size; position++) {
            builder.append(valueAt(position));
        }

        return builder.build();
    }

    /**
     * Returns a new collection where the <code>length</code> amount of first elements has been removed.
     *
     * @param length the amount of elements to be removed from the start of the collection.
     * @return A new collection without the first elements.
     */
    default Transformable<T> skip(int length) {
        return slice(new ImmutableIntRange(length, Integer.MAX_VALUE));
    }

    /**
     * Returns a new collection where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of this map in iteration order.
     * @return A new collection just including the first elements.
     */
    @ToBeAbstract("This implementation is unable to provide the proper type")
    default Transformable<T> take(int length) {
        return (length == 0)? ImmutableList.empty() : slice(new ImmutableIntRange(0, length - 1));
    }

    /**
     * Returns a new collection where the <code>length</code> amount of last elements
     * has been removed.
     * <p>
     * This will return an empty collection if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the end of this collection.
     * @return A new instance without the last elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance if the given length is equal or greater
     *         than the actual length of the collection.
     */
    @ToBeAbstract("This implementation is unable to provide the proper transformable type. So the iteration order gets broken")
    default Transformable<T> skipLast(int length) {
        if (length == 0) {
            return this;
        }

        final int size = size();
        final int max = size - length - 1;
        return (max < 0)? ImmutableList.empty() : slice(new ImmutableIntRange(0, max));
    }
}

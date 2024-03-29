package sword.collections;

import sword.annotations.ToBeAbstract;
import sword.annotations.ToBeSubtyped;

public interface IntTransformable extends IntTraversable {
    @ToBeSubtyped(IntTransformerWithKey.class)
    IntTransformer iterator();

    /**
     * Converts collection to a list.
     *
     * The order of iteration is guaranteed to be the same in the new list.
     */
    default IntList toList() {
        return iterator().toList();
    }

    /**
     * Converts this collection into a set.
     *
     * All duplicated elements within the collection will be removed as sets does not allow duplicating values.
     * Because of that, the amount of elements in the resulting set may be less
     * than the amount of elements in the original list, but never more.
     *
     * The iteration order of elements in the resulting set is not guaranteed
     * to be the same that was in the collection even if no elements are removed for duplication.
     */
    default IntSet toSet() {
        return iterator().toSet();
    }

    /**
     * Return all indexes within this collection in ascending order.
     */
    default IntSet indexes() {
        return iterator().indexes().toSet();
    }

    /**
     * Applies the given predicate to each of the values within the set and
     * composes a new set including only the values whose predicate returned
     * true.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    IntTransformable filter(IntPredicate predicate);

    /**
     * Applies the given predicate to each of the values within the set and
     * composes a new set including only the values whose predicate returned
     * false.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    default IntTransformable filterNot(IntPredicate predicate) {
        return filter(v -> !predicate.apply(v));
    }

    default IntTransformable mapToInt(IntToIntFunction func) {
        return iterator().mapToInt(func).toList();
    }

    default <U> Transformable<U> map(IntFunction<? extends U> func) {
        return iterator().<U>map(func).toList();
    }

    /**
     * Composes a map by traversing the collection and counting how many times an element is found.
     *
     * The resulting map will have the elements from the collection as keys,
     * and the number of times that each element is found as its value.
     *
     * It is expected that the size of the resulting map will never be longer that the original one,
     * and the sum of all resulting values should match the original size. Note that the values within
     * the resulting map will never be negative nor 0.
     */
    default IntPairMap count() {
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
     */
    @ToBeAbstract("This implementation is unable to return the proper transformable type")
    default IntTransformable slice(ImmutableIntRange range) {
        final int size = size();
        final int min = range.min();
        final int max = range.max();
        if (min <= 0 && max >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableIntList.empty();
        }

        final ImmutableIntList.Builder builder = new ImmutableIntList.Builder();
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
    default IntTransformable skip(int length) {
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
    default IntTransformable take(int length) {
        return (length == 0)? ImmutableIntList.empty() : slice(new ImmutableIntRange(0, length - 1));
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
    default IntTransformable skipLast(int length) {
        if (length == 0) {
            return this;
        }

        final int size = size();
        final int max = size - length - 1;
        return (max < 0)? ImmutableIntList.empty() : slice(new ImmutableIntRange(0, max));
    }

    /**
     * Returns a new collection where only the <code>length</code> amount of
     * last elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the end of this collection.
     * @return A new instance just including the last elements,
     *         an empty instance in case the given length is 0.
     */
    @ToBeAbstract("Unable to provide the proper type. If it was a sorted collection, its sortFunction is lost")
    default IntTransformable takeLast(int length) {
        final int size = size();
        return (size == 0)? this : (length == 0)? ImmutableIntList.empty() : slice(new ImmutableIntRange(size - length, size - 1));
    }
}

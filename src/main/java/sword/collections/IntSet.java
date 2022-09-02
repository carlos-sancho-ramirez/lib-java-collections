package sword.collections;

import sword.annotations.ToBeAbstract;

public interface IntSet extends IntTransformable {

    /**
     * Applies the given predicate to each of the values within the set and
     * composes a new set including only the values whose predicate returned
     * true.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    default IntSet filter(IntPredicate predicate) {
        return iterator().filter(predicate).toSet();
    }

    /**
     * Applies the given predicate to each of the values within the set and
     * composes a new set including only the values whose predicate returned
     * false.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    default IntSet filterNot(IntPredicate predicate) {
        return iterator().filterNot(predicate).toSet();
    }

    default IntList mapToInt(IntToIntFunction func) {
        return iterator().mapToInt(func).toList();
    }

    default <U> List<U> map(IntFunction<? extends U> func) {
        return iterator().<U>map(func).toList();
    }

    /**
     * Assign a value calculated though the given function to each value of this set,
     * resulting in a map where the values of this set become that keys of the new map.
     *
     * @param function Function to calculate the suitable value for the new map.
     * @param <V> Type of the value in the resulting map.
     * @return A new map where value of this set becomes its keys,
     *         and values are calculated through the given function.
     */
    <V> IntKeyMap<V> assign(IntFunction<? extends V> function);

    /**
     * Assign a value calculated though the given function to each value of this set,
     * resulting in a map where the values of this set become that keys of the new map.
     *
     * @param function Function to calculate the suitable value for the new map.
     * @return A new map where value of this set becomes its keys,
     *         and values are calculated through the given function.
     */
    IntPairMap assignToInt(IntToIntFunction function);

    @Override
    default IntSet slice(ImmutableIntRange range) {
        final int size = size();
        final int min = range.min();
        final int max = range.max();
        if (min <= 0 && max >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableIntArraySet.empty();
        }

        final int newSize = Math.min(size - 1, max) - min + 1;
        final ImmutableIntSetCreator builder = new ImmutableIntSetCreator((currentSize, nS) -> newSize);
        for (int i = 0; i < newSize; i++) {
            builder.add(valueAt(min + i));
        }

        return builder.build();
    }

    /**
     * Returns a new IntSet where the <code>length</code>
     * amount of first elements in iteration order has been removed.
     * <p>
     * This will return an empty set if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the start of the set.
     * @return A new IntSet instance without the first elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance of the given length is equal or greater
     *         than the actual length of the list.
     */
    @Override
    default IntSet skip(int length) {
        return slice(new ImmutableIntRange(length, Integer.MAX_VALUE));
    }

    /**
     * Returns a new IntSet where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of the set in iteration order.
     * @return A new IntSet instance just including the first elements,
     *         an empty instance in case the given length is 0, or the same
     *         instance in case the given length is equal or greater than the
     *         actual size of this set.
     */
    default IntSet take(int length) {
        return (length == 0)? ImmutableIntArraySet.empty() : slice(new ImmutableIntRange(0, length - 1));
    }

    /**
     * Returns a new IntSet where the <code>length</code> amount of last elements
     * has been removed.
     * <p>
     * This will return an empty set if the given parameter matches
     * or exceeds the length of this collection.
     *
     * @param length the amount of elements to be removed from the end of the set.
     * @return A new IntSet instance without the last elements,
     *         the same instance in case the given length is 0,
     *         or an empty instance if the given length is equal or greater
     *         than the actual length of the set.
     */
    @Override
    default IntSet skipLast(int length) {
        if (length == 0) {
            return this;
        }

        final int size = size();
        final int max = size - length - 1;
        return (max < 0)? ImmutableIntArraySet.empty() : slice(new ImmutableIntRange(0, max));
    }

    /**
     * Returns a new IntSet where only the <code>length</code> amount of
     * last elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the end of this set.
     * @return A new IntSet instance just including the last elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length equals or greater than the
     *         actual size of this collection.
     */
    default IntSet takeLast(int length) {
        final int size = size();
        return (size == 0)? this : (length == 0)? ImmutableIntArraySet.empty() : slice(new ImmutableIntRange(size - length, size - 1));
    }

    /**
     * Return an immutable set from the values contained in this set.
     * The same instance will be returned in case of being already immutable.
     */
    ImmutableIntSet toImmutable();

    /**
     * Return a new mutable set.
     * This method will always generate a new instance in order to avoid affecting the state of its original set.
     */
    MutableIntSet mutate();

    /**
     * Check if 2 set instances contain equivalent elements.
     *
     * @param set set to be compared with this instance.
     * @return whether the given set contains equivalent values to this one.
     */
    default boolean equalSet(IntSet set) {
        if (size() != set.size()) {
            return false;
        }

        for (int key : this) {
            if (!set.contains(key)) {
                return false;
            }
        }

        return true;
    }

    interface Builder extends IntTransformableBuilder {
        @Override
        Builder add(int value);

        @Override
        IntSet build();
    }
}

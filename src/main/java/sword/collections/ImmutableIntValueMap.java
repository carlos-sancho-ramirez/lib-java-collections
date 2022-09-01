package sword.collections;

import sword.annotations.ToBeAbstract;

/**
 * Immutable version of a Map where values are integer values.
 *
 * This Map is immutable, that means that its content cannot be modified once
 * it is created. This also means that, as no algorithms to insert, modify and
 * remove are required, its memory layout can be simplified and its footprint
 * can be reduced in a more optimal way.
 *
 * @param <T> Type for the key elements within the Map
 */
public interface ImmutableIntValueMap<T> extends IntValueMap<T>, ImmutableIntTransformable {

    @Override
    ImmutableSet<T> keySet();

    @Override
    ImmutableSet<Entry<T>> entries();

    ImmutableIntValueMap<T> put(T key, int value);

    /**
     * Creates a new map containing all the current elements and the ones given in the map.
     *
     * As this is a map, duplicated keys will not be allowed.
     * Than means that elements within the given map will replace any value in this map if
     * there is an equivalent key already included in this map.
     *
     * @param other Map from where new items will be added.
     */
    default ImmutableIntValueMap<T> putAll(IntValueMap<? extends T> other) {
        ImmutableIntValueMap<T> result = this;
        for (IntValueMap.Entry<? extends T> entry : other.entries()) {
            result = result.put(entry.key(), entry.value());
        }

        return result;
    }

    @Override
    MutableIntValueMap<T> mutate();

    @Override
    ImmutableIntValueMap<T> filter(IntPredicate predicate);

    @Override
    default ImmutableIntValueMap<T> filterNot(IntPredicate predicate) {
        return filter(v -> !predicate.apply(v));
    }

    @Override
    @ToBeAbstract("This implementation is unable to provide the proper map type. For example, sorted maps will always receive a hash map as response, which is not suitable")
    default ImmutableIntValueMap<T> filterByKey(Predicate<? super T> predicate) {
        return (ImmutableIntValueMap<T>) IntValueMap.super.filterByKey(predicate);
    }

    @Override
    @ToBeAbstract("This implementation is unable to provide the proper map type. For example, sorted maps will always receive a hash map as response, which is not suitable")
    default ImmutableIntValueMap<T> filterByEntry(Predicate<IntValueMapEntry<T>> predicate) {
        return (ImmutableIntValueMap<T>) IntValueMap.super.filterByEntry(predicate);
    }

    @Override
    ImmutableIntValueMap<T> mapToInt(IntToIntFunction mapFunc);

    @Override
    <U> ImmutableMap<T, U> map(IntFunction<? extends U> mapFunc);

    @Override
    ImmutableIntValueMap<T> sort(SortFunction<? super T> function);

    @Override
    ImmutableIntValueMap<T> removeAt(int index);

    ImmutableIntKeyMap<T> invert();

    @Override
    @ToBeAbstract("Unable to return the proper type. So the iteration order may be altered")
    default ImmutableIntValueMap<T> slice(ImmutableIntRange range) {
        final int size = size();
        if (size == 0) {
            return this;
        }

        final int min = range.min();
        final int max = range.max();
        if (min >= size || max < 0) {
            return ImmutableIntValueHashMap.empty();
        }

        if (min <= 0 && max >= size - 1) {
            return this;
        }

        final ImmutableIntValueMap.Builder<T> builder = new ImmutableIntValueHashMap.Builder<>();
        final int maxPosition = Math.min(max, size - 1);
        for (int position = min; position <= maxPosition; position++) {
            builder.put(keyAt(position), valueAt(position));
        }

        return builder.build();
    }

    @Override
    default ImmutableIntValueMap<T> skip(int length) {
        return slice(new ImmutableIntRange(length, Integer.MAX_VALUE));
    }

    /**
     * Returns a new ImmutableIntValueMap where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of this map in iteration order.
     * @return A new ImmutableIntValueMap instance just including the first elements,
     *         this instance if it is already empty,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length is equal or greater than the
     *         actual size of this collection.
     */
    @Override
    @ToBeAbstract("Returned type is wrong for sorted maps")
    default ImmutableIntValueMap<T> take(int length) {
        return isEmpty()? this :
                (length == 0)? ImmutableIntValueHashMap.empty() :
                    slice(new ImmutableIntRange(0, length - 1));
    }

    /**
     * Returns a new ImmutableIntValueMap where the <code>length</code> amount of last
     * elements has been removed.
     * <p>
     * This will return an empty map if the given parameter matches or exceeds
     * the length of this collection.
     *
     * @param length the amount of elements to be removed from the end of the map.
     * @return A new ImmutableIntValueMap instance without the last elements,
     *         the same instance in case the given length is 0 or this map is already empty,
     *         or the empty instance if the given length is equal or greater
     *         than the actual length of the set.
     */
    @ToBeAbstract("Unable to provide the proper type. If it was a sorted map, it sortFunction is lost")
    default ImmutableIntValueMap<T> skipLast(int length) {
        final int size = size();
        if (size == 0) {
            return this;
        }

        final int max = size - length - 1;
        return (max < 0)? ImmutableIntValueHashMap.empty() : slice(new ImmutableIntRange(0, max));
    }

    interface Builder<E> extends IntValueMap.Builder<E> {
        Builder<E> put(E key, int value);
        ImmutableIntValueMap<E> build();
    }
}

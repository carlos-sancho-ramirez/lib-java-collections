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

    interface Builder<E> extends IntValueMap.Builder<E> {
        Builder<E> put(E key, int value);
        ImmutableIntValueMap<E> build();
    }
}

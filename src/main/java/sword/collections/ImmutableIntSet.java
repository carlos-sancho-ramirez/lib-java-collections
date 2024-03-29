package sword.collections;

/**
 * Base for efficient implementations for immutable sets when there are few values included.
 * 'Set' must be understood as a collection where its elements cannot be repeated.
 *
 * This Set is immutable, that means that its content cannot be modified once
 * it is created. This also means that, as no algorithms to insert, modify and
 * remove are required, its memory layout can be simplified and its footprint
 * can be reduced in a more optimal way.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should create a builder in order to obtain
 * an instance of it.
 *
 * This class also implements the {@link Iterable} interface, which
 * ensures that the for-each construction can be used.
 */
public interface ImmutableIntSet extends IntSet, ImmutableIntTransformable {

    @Override
    ImmutableIntSet filter(IntPredicate predicate);

    @Override
    ImmutableIntSet filterNot(IntPredicate predicate);

    @Override
    ImmutableIntList mapToInt(IntToIntFunction func);

    @Override
    <U> ImmutableList<U> map(IntFunction<? extends U> func);

    @Override
    <V> ImmutableIntKeyMap<V> assign(IntFunction<? extends V> function);

    @Override
    ImmutableIntPairMap assignToInt(IntToIntFunction function);

    /**
     * Add the given value to the values of the current set if not
     * included yet.
     *
     * As this class is immutable, this method do not affect in the
     * current values of this set, but a new set is returned instead.
     *
     * @param value Value to be added.
     * @return A new set containing all elements included in this set
     *         plus the one given, or this same instance if the value
     *         was already present.
     */
    ImmutableIntSet add(int value);

    /**
     * Compose a new set including all current values within this set
     * and all values from the given collection.
     *
     * As this class is immutable, this method do not affect in the
     * current values of this set, but a new set is returned instead.
     *
     * Note that repeated values will be ignored. Thus, the resulting
     * set will have a length smaller or equal than the sum of the
     * lengths of both collections.
     * @param values Values to be added to the ones of this set.
     * @return A new set containing all elements included in this set
     *         plus the given ones, or this same instance if the
     *         values were already present.
     */
    default ImmutableIntSet addAll(Iterable<Integer> values) {
        final MutableIntSet set = mutate();
        return set.addAll(values)? set.toImmutable() : this;
    }

    @Override
    ImmutableIntSet removeAt(int index);

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
    ImmutableIntSet remove(int value);

    @Override
    ImmutableIntList toList();

    /**
     * Composes a new map traversing this set, applying the given function to each item.
     *
     * This method will compose a new set for all items that the given function does
     * return an equivalent value. The resulting set will be the value within the new map,
     * and the returned value will be the key within the map for that set.
     *
     * Example:
     * Set(1,2,3,4,5) grouped by func (item % 2) will create Map(0 -&gt; Set(2,4), 1 -&gt; Set(1,3,5))
     *
     * @param function Function to be applied to each item within the set to determine its group.
     * @param <K> Type for the new key within the returned map.
     * @return A new map where items have been grouped into different set according with the function given.
     */
    default <K> ImmutableMap<K, ImmutableIntSet> groupBy(IntFunction<K> function) {
        final MutableMap<K, MutableIntArraySet> map = MutableHashMap.empty();
        for (int value : this) {
            final K group = function.apply(value);
            MutableIntArraySet set = map.get(group, null);
            if (set == null) {
                set = MutableIntArraySet.empty();
                map.put(group, set);
            }

            set.add(value);
        }

        if (map.size() == 1) {
            return new ImmutableHashMap.Builder<K, ImmutableIntSet>()
                    .put(map.keyAt(0), this)
                    .build();
        }

        final int mapLength = map.size();
        final ImmutableHashMap.Builder<K, ImmutableIntSet> builder = new ImmutableHashMap.Builder<>();
        for (int i = 0; i < mapLength; i++) {
            builder.put(map.keyAt(i), map.valueAt(i).toImmutable());
        }

        return builder.build();
    }

    /**
     * Composes a new map traversing this set, applying the given function to each item.
     *
     * This method will compose a new set for all items that the given function does
     * return the same integer value. The resulting set will be the value within the new map,
     * and the returned value will be the key within the map for that set.
     *
     * Example:
     * Set(1,2,3,4,5) grouped by func (item % 2) will create Map(0 -&gt; Set(2,4), 1 -&gt; Set(1,3,5))
     *
     * @param function Function to be applied to each item within the set to determine its group.
     * @return A new map where items have been grouped into different set according with the function given.
     */
    default ImmutableIntKeyMap<ImmutableIntSet> groupByInt(IntToIntFunction function) {
        final MutableIntKeyMap<MutableIntArraySet> map = MutableIntKeyMap.empty();
        for (int value : this) {
            final int group = function.apply(value);
            MutableIntArraySet set = map.get(group, null);
            if (set == null) {
                set = MutableIntArraySet.empty();
                map.put(group, set);
            }

            set.add(value);
        }

        if (map.size() == 1) {
            return new ImmutableIntKeyMap.Builder<ImmutableIntSet>()
                    .put(map.keyAt(0), this)
                    .build();
        }

        final int mapLength = map.size();
        final ImmutableIntKeyMap.Builder<ImmutableIntSet> builder = new ImmutableIntKeyMap.Builder<>();
        for (int i = 0; i < mapLength; i++) {
            builder.put(map.keyAt(i), map.valueAt(i).toImmutable());
        }

        return builder.build();
    }

    @Override
    default ImmutableIntSet slice(ImmutableIntRange range) {
        final int size = size();
        final int min = range.min();
        final int max = range.max();
        if (size == 0 || min <= 0 && max >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableIntArraySet.empty();
        }

        final IntTraverser it = iterator();
        it.skip(min);
        final ImmutableIntSetCreator builder = new ImmutableIntSetCreator();
        for (int i = min; i <= max && it.hasNext(); i++) {
            builder.add(it.next());
        }

        return builder.build();
    }

    /**
     * Returns a new ImmutableIntSet where the <code>length</code>
     * amount of first elements in iteration order has been removed.
     * <p>
     * This will return an empty set if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the start of the set.
     * @return A new ImmutableIntSet instance without the first elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance of the given length is equal or greater
     *         than the actual length of the list.
     */
    @Override
    default ImmutableIntSet skip(int length) {
        return slice(new ImmutableIntRange(length, Integer.MAX_VALUE));
    }

    /**
     * Returns a new ImmutableIntSet where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of the set in iteration order.
     * @return A new ImmutableIntSet instance just including the first elements,
     *         this same instance if it is already empty,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length is equal or greater than the
     *         actual size of this set.
     */
    @Override
    default ImmutableIntSet take(int length) {
        return isEmpty()? this :
                (length == 0)? ImmutableIntArraySet.empty() :
                        slice(new ImmutableIntRange(0, length - 1));
    }

    /**
     * Returns a new ImmutableIntSet where the <code>length</code> amount of last
     * elements has been removed.
     * <p>
     * This will return an empty set if the given parameter matches or exceeds
     * the length of this collection.
     *
     * @param length the amount of elements to be removed from the end of the set.
     * @return A new ImmutableIntSet instance without the last elements,
     *         the same instance in case the given length is 0 or this set is already empty,
     *         or the empty instance if the given length is equal or greater
     *         than the actual length of the set.
     */
    @Override
    default ImmutableIntSet skipLast(int length) {
        final int size = size();
        if (size == 0) {
            return this;
        }

        final int max = size - length - 1;
        return (max < 0)? ImmutableIntArraySet.empty() : slice(new ImmutableIntRange(0, max));
    }

    /**
     * Returns a new ImmutableIntSet where only the <code>length</code> amount of
     * last elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the end of this set.
     * @return A new ImmutableIntSet instance just including the last elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length equals or greater than the
     *         actual size of this collection.
     */
    @Override
    default ImmutableIntSet takeLast(int length) {
        final int size = size();
        return (size == 0)? this : (length == 0)? ImmutableIntArraySet.empty() : slice(new ImmutableIntRange(size - length, size - 1));
    }

    /**
     * Builder to create a new instance of an {@link ImmutableIntSet}.
     */
    interface Builder extends IntSet.Builder, ImmutableIntTransformableBuilder {

        /**
         * Includes a new value for the new set.
         * @param value Value to be added
         * @return The same instance of this builder.
         */
        Builder add(int value);

        /**
         * Builds the ImmutableIntSet with all given values given so far.
         *
         * After calling this method, builder instance should not be reused and
         * should be discarded to be garbage collected. Not doing it may affect
         * the immutability of the set generated.
         *
         * @return The ImmutableIntSet containing all values included so far.
         */
        ImmutableIntSet build();
    }
}

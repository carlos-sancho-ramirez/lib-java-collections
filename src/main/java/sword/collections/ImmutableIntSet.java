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
public interface ImmutableIntSet extends IntSet, IterableImmutableIntCollection {

    @Override
    ImmutableIntSet filter(IntPredicate predicate);

    @Override
    ImmutableIntSet filterNot(IntPredicate predicate);

    @Override
    ImmutableIntSet map(IntToIntFunction func);

    @Override
    <U> ImmutableHashSet<U> map(IntFunction<U> func);

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

    /**
     * Applies the given function to each value on this set and composes
     * a new {@link ImmutableIntKeyMap}
     * where original values of this set becomes the keys and the result
     * of each function executing becomes the values.
     *
     * @param function the function used to generate all values.
     * @param <E> Type for the values in the new {@link ImmutableIntKeyMap}
     */
    <E> ImmutableIntKeyMap<E> mapTo(IntFunction<E> function);

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
        final MutableMap<K, MutableIntSet> map = MutableMap.empty();
        for (int value : this) {
            final K group = function.apply(value);
            MutableIntSet set = map.get(group, null);
            if (set == null) {
                set = MutableIntSet.empty();
                map.put(group, set);
            }

            set.add(value);
        }

        if (map.size() == 1) {
            return new ImmutableMap.Builder<K, ImmutableIntSet>()
                    .put(map.keyAt(0), this)
                    .build();
        }

        final int mapLength = map.size();
        final ImmutableMap.Builder<K, ImmutableIntSet> builder = new ImmutableMap.Builder<>();
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
        final MutableIntKeyMap<MutableIntSet> map = MutableIntKeyMap.empty();
        for (int value : this) {
            final int group = function.apply(value);
            MutableIntSet set = map.get(group, null);
            if (set == null) {
                set = MutableIntSet.empty();
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

    /**
     * Builder to create a new instance of an {@link ImmutableIntSet}.
     */
    interface Builder extends ImmutableIntCollectionBuilder<ImmutableIntSet> {

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

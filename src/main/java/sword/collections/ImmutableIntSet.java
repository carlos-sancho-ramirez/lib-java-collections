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
     * Add the given value from the current set if not included yet.
     *
     * As this class is immutable, this method do not affect in the
     * current values of this set, but a new set is returned instead.
     *
     * @param value Value to be removed.
     * @return A new set containing all elements included in this set
     *         plus the one given, or this same instance if the value
     *         was already present.
     */
    ImmutableIntSet add(int value);

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

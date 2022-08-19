package sword.collections;

public interface IntList extends IntTransformable {

    /**
     * Return the value assigned to the given key.
     * @throws UnmappedKeyException if the given key is not found within the map.
     */
    int get(int key);

    /**
     * Return the value assigned to the given key.
     * Or the given defaultValue if that key is not in the map.
     */
    int get(int key, int defaultValue);

    @Override
    default IntList filter(IntPredicate predicate) {
        return iterator().filter(predicate).toList();
    }

    @Override
    default IntList filterNot(IntPredicate predicate) {
        return iterator().filterNot(predicate).toList();
    }

    @Override
    default IntList mapToInt(IntToIntFunction func) {
        return iterator().mapToInt(func).toList();
    }

    @Override
    default <U> List<U> map(IntFunction<? extends U> func) {
        return iterator().<U>map(func).toList();
    }

    @Override
    default IntList slice(ImmutableIntRange range) {
        final int size = size();
        final int min = range.min();
        final int max = range.max();
        if (range.min() <= 0 && range.max() >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableIntList.empty();
        }

        final int newSize = Math.min(size - 1, max) - min + 1;
        final int[] newValues = new int[newSize];
        for (int i = 0; i < newSize; i++) {
            newValues[i] = valueAt(min + i);
        }

        return new ImmutableIntList(newValues);
    }

    /**
     * Returns a new IntList where the <code>length</code>
     * amount of first elements has been removed.
     * <p>
     * This will return an empty list if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the start of the list.
     * @return A new IntList instance without the first elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance if the given length is equal or greater
     *         than the actual length of the list.
     */
    @Override
    default IntList skip(int length) {
        return slice(new ImmutableIntRange(length, Integer.MAX_VALUE));
    }

    /**
     * Returns a new IntList where only the <code>length</code> amount of
     * first elements are included, and the rest are discarded if any.
     *
     * @param length the maximum number of elements to be included from the
     *               start of the list.
     * @return A new IntList instance just including the first elements,
     *         or the empty instance in case the given length is 0.
     */
    default IntList take(int length) {
        return (length == 0)? ImmutableIntList.empty() : slice(new ImmutableIntRange(0, length - 1));
    }

    /**
     * Return an immutable list from the values contained in this map.
     * The same instance will be returned in case of being already immutable.
     */
    ImmutableIntList toImmutable();

    /**
     * Return a new mutable list.
     * This method will always generate a new instance in order to avoid affecting the state of its original collection.
     */
    MutableIntList mutate();

    /**
     * Return a new mutable list with the given {@link ArrayLengthFunction}.
     * This method will always generate a new instance in order to avoid affecting the state of its original collection.
     */
    MutableIntList mutate(ArrayLengthFunction arrayLengthFunction);
}

package sword.collections;

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
        if (range.min() <= 0 && range.max() >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableIntArraySet.empty();
        }

        final int newSize = Math.min(size, max + 1) - min;
        final ImmutableIntSetCreator builder = new ImmutableIntSetCreator((currentSize, nS) -> newSize);
        for (int i = 0; i < newSize; i++) {
            builder.add(valueAt(min + i));
        }

        return builder.build();
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

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

    default <U> List<U> map(IntFunction<U> func) {
        return iterator().map(func).toList();
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

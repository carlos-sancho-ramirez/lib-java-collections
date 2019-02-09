package sword.collections;

public interface IntSet extends IntTransformable, Sizable {

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
     * Value in the given index position.
     *
     * @param index Index within the array of values, valid indexes goes from 0 to {@link #size()} - 1
     * @return The value in the given position.
     */
    int valueAt(int index);

    /**
     * Converts this set to a list.
     *
     * The order of iteration is guaranteed to be the same in the new list.
     */
    IntList toList();

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
}

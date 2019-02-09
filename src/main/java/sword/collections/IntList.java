package sword.collections;

public interface IntList extends IntTransformable, Sizable {

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
    default <U> List<U> map(IntFunction<U> func) {
        return iterator().map(func).toList();
    }

    /**
     * Converts this list into a set.
     *
     * All duplicated elements within the list will be removed as sets does not allow duplicating values.
     * Because of that, the amount of elements in the resulting set may be less
     * than the amount of elements in the original list, but never more.
     *
     * The iteration order of elements in the resulting set is not guaranteed
     * to be the same that was in the list even if no elements are removed for duplication.
     */
    IntSet toSet();

    /**
     * Return an immutable list from the values contained in this map.
     * The same instance will be returned in case of being already immutable.
     */
    ImmutableIntList toImmutable();
}

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

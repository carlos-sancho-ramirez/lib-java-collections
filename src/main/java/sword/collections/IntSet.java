package sword.collections;

public interface IntSet extends IterableIntCollection, Sizable {

    /**
     * Return the minimum value within the set.
     * @throws EmptyCollectionException if collection is empty.
     */
    int min() throws EmptyCollectionException;

    /**
     * Return the maximum value within the set.
     * @throws EmptyCollectionException if collection is empty.
     */
    int max() throws EmptyCollectionException;

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

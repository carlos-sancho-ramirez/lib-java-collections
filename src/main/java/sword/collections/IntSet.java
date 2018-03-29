package sword.collections;

public interface IntSet extends IterableIntCollection, Sizable {

    /**
     * Value in the given index position.
     *
     * @param index Index within the array of values, valid indexes goes from 0 to {@link #size()} - 1
     * @return The value in the given position.
     */
    int valueAt(int index);

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

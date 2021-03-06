package sword.collections;

public interface IntTransformer extends IntTraverser {

    /**
     * Build a new list containing all elements given on traversing this collection.
     */
    IntList toList();

    /**
     * Build a new set containing all elements given on traversing this collection.
     * The result of this method will remove any duplicated integer value and will
     * be sorted in ascending order.
     */
    IntSet toSet();

    /**
     * Return all indexes from the current position ignoring the actual values of this collection.
     */
    IntTransformer indexes();

    /**
     * Composes a new IntTransformer that filters this one by applying the given predicate to the
     * elements within this collection.
     * @param predicate Only value returning true for the given this predicate will be present
     *                  in the resulting IntTransformer.
     */
    IntTransformer filter(IntPredicate predicate);

    /**
     * Composes a new IntTransformer that filters this one by applying the given predicate to the
     * elements within this collection and collecting all elements where the predicate does not hold.
     * @param predicate Only value returning false for the given this predicate will be present
     *                  in the resulting IntTransformer.
     */
    IntTransformer filterNot(IntPredicate predicate);

    /**
     * Applies the given function to each of the
     * values within this collection keeping its order.
     * @param mapFunc Function to be applied to each of the values within the collection.
     * @return A transformer that applies the given function just in time.
     */
    IntTransformer mapToInt(IntToIntFunction mapFunc);

    /**
     * Applies the given function to each of the
     * values within this collection keeping its order.
     * @param mapFunc Function to be applied to each of the values within the collection.
     * @return A transformer that applies the given function just in time.
     */
    <U> Transformer<U> map(IntFunction<? extends U> mapFunc);

    /**
     * Composes a map by traversing the collection and counting how many times an element is found.
     *
     * The resulting map will have the elements from this transformer as keys,
     * and the number of times that each element is found as its value.
     *
     * It is expected that the size of the resulting map will never be longer that the original one,
     * and the sum of all resulting values should match the original size. Note that the values within
     * the resulting map will never be negative nor 0.
     */
    IntPairMap count();
}

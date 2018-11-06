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
}

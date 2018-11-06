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
}

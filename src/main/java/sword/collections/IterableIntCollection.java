package sword.collections;

interface IterableIntCollection extends Iterable<Integer> {

    /**
     * Return true if the given value is found in the collection.
     * @param value Value to check
     */
    boolean contains(int value);
}

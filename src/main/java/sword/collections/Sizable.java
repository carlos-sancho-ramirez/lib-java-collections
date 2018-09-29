package sword.collections;

public interface Sizable {

    /**
     * Return the number of elements included in this collection.
     */
    int size();

    /**
     * Checks if there is any element included in this collection.
     * @return True if empty, false if any value is included.
     */
    default boolean isEmpty() {
        return size() == 0;
    }
}

package sword.collections;

public interface SortFunction<T> {

    /**
     * Returns true if the first element should be before the second one.
     *
     * Note that if both parameters are in the same sorting position, this should return false for any order of the operands.
     * @param a One of the elements to be sorted.
     * @param b Another element to be sorted.
     * @return True if a is considered to go before b. False otherwise.
     */
    boolean lessThan(T a, T b);
}

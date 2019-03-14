package sword.collections;

public interface MutableIntSet extends IntSet, MutableIntTransformable {

    /**
     * Include the given value within this set.
     * @param value Value to be included.
     * @return True if the value was not present and this operation modified
     *         this set, or false if it was already included.
     */
    boolean add(int value);

    /**
     * Include all given integer values into this set.
     * @param values Iterable collection holding all integer values
     *               to be included.
     * @return True if this operation made any change in this set,
     *         or false if all integers where already included.
     */
    default boolean addAll(Iterable<Integer> values) {
        boolean changed = false;
        for (int value : values) {
            if (add(value)) {
                changed = true;
            }
        }

        return changed;
    }

    interface Builder extends IntSet.Builder, MutableIntTransformableBuilder {
        @Override
        Builder add(int value);

        @Override
        MutableIntSet build();
    }
}

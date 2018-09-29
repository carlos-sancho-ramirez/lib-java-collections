package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

public interface Traverser<T> extends Iterator<T> {

    /**
     * Return true if an equivalent item is found in the collection, this means
     * that it will be true if calling {@link Object#equals(Object)} with
     * this value returns true on any of the elements.
     *
     * @param value Value to check
     */
    default boolean contains(T value) {
        while (hasNext()) {
            if (equal(next(), value)) {
                return true;
            }
        }

        return false;
    }
}

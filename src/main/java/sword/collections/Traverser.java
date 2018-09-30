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

    /**
     * Returns true if the given predicate returns true for any of the items
     * in this collection.
     *
     * @param predicate Predicate to be evaluated.
     */
    default boolean anyMatch(Predicate<T> predicate) {
        while (hasNext()) {
            if (predicate.apply(next())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the index from the current Traverser position for the first element matching the given value.
     * Or -1 if none matches.
     * @param value Value to be matched. {@link java.lang.Object#equals(Object)} will be called for this purpose.
     */
    default int indexOf(T value) {
        for (int index = 0; hasNext(); index++) {
            if (equal(value, next())) {
                return index;
            }
        }

        return -1;
    }

    /**
     * Return the value in the given index position.
     *
     * @param index Index from the current position of the Traverser.
     *              It must be zero or a positive number that must be below the number of elements
     *              returned when this collection is iterated.
     * @return The value in the given position.
     * @throws IndexOutOfBoundsException if the given index is invalid for this Traverser.
     */
    default T valueAt(int index) {
        for (int i = 0; hasNext(); i++) {
            final T value = next();
            if (i == index) {
                return value;
            }
        }

        throw new IndexOutOfBoundsException();
    }
}

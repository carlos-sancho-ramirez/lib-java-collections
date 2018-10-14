package sword.collections;

import java.util.Iterator;

public interface IntTraverser extends Iterator<Integer> {

    /**
     * Return true if the given value is found in the collection.
     * @param value Value to check
     */
    default boolean contains(int value) {
        while (hasNext()) {
            if (value == next()) {
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
    default boolean anyMatch(IntPredicate predicate) {
        while (hasNext()) {
            if (predicate.apply(next())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the index from the current Traverser position from the first element matching the given value.
     * Or -1 if none matches.
     * @param value Value to be matched. {@link java.lang.Object#equals(Object)} will be called for this purpose.
     */
    default int indexOf(int value) {
        for (int i = 0; hasNext(); i++) {
            if (value == next()) {
                return i;
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
    default int valueAt(int index) {
        if (index >= 0) {
            for (int i = 0; hasNext(); i++) {
                final int value = next();
                if (i == index) {
                    return value;
                }
            }
        }

        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the first item matching the predicate or the default value if none matches.
     */
    default int findFirst(IntPredicate predicate, int defaultValue) {
        while (hasNext()) {
            final int value = next();
            if (predicate.apply(value)) {
                return value;
            }
        }

        return defaultValue;
    }

    /**
     * Traverses the whole collection reducing it to a single element by applying the given function
     * on each pair of values from start to end.
     * @param func Associate function to be applied on each pair of elements.
     * @return The resulting value of applying the given function to each value pair.
     * @throws EmptyCollectionException in case the collection is empty.
     */
    default int reduce(IntReduceFunction func) throws EmptyCollectionException {
        if (!hasNext()) {
            throw new EmptyCollectionException();
        }

        int value = next();
        while (hasNext()) {
            value = func.apply(value, next());
        }

        return value;
    }

    /**
     * Traverses the whole collection reducing it to a single element by applying the given function
     * on each pair of values from start to end, or return the default value if the collection is empty.
     * @param func Associate function to be applied on each pair of elements.
     * @return The resulting value of applying the given function to each value pair, or the default value if empty.
     */
    default int reduce(IntReduceFunction func, int defaultValue) {
        if (!hasNext()) {
            return defaultValue;
        }

        int value = next();
        while (hasNext()) {
            value = func.apply(value, next());
        }

        return value;
    }

    /**
     * Traverses the whole collection and returns the minimum value found on it.
     * @throws EmptyCollectionException is {@link IntTraverser} already reached the end.
     */
    default int min() throws EmptyCollectionException {
        if (!hasNext()) {
            throw new EmptyCollectionException();
        }

        int result = next();
        while (hasNext()) {
            final int value = next();
            if (value < result) {
                result = value;
            }
        }

        return result;
    }

    /**
     * Traverses the whole collection and returns the maximum value found on it.
     * @throws EmptyCollectionException is {@link IntTraverser} already reached the end.
     */
    default int max() throws EmptyCollectionException {
        if (!hasNext()) {
            throw new EmptyCollectionException();
        }

        int result = next();
        while (hasNext()) {
            final int value = next();
            if (value > result) {
                result = value;
            }
        }

        return result;
    }
}

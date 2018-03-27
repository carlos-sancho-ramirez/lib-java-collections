package sword.collections;

final class SortUtils {

    /**
     * Internal value used as hashcode for null key, as null key is allowed.
     */
    static final int HASH_FOR_NULL = 0;

    interface SwapMethod {
        void apply(int index1, int index2);
    }

    static void quickSort(int[] array, int leftIndex, int rightIndex, SwapMethod method) {
        final int pivot = array[rightIndex];

        int left = leftIndex;
        int right = rightIndex - 1;

        do {
            while (array[left] < pivot) {
                left++;
            }

            while (right > 0 && array[right] > pivot) {
                right--;
            }

            if (left < right) {
                method.apply(left, right);
            }
        }
        while (left < right);

        if (left != rightIndex) {
            method.apply(left, rightIndex);
        }

        if (left - leftIndex >= 2) {
            quickSort(array, leftIndex, left - 1, method);
        }

        if (rightIndex - left >= 2) {
            quickSort(array, left + 1, rightIndex, method);
        }
    }

    /**
     * Return in which index the given key is located, or -1 if not found.
     */
    static int findKey(int[] keys, int length, int key) {
        int max = length;
        int min = 0;

        while (max > min) {
            int middle = min + (max - min) / 2;
            int keyInMiddle = keys[middle];

            if (keyInMiddle == key) {
                return middle;
            }
            else if (keyInMiddle < key) {
                min = middle + 1;
            }
            else {
                max = middle;
            }
        }

        return -1;
    }

    static <E> int findKey(int[] hashCodes, E[] keys, int length, E key) {
        final int hashCode = (key != null)? key.hashCode() : HASH_FOR_NULL;

        final int hashIndex = findKey(hashCodes, length, hashCode);
        if (hashIndex >= 0) {
            // Hashes can be repeated, but duplicates should be just before or after. Let's check first if there is
            // any in the given position, and if not let's keep decreasing
            int index = hashIndex;
            while (index >= 0 && hashCodes[index] == hashCode) {
                if (equal(key, keys[index])) {
                    return index;
                }

                --index;
            }

            // If not found, let's try increasing from hashIndex
            index = hashIndex + 1;
            while (index < length && hashCodes[index] == hashCode) {
                if (equal(key, keys[index])) {
                    return index;
                }

                ++index;
            }
        }

        return -1;
    }

    /**
     * Return the index where the given key should be inserted in order to keep having a sorted set
     * of keys.
     */
    static int findSuitableIndex(int[] keys, int length, int key) {
        int max = length;
        int min = 0;

        while (max > min) {
            int middle = min + (max - min) / 2;
            int keyInMiddle = keys[middle];

            if (keyInMiddle < key) {
                min = middle + 1;
            }
            else {
                max = middle;
            }
        }

        return min;
    }

    static int indexOf(Object[] values, int size, Object value) {
        for (int i = 0; i < size; i++) {
            final Object item = values[i];
            if (value == null && item == null || value != null && value.equals(item)) {
                return i;
            }
        }

        return -1;
    }

    static <E> E findFirst(Object[] values, int length, Predicate<E> predicate, E defaultValue) {
        for (int i = 0; i < length; i++) {
            final E item = (E) values[i];
            if (predicate.apply(item)) {
                return item;
            }
        }

        return defaultValue;
    }

    static boolean equal(Object a, Object b) {
        return a == b || a != null && a.equals(b);
    }

    private SortUtils() {
    }
}

package sword.collections;

public final class SortUtils {

    static final int DEFAULT_GRANULARITY = 4;

    /**
     * Internal value used as hashcode for null key, as null key is allowed.
     */
    private static final int HASH_FOR_NULL = 0;

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
        final int hashCode = hashCode(key);
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

    static <E> int findValue(SortFunction<E> sortFunction, Object[] keys, int length, E key) {
        if (length == 0) {
            return -1;
        }

        int min = -1;
        int max = length - 1;
        while (min < max) {
            final int pos = (max - min + 1) / 2 + min;
            final E element = (E) keys[pos];
            if (sortFunction.lessThan(element, key)) {
                min = pos;
            }
            else {
                max = pos - 1;
            }
        }

        ++min;
        while (min < length && !sortFunction.lessThan(key, (E) keys[min])) {
            if (equal(key, keys[min])) {
                return min;
            }
            ++min;
        }

        return -1;
    }

    static <E> int findSuitableIndex(SortFunction<E> sortFunction, Object[] values, int length, E value) {
        if (length == 0) {
            return 0;
        }

        int min = -1;
        int max = length - 1;
        while (min < max) {
            final int pos = (max - min + 1) / 2 + min;
            final E element = (E) values[pos];
            if (sortFunction.lessThan(value, element)) {
                max = pos - 1;
            }
            else {
                min = pos;
            }
        }

        return min + 1;
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

    static boolean equal(Object a, Object b) {
        return a == b || a != null && a.equals(b);
    }

    static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    static int hashCode(Object object) {
        return (object != null)? object.hashCode() : HASH_FOR_NULL;
    }

    /**
     * Compare 2 char sequences and returns true if the first is expected to come before the second one.
     * This method compares the given sequences from first character and keeps going to the
     * following to the next character until a mismatch is found.
     * Comparison between characters is done by unicode, which means among other things that
     * numbers come first 0-9, followed by uppercase character A-Z and then lowercase ones a-z.
     *
     * @param a First char sequence to be compared
     * @param b Second char sequence to be compared.
     * @return True only if the first is expected to go before the second.
     * Note that will be false even if they are equal.
     */
    public static boolean compareCharSequenceByUnicode(CharSequence a, CharSequence b) {
        if (a == null) {
            return b != null;
        }

        if (b == null) {
            return false;
        }

        final int aLength = a.length();
        final int bLength = b.length();
        for (int i = 0; i < aLength; i++) {
            if (i >= bLength) {
                return false;
            }

            final char aChar = a.charAt(i);
            final char bChar = b.charAt(i);
            if (aChar != bChar) {
                return aChar < bChar;
            }
        }

        return aLength < bLength;
    }

    private SortUtils() {
    }
}

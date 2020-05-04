package sword.collections;

/**
 * Function that determines the suitable array length to hold a given number of items.
 *
 * Due to performance issues, it is not a good idea to always match the number
 * of elements in a collection with the actual length of the backed array.
 * If so, any time a new item is removed or added, a new array should be
 * created and all items should be copied between the arrays, could take lot of
 * time for really long collections, and also lead to fragmentation memory
 * issues in a long run.
 *
 * However, taking a really long array in memory to just keep a small number of
 * items is also a waste of memory.
 *
 * Generic use collection cannot determine the best array length, and sometimes
 * it is valuable that the logic using this collections, that may know better
 * the data that it is manipulating, can determinate the length of the backed
 * array.
 *
 * This function is designed to let the logic using the collection set which is
 * the most accurate array length and stick to it.
 */
public interface ArrayLengthFunction {
    int suitableArrayLength(int currentSize, int newSize);
}

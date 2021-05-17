package sword.collections;

/**
 * Mutable version of a Map with integer values.
 *
 * This Map is mutable, that means that, in contrast with the immutable version,
 * algorithms to insert, modify and remove are required. This may affect its
 * internal memory footprint to ensure easy modification.
 *
 * Implementation of this interfaces will assume that keys inserted are immutable.
 * Mutation of the contained keys may result in duplicates within
 * the keys or wrong sorting of keys.
 * It is not guaranteed to work if keys are mutable.
 * This does no apply to values of the map, that can mutate without risk.
 *
 * @param <T> Type for the key elements within the Map
 */
public interface MutableIntValueMap<T> extends IntValueMap<T>, MutableIntTransformable {

    boolean put(T key, int value);

    /**
     * Create a new mutable map instance and copy from this collection the actual data references. After it, this collection gets cleared.
     * <p>
     * This is a more efficient alternative to the following code:
     * <code>
     * <br>MutableIntValueMap newMap = map.mutate();
     * <br>map.clear();
     * </code>
     *
     * @return A new mutable map that contains the actual data of this map.
     */
    default MutableIntValueMap<T> donate() {
        final MutableIntValueMap<T> mutated = mutate();
        clear();
        return mutated;
    }

    interface Builder<E> extends IntValueMap.Builder<E> {

        @Override
        Builder<E> put(E key, int value);

        @Override
        MutableIntValueMap<E> build();
    }
}

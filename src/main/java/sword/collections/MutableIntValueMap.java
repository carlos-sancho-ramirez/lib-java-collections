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

    interface Builder<E> extends IntValueMap.Builder<E> {

        @Override
        Builder<E> put(E key, int value);

        @Override
        MutableIntValueMap<E> build();
    }
}

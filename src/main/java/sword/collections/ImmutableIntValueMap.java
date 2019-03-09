package sword.collections;

/**
 * Immutable version of a Map where values are integer values.
 *
 * This Map is immutable, that means that its content cannot be modified once
 * it is created. This also means that, as no algorithms to insert, modify and
 * remove are required, its memory layout can be simplified and its footprint
 * can be reduced in a more optimal way.
 *
 * @param <T> Type for the key elements within the Map
 */
public interface ImmutableIntValueMap<T> extends IntValueMap<T>, ImmutableIntTransformable {

    @Override
    ImmutableSet<T> keySet();

    @Override
    ImmutableSet<Entry<T>> entries();

    @Override
    MutableIntValueMap<T> mutate();

    @Override
    ImmutableIntValueMap<T> filter(IntPredicate predicate);

    @Override
    ImmutableIntValueMap<T> filterNot(IntPredicate predicate);

    @Override
    ImmutableIntValueMap<T> mapToInt(IntToIntFunction mapFunc);

    @Override
    <U> ImmutableMap<T, U> map(IntFunction<U> mapFunc);

    @Override
    ImmutableIntValueMap<T> sort(SortFunction<T> function);

    ImmutableIntKeyMap<T> invert();

    interface Builder<E> extends IntValueMap.Builder<E> {
        Builder<E> put(E key, int value);
        ImmutableIntValueMap<E> build();
    }
}

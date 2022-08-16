package sword.collections;

import sword.annotations.ToBeAbstract;
import sword.annotations.ToBeSubtyped;

public interface IntTransformable extends IntTraversable {
    @ToBeSubtyped(IntTransformerWithKey.class)
    IntTransformer iterator();

    /**
     * Converts collection to a list.
     *
     * The order of iteration is guaranteed to be the same in the new list.
     */
    default IntList toList() {
        return iterator().toList();
    }

    /**
     * Converts this collection into a set.
     *
     * All duplicated elements within the collection will be removed as sets does not allow duplicating values.
     * Because of that, the amount of elements in the resulting set may be less
     * than the amount of elements in the original list, but never more.
     *
     * The iteration order of elements in the resulting set is not guaranteed
     * to be the same that was in the collection even if no elements are removed for duplication.
     */
    default IntSet toSet() {
        return iterator().toSet();
    }

    /**
     * Return all indexes within this collection in ascending order.
     */
    default IntSet indexes() {
        return iterator().indexes().toSet();
    }

    /**
     * Applies the given predicate to each of the values within the set and
     * composes a new set including only the values whose predicate returned
     * true.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    IntTransformable filter(IntPredicate predicate);

    /**
     * Applies the given predicate to each of the values within the set and
     * composes a new set including only the values whose predicate returned
     * false.
     *
     * @param predicate To be applied to each element in order to filter.
     */
    default IntTransformable filterNot(IntPredicate predicate) {
        return filter(v -> !predicate.apply(v));
    }

    default IntTransformable mapToInt(IntToIntFunction func) {
        return iterator().mapToInt(func).toList();
    }

    default <U> Transformable<U> map(IntFunction<? extends U> func) {
        return iterator().<U>map(func).toList();
    }

    /**
     * Composes a map by traversing the collection and counting how many times an element is found.
     *
     * The resulting map will have the elements from the collection as keys,
     * and the number of times that each element is found as its value.
     *
     * It is expected that the size of the resulting map will never be longer that the original one,
     * and the sum of all resulting values should match the original size. Note that the values within
     * the resulting map will never be negative nor 0.
     */
    default IntPairMap count() {
        return iterator().count();
    }

    /**
     * Composes a new collection where the elements are extracted from this one
     * according to the positions given in the range.
     * <p>
     * The size of the resulting collection should be at most the size of the given
     * range. It can be less if the actual collection does not have enough elements.
     *
     * @param range Positions to be extracted from the original collection.
     *              Negative numbers are not expected.
     * @return A new collection where the elements are extracted from this collection.
     */
    @ToBeAbstract("This implementation is unable to return the proper transformable type")
    default IntTransformable slice(ImmutableIntRange range) {
        final int size = size();
        final int min = range.min();
        final int max = range.max();
        if (min <= 0 && max >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableIntList.empty();
        }

        final ImmutableIntList.Builder builder = new ImmutableIntList.Builder();
        for (int position = min; position <= max && position < size; position++) {
            builder.append(valueAt(position));
        }

        return builder.build();
    }
}

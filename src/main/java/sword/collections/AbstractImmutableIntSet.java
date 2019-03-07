package sword.collections;

import java.util.Iterator;

abstract class AbstractImmutableIntSet extends AbstractImmutableIntTransformable implements ImmutableIntSet {

    @Override
    <U> ImmutableTransformableBuilder<U> newBuilder() {
        return new ImmutableHashSet.Builder<>();
    }

    @Override
    ImmutableIntCollectionBuilder newIntBuilder() {
        return new ImmutableIntSetBuilder();
    }

    @Override
    public ImmutableIntSet filter(IntPredicate predicate) {
        return (ImmutableIntSet) super.filter(predicate);
    }

    @Override
    public ImmutableIntSet filterNot(IntPredicate predicate) {
        return (ImmutableIntSet) super.filterNot(predicate);
    }

    @Override
    public abstract ImmutableIntList mapToInt(IntToIntFunction func);

    @Override
    public abstract <U> ImmutableList<U> map(IntFunction<U> func);

    @Override
    public <E> ImmutableIntKeyMap<E> mapTo(IntFunction<E> function) {
        ImmutableIntKeyMap.Builder<E> builder = new ImmutableIntKeyMap.Builder<>();
        for (int key : this) {
            builder.put(key, function.apply(key));
        }

        return builder.build();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AbstractImmutableIntSet)) {
            return false;
        }

        final AbstractImmutableIntSet that = (AbstractImmutableIntSet) other;
        if (size() != that.size()) {
            return false;
        }

        final Iterator<Integer> thisIterator = iterator();
        final Iterator<Integer> thatIterator = that.iterator();
        while (thisIterator.hasNext()) {
            if (thisIterator.next().intValue() != thatIterator.next().intValue()) {
                return false;
            }
        }

        return true;
    }
}

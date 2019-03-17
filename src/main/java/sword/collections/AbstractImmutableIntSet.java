package sword.collections;

import java.util.Iterator;

abstract class AbstractImmutableIntSet extends AbstractImmutableIntTransformable implements ImmutableIntSet {

    @Override
    <U> ImmutableTransformableBuilder<U> newBuilder() {
        return new ImmutableHashSet.Builder<>();
    }

    @Override
    ImmutableIntTransformableBuilder newIntBuilder() {
        return new ImmutableIntSetCreator();
    }

    @Override
    public ImmutableIntSet toSet() {
        return this;
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
    public <E> ImmutableIntKeyMap<E> assign(IntFunction<E> function) {
        final int size = size();
        if (size == 0) {
            return ImmutableIntKeyMap.empty();
        }

        final int[] keys = new int[size];
        final Object[] values = new Object[size];

        final IntTraverser it = iterator();
        for (int i = 0; it.hasNext(); i++) {
            final int key = it.next();
            keys[i] = key;
            values[i] = function.apply(key);
        }

        return new ImmutableIntKeyMap<>(keys, values);
    }

    @Override
    public ImmutableIntPairMap assignToInt(IntToIntFunction function) {
        final int size = size();
        if (size == 0) {
            return ImmutableIntPairMap.empty();
        }

        final int[] keys = new int[size];
        final int[] values = new int[size];

        final IntTraverser it = iterator();
        for (int i = 0; i < size; i++) {
            final int key = it.next();
            keys[i] = key;
            values[i] = function.apply(key);
        }

        return new ImmutableIntPairMap(keys, values);
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

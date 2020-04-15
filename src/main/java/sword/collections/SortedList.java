package sword.collections;

import java.util.Iterator;

class SortedList<T> implements List<T> {

    private final List<T> _source;
    private final SortFunction<? super T> _sortFunction;

    SortedList(List<T> source, SortFunction<? super T> sortFunction) {
        _source = source;
        _sortFunction = sortFunction;
    }

    @Override
    public ImmutableList<T> toImmutable() {
        final int length = _source.size();
        if (length < 2) {
            return _source.toImmutable();
        }

        final Object[] newValues = new Object[length];
        final Iterator<T> it = _source.iterator();
        newValues[0] = it.next();
        boolean changed = false;

        for (int i = 1; i < length; i++) {
            final T value = it.next();
            final int index = SortUtils.findSuitableIndex(_sortFunction, newValues, i, value);
            changed |= index != i;
            for (int j = i; j > index; j--) {
                newValues[j] = newValues[j - 1];
            }
            newValues[index] = value;
        }

        return (changed || !(_source instanceof ImmutableList))? new ImmutableList<>(newValues) : _source.toImmutable();
    }

    @Override
    public MutableList<T> mutate() {
        final int length = _source.size();
        if (length < 2) {
            return _source.mutate();
        }

        final Object[] newValues = new Object[MutableList.suitableArrayLength(length)];
        final Iterator<T> it = _source.iterator();
        newValues[0] = it.next();

        for (int i = 1; i < length; i++) {
            final T value = it.next();
            final int index = SortUtils.findSuitableIndex(_sortFunction, newValues, i, value);
            for (int j = i; j > index; j--) {
                newValues[j] = newValues[j - 1];
            }
            newValues[index] = value;
        }

        return new MutableList<>(newValues, length);
    }

    @Override
    public Transformer<T> iterator() {
        return toImmutable().iterator();
    }
}

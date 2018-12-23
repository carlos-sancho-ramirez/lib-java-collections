package sword.collections;

import java.util.Arrays;

import static sword.collections.SortUtils.DEFAULT_GRANULARITY;
import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;

public final class MutableIntSet extends AbstractIntIterable implements IntSet, MutableIterableIntCollection {

    private static final int GRANULARITY = DEFAULT_GRANULARITY;

    public static MutableIntSet empty() {
        return new MutableIntSet(new int[GRANULARITY], 0);
    }

    private static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    private int _size;
    private int[] _values;

    private MutableIntSet(int[] values, int size) {
        _values = values;
        _size = size;
    }

    private void enlargeArrays() {
        int[] oldKeys = _values;
        _values = new int[_size + GRANULARITY];
        System.arraycopy(oldKeys, 0, _values, 0, _size);
    }

    @Override
    public int valueAt(int index) {
        return _values[index];
    }

    @Override
    public void removeAt(int index) throws IndexOutOfBoundsException {
        if (_size != 1 && (_size % GRANULARITY) == 1) {
            int[] oldValues = _values;
            _values = new int[--_size];

            if (index > 0) {
                System.arraycopy(oldValues, 0, _values, 0, index);
            }

            if (_size > index) {
                System.arraycopy(oldValues, index + 1, _values, index, _size - index);
            }
        }
        else {
            --_size;
            for (int i = index; i < _size; i++) {
                _values[i] = _values[i + 1];
            }
        }
    }

    @Override
    public boolean clear() {
        final boolean somethingRemoved = _size > 0;
        if (_size > GRANULARITY) {
            _values = new int[GRANULARITY];
        }

        _size = 0;
        return somethingRemoved;
    }

    /**
     * Include the given value within this set.
     * @param value Value to be included.
     * @return True if the value was not present and this operation modified
     *         this set, or false if it was already included.
     */
    public boolean add(int value) {
        int index = findKey(_values, _size, value);
        if (index < 0) {
            if (_size != 0 && _size % GRANULARITY == 0) {
                enlargeArrays();
            }

            index = findSuitableIndex(_values, _size, value);
            for (int i = _size; i > index; i--) {
                _values[i] = _values[i - 1];
            }

            _values[index] = value;
            _size++;
            return true;
        }

        return false;
    }

    /**
     * Include all given integer values into this set.
     * @param values Iterable collection holding all integer values
     *               to be included.
     * @return True if this operation made any change in this set,
     *         or false if all integers where already included.
     */
    public boolean addAll(Iterable<Integer> values) {
        boolean changed = false;
        for (int value : values) {
            changed |= add(value);
        }

        return changed;
    }

    public boolean remove(int value) {
        int index = findKey(_values, _size, value);
        if (index >= 0) {
            removeAt(index);
            return true;
        }

        return false;
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    public IntTraverser iterator() {
        return new Iterator();
    }

    @Override
    public int min() throws EmptyCollectionException {
        if (_size == 0) {
            throw new EmptyCollectionException();
        }

        return _values[0];
    }

    @Override
    public int max() throws EmptyCollectionException {
        if (_size == 0) {
            throw new EmptyCollectionException();
        }

        return _values[_size - 1];
    }

    @Override
    public IntList toList() {
        final int[] values = new int[_size];
        for (int i = 0; i < _size; i++) {
            values[i] = _values[i];
        }

        return new ImmutableIntList(values);
    }

    @Override
    public ImmutableIntSet toImmutable() {
        return ImmutableIntSetBuilder.fromMutableIntSet(this);
    }

    @Override
    public MutableIntSet mutate() {
        return fromIntSet(this);
    }

    private class Iterator implements IntTraverser {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Integer next() {
            return _values[_index++];
        }

        @Override
        public void remove() {
            removeAt(--_index);
        }
    }

    static MutableIntSet fromIntSet(IntSet set) {
        final MutableIntSet result = MutableIntSet.empty();
        for (int value : set) {
            result.add(value);
        }

        return result;
    }

    static MutableIntSet fromIntRange(ImmutableIntRange range) {
        final int size = range.size();
        final int[] values = new int[suitableArrayLength(size)];
        final int min = range.min();
        for (int i = 0; i < size; i++) {
            values[i] = min + i;
        }

        return new MutableIntSet(values, size);
    }

    public static class Builder implements IntCollectionBuilder<MutableIntSet> {
        private final MutableIntSet _set = MutableIntSet.empty();

        @Override
        public Builder add(int value) {
            _set.add(value);
            return this;
        }

        @Override
        public MutableIntSet build() {
            return _set;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof MutableIntSet)) {
            return super.equals(other);
        }

        final MutableIntSet that = (MutableIntSet) other;
        return Arrays.equals(_values, that._values);
    }
}

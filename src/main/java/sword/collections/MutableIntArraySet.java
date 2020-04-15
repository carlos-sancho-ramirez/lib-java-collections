package sword.collections;

import java.util.Arrays;

import static sword.collections.SortUtils.DEFAULT_GRANULARITY;
import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;

public final class MutableIntArraySet extends AbstractIntTraversable implements MutableIntSet {

    private static final int GRANULARITY = DEFAULT_GRANULARITY;

    public static MutableIntArraySet empty() {
        return new MutableIntArraySet(new int[GRANULARITY], 0);
    }

    private static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    private int _size;
    private int[] _values;

    private MutableIntArraySet(int[] values, int size) {
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
    public int indexOf(int value) {
        return findKey(_values, _size, value);
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
    public IntList mapToInt(IntToIntFunction func) {
        if (_size == 0) {
            return ImmutableIntList.empty();
        }

        final int[] newValues = new int[_size];
        for (int i = 0; i < _size; i++) {
            newValues[i] = func.apply(_values[i]);
        }

        return new ImmutableIntList(newValues);
    }

    @Override
    public <U> List<U> map(IntFunction<? extends U> func) {
        if (_size == 0) {
            return ImmutableList.empty();
        }

        final Object[] newValues = new Object[_size];
        for (int i = 0; i < _size; i++) {
            newValues[i] = func.apply(_values[i]);
        }

        return new ImmutableList<>(newValues);
    }

    @Override
    public <E> IntKeyMap<E> assign(IntFunction<? extends E> function) {
        if (_size == 0) {
            return ImmutableIntKeyMap.empty();
        }

        final int[] keys = new int[_size];
        final Object[] values = new Object[_size];
        for (int i = 0; i < _size; i++) {
            final int value = _values[i];
            keys[i] = value;
            values[i] = function.apply(value);
        }

        return new ImmutableIntKeyMap<>(keys, values);
    }

    @Override
    public IntPairMap assignToInt(IntToIntFunction function) {
        if (_size == 0) {
            return ImmutableIntPairMap.empty();
        }

        final int[] keys = new int[_size];
        final int[] values = new int[_size];
        for (int i = 0; i < _size; i++) {
            final int value = _values[i];
            keys[i] = value;
            values[i] = function.apply(value);
        }

        return new ImmutableIntPairMap(keys, values);
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

    @Override
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

    @Override
    public int size() {
        return _size;
    }

    @Override
    public IntTransformer iterator() {
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
    public IntSet toSet() {
        return this;
    }

    @Override
    public ImmutableIntSet toImmutable() {
        return ImmutableIntSetCreator.fromMutableIntSet(this);
    }

    @Override
    public MutableIntArraySet mutate() {
        return fromIntSet(this);
    }

    private class Iterator extends AbstractIntTransformer {

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

    static MutableIntArraySet fromIntSet(IntSet set) {
        final MutableIntArraySet result = MutableIntArraySet.empty();
        for (int value : set) {
            result.add(value);
        }

        return result;
    }

    static MutableIntArraySet fromIntRange(ImmutableIntRange range) {
        final int size = range.size();
        final int[] values = new int[suitableArrayLength(size)];
        final int min = range.min();
        for (int i = 0; i < size; i++) {
            values[i] = min + i;
        }

        return new MutableIntArraySet(values, size);
    }

    public static class Builder implements MutableIntSet.Builder {
        private final MutableIntArraySet _set = MutableIntArraySet.empty();

        @Override
        public Builder add(int value) {
            _set.add(value);
            return this;
        }

        @Override
        public MutableIntArraySet build() {
            return _set;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MutableIntArraySet)) {
            return super.equals(other);
        }

        final MutableIntArraySet that = (MutableIntArraySet) other;
        return Arrays.equals(_values, that._values);
    }
}

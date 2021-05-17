package sword.collections;

public final class MutableIntList extends AbstractIntTraversable implements IntList, MutableIntTransformable {

    public static MutableIntList empty(ArrayLengthFunction arrayLengthFunction) {
        final int length = arrayLengthFunction.suitableArrayLength(0, 0);
        return new MutableIntList(arrayLengthFunction, new int[length], 0);
    }

    public static MutableIntList empty() {
        return empty(GranularityBasedArrayLengthFunction.getInstance());
    }

    private final ArrayLengthFunction _arrayLengthFunction;
    private int[] _values;
    private int _size;

    MutableIntList(ArrayLengthFunction arrayLengthFunction, int[] values, int size) {
        _arrayLengthFunction = arrayLengthFunction;
        _values = values;
        _size = size;
    }

    @Override
    public int get(int key) {
        if (key < 0 || key >= _size) {
            throw new UnmappedKeyException();
        }

        return _values[key];
    }

    @Override
    public int get(int key, int defaultValue) {
        return (key >= 0 && key < _size)? _values[key] : defaultValue;
    }

    @Override
    public IntSet toSet() {
        final ImmutableIntSetCreator builder = new ImmutableIntSetCreator();
        for (int i = 0; i < _size; i++) {
            builder.add(_values[i]);
        }
        return builder.build();
    }

    @Override
    public int size() {
        return _size;
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
    public ImmutableIntList toImmutable() {
        if (_size == 0) {
            return ImmutableIntList.empty();
        }
        else {
            final int[] values = new int[_size];
            System.arraycopy(_values, 0, values, 0, _size);
            return new ImmutableIntList(values);
        }
    }

    @Override
    public MutableIntList mutate(ArrayLengthFunction arrayLengthFunction) {
        final int[] newValues = new int[arrayLengthFunction.suitableArrayLength(0, _size)];
        System.arraycopy(_values, 0, newValues, 0, _size);
        return new MutableIntList(arrayLengthFunction, newValues, _size);
    }

    @Override
    public MutableIntList mutate() {
        return mutate(_arrayLengthFunction);
    }

    /**
     * Inserts the given value in the given position.
     *
     * This will shift all elements with higher index or equal index in one,
     * in order to make space for the new value.
     * @param index Index where this value wants to be included.
     *              Index must be between zero and the value returned by {@link #size()}, both included.
     * @param value Value to be inserted in that position.
     * @throws IndexOutOfBoundsException if the given index is not within the expected range.
     */
    public void insert(int index, int value) {
        if (index < 0 || index > _size) {
            throw new IndexOutOfBoundsException();
        }

        final int arrayLength = _arrayLengthFunction.suitableArrayLength(_values.length, _size + 1);
        if (arrayLength != _values.length) {
            int[] newValues = new int[arrayLength];
            if (index > 0) {
                System.arraycopy(_values, 0, newValues, 0, index);
            }
            if (_size > index) {
                System.arraycopy(_values, index, newValues, index + 1, _size - index);
            }
            _values = newValues;
        }
        else {
            for (int i = _size; i > index; i--) {
                _values[i] = _values[i - 1];
            }
        }
        _values[index] = value;
        ++_size;
    }

    public void prepend(int item) {
        insert(0, item);
    }

    public void append(int value) {
        final int length = _arrayLengthFunction.suitableArrayLength(_values.length, _size + 1);
        if (length != _values.length) {
            final int[] values = new int[length];
            System.arraycopy(_values, 0, values, 0, _size);
            _values = values;
        }

        _values[_size++] = value;
    }

    public void appendAll(IntList that) {
        for (int item : that) {
            append(item);
        }
    }

    /**
     * Create a new mutable instance and copy from this collection the actual data references. After it, this collection gets cleared.
     * <p>
     * This is a more efficient alternative to the following code:
     * <code>
     * <br>MutableIntList newList = list.mutate();
     * <br>list.clear();
     * </code>
     *
     * @return A new mutable map that contains the actual data of this map.
     */
    public MutableIntList donate() {
        final MutableIntList newList = new MutableIntList(_arrayLengthFunction, _values, _size);
        final int length = _arrayLengthFunction.suitableArrayLength(0, 0);
        _values = new int[length];
        _size = 0;
        return newList;
    }

    @Override
    public void removeAt(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        final int length = _arrayLengthFunction.suitableArrayLength(_values.length, --_size);
        if (length != _values.length) {
            final int[] values = new int[length];
            if (index > 0) {
                System.arraycopy(_values, 0, values, 0, index);
            }

            if (index < _size) {
                System.arraycopy(_values, index + 1, values, index, _size - index);
            }
            _values = values;
        }
        else {
            for (int i = index; i < _size; i++) {
                _values[i] = _values[i + 1];
            }
        }
    }

    /**
     * Replace the value in the given index by the one provided here.
     *
     * This method will never modify the size of this collection.
     *
     * @param index Index where this value must be replaced.
     *              Index must be between zero (included) and the value returned by {@link #size()} (excluded).
     * @param value Value to be set in that position.
     * @return True if this operation modified the collection values.
     *         False if the value provided matches the one it was there before.
     * @throws IndexOutOfBoundsException if the given index is not within the expected range.
     */
    public boolean put(int index, int value) {
        final boolean result = _values[index] != value;
        _values[index] = value;
        return result;
    }

    @Override
    public boolean clear() {
        final int suitableLength = _arrayLengthFunction.suitableArrayLength(_values.length, 0);
        if (_values.length != suitableLength) {
            _values = new int[suitableLength];
        }

        final boolean changed = _size > 0;
        _size = 0;
        return changed;
    }

    private class Iterator extends AbstractIntTransformer {
        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _size;
        }

        @Override
        public Integer next() {
            return _values[_index++];
        }

        @Override
        public void remove() {
            removeAt(--_index);
        }
    }

    @Override
    public IntTransformer iterator() {
        return new Iterator();
    }

    @Override
    public IntList toList() {
        return this;
    }

    public static final class Builder implements IntListBuilder, MutableIntTransformableBuilder {

        private final MutableIntList _list = MutableIntList.empty();

        public Builder append(int value) {
            _list.append(value);
            return this;
        }

        @Override
        public Builder add(int value) {
            _list.append(value);
            return this;
        }

        @Override
        public MutableIntList build() {
            return _list;
        }
    }

    @Override
    public int hashCode() {
        final int length = _size;
        int hash = length;

        for (int i = 0; i < length; i++) {
            hash = hash * 31 + _values[i];
        }

        return hash;
    }
}

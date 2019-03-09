package sword.collections;

public final class ImmutableIntList extends AbstractImmutableIntTransformable implements IntList {

    private static final ImmutableIntList EMPTY = new ImmutableIntList(new int[0]);

    public static ImmutableIntList empty() {
        return EMPTY;
    }

    private final int[] _values;

    ImmutableIntList(int[] values) {
        _values = values;
    }

    @Override
    public int get(int key) {
        if (key < 0 || key >= _values.length) {
            throw new UnmappedKeyException();
        }

        return _values[key];
    }

    @Override
    public int get(int key, int defaultValue) {
        return (key >= 0 && key < _values.length)? _values[key] : defaultValue;
    }

    @Override
    public ImmutableIntSet toSet() {
        final ImmutableIntSetBuilder builder = new ImmutableIntSetBuilder();
        for (int i = 0; i < _values.length; i++) {
            builder.add(_values[i]);
        }
        return builder.build();
    }

    @Override
    public ImmutableIntList toImmutable() {
        return this;
    }

    @Override
    public int valueAt(int index) {
        return _values[index];
    }

    @Override
    public int size() {
        return _values.length;
    }

    @Override
    public int indexOf(int value) {
        for (int i = 0; i < _values.length; i++) {
            if (value == _values[i]) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean contains(int value) {
        return indexOf(value) >= 0;
    }

    @Override
    public ImmutableIntList toList() {
        return this;
    }

    @Override
    public ImmutableIntList filter(IntPredicate predicate) {
        return (ImmutableIntList) super.filter(predicate);
    }

    @Override
    public ImmutableIntList filterNot(IntPredicate predicate) {
        return (ImmutableIntList) super.filterNot(predicate);
    }

    @Override
    public <U> ImmutableList<U> map(IntFunction<U> func) {
        return (ImmutableList<U>) super.map(func);
    }

    @Override
    public ImmutableIntList mapToInt(IntToIntFunction func) {
        return (ImmutableIntList) super.mapToInt(func);
    }

    @Override
    public int reduce(IntReduceFunction func) {
        final int size = _values.length;
        if (size == 0) {
            throw new EmptyCollectionException();
        }

        if (size == 1) {
            return _values[0];
        }

        int result = func.apply(_values[0], _values[1]);
        for (int i = 2; i < size; i++) {
            result = func.apply(result, _values[i]);
        }

        return result;
    }

    @Override
    public IntTransformer iterator() {
        return new Iterator();
    }

    @Override
    Builder newIntBuilder() {
        return new Builder();
    }

    @Override
    <U> ImmutableList.Builder<U> newBuilder() {
        return new ImmutableList.Builder<U>();
    }

    private class Iterator extends AbstractIntTransformer {
        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _values.length;
        }

        @Override
        public Integer next() {
            return _values[_index++];
        }
    }

    /**
     * Returns the first value matching the predicate or the default value if none matches.
     */
    public int findFirst(IntPredicate predicate, int defaultValue) {
        final int length = _values.length;
        for (int i = 0; i < length; i++) {
            final int item = _values[i];
            if (predicate.apply(item)) {
                return item;
            }
        }

        return defaultValue;
    }

    public static class Builder implements ImmutableIntTransformableBuilder<ImmutableIntList> {
        private final MutableIntList _list = MutableIntList.empty();

        public Builder append(int item) {
            _list.append(item);
            return this;
        }

        @Override
        public Builder add(int value) {
            _list.append(value);
            return this;
        }

        @Override
        public ImmutableIntList build() {
            return _list.toImmutable();
        }
    }

    /**
     * Creates a new {@link ImmutableIntList} of the same type where the given
     * value is included at the end of the list.
     *
     * @param value Item to be included at the end of the list.
     */
    public ImmutableIntList append(int value) {
        final int length = _values.length;
        final int[] newValues = new int[length + 1];
        System.arraycopy(_values, 0, newValues, 0, length);
        newValues[length] = value;
        return new ImmutableIntList(newValues);
    }

    public ImmutableIntList appendAll(IntList that) {
        if (that == null || that.isEmpty()) {
            return this;
        }
        else if (isEmpty()) {
            return that.toImmutable();
        }
        else {
            final Builder builder = new Builder();
            final int thisLength = _values.length;

            for (int i = 0; i < thisLength; i++) {
                builder.append(_values[i]);
            }

            for (int item : that) {
                builder.append(item);
            }

            return builder.build();
        }
    }

    /**
     * Composes a new map traversing this list, applying the given function to each item.
     *
     * This method will compose a new list for all items that the given function does
     * return an equivalent value. The resulting list will be the value within the new map,
     * and the returned value will be the key within the map for that list.
     *
     * Example:
     * List(1,2,3,4,5) grouped by func (item % 2) will create Map(0 -&gt; List(2,4), 1 -&gt; List(1,3,5))
     *
     * @param function Function to be applied to each item within the list to determine its group.
     * @param <K> Type for the new key within the returned map.
     * @return A new map where items have been grouped into different lists according with the function given.
     */
    public <K> ImmutableMap<K, ImmutableIntList> groupBy(IntFunction<K> function) {
        MutableMap<K, ImmutableIntList> map = MutableHashMap.empty();
        final int length = size();
        for (int i = 0; i < length; i++) {
            final int value = _values[i];
            final K group = function.apply(value);
            final ImmutableIntList current = map.get(group, ImmutableIntList.empty());
            map.put(group, current.append(value));
        }

        return (map.size() != 1)? map.toImmutable() :
                new ImmutableHashMap.Builder<K, ImmutableIntList>().put(map.keyAt(0), this).build();
    }

    /**
     * Composes a new map traversing this list, applying the given function to each item.
     *
     * This method will compose a new list for all items that the given function does
     * return the same integer value. The resulting list will be the value within the new map,
     * and the returned value will be the key within the map for that list.
     *
     * Example:
     * List(1,2,3,4,5) grouped by func (item % 2) will create Map(0 -&gt; List(2,4), 1 -&gt; List(1,3,5))
     *
     * @param function Function to be applied to each item within the list to determine its group.
     * @return A new map where items have been grouped into different lists according with the function given.
     */
    public ImmutableIntKeyMap<ImmutableIntList> groupByInt(IntToIntFunction function) {
        MutableIntKeyMap<ImmutableIntList> map = MutableIntKeyMap.empty();
        final int length = size();
        for (int i = 0; i < length; i++) {
            final int value = _values[i];
            final int group = function.apply(value);
            final ImmutableIntList current = map.get(group, ImmutableIntList.empty());
            map.put(group, current.append(value));
        }

        return (map.size() != 1)? map.toImmutable() :
                new ImmutableIntKeyMap.Builder<ImmutableIntList>().put(map.keyAt(0), this).build();
    }

    @Override
    public int hashCode() {
        final int length = _values.length;
        int hash = length;

        for (int i = 0; i < length; i++) {
            hash = hash * 31 + _values[i];
        }

        return hash;
    }
}

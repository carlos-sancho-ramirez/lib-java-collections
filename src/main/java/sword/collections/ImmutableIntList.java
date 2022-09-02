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
        final ImmutableIntSetCreator builder = new ImmutableIntSetCreator();
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
    public MutableIntList mutate(ArrayLengthFunction arrayLengthFunction) {
        final int size = _values.length;
        final int length = arrayLengthFunction.suitableArrayLength(0, size);
        final int[] newValues = new int[length];
        System.arraycopy(_values, 0, newValues, 0, size);
        return new MutableIntList(arrayLengthFunction, newValues, size);
    }

    @Override
    public MutableIntList mutate() {
        return mutate(GranularityBasedArrayLengthFunction.getInstance());
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
    public int last() throws EmptyCollectionException {
        final int size = _values.length;
        if (size == 0) {
            throw new EmptyCollectionException();
        }

        return _values[size - 1];
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
    public ImmutableIntList slice(ImmutableIntRange range) {
        final int size = _values.length;
        final int min = range.min();
        final int max = range.max();
        if (range.min() <= 0 && range.max() >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableIntList.empty();
        }

        final int newSize = Math.min(max, size - 1) - min + 1;
        final int[] newValues = new int[newSize];
        System.arraycopy(_values, min, newValues, 0, newSize);
        return new ImmutableIntList(newValues);
    }

    private ImmutableIntList skip(int index, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Unable to skip a negative number of elements");
        }
        else if (length == 0) {
            return this;
        }
        else if (length >= _values.length) {
            return empty();
        }

        final int remain = _values.length - length;
        int[] newValues = new int[remain];
        System.arraycopy(_values, index, newValues, 0, remain);
        return new ImmutableIntList(newValues);
    }

    private ImmutableIntList take(int index, int length) {
        final int size = _values.length;
        if (length >= size) {
            return this;
        }

        if (length == 0) {
            return ImmutableIntList.empty();
        }

        final int[] newValues = new int[length];
        System.arraycopy(_values, index, newValues, 0, length);
        return new ImmutableIntList(newValues);
    }

    /**
     * Returns a new ImmutableIntList where the <code>length</code>
     * amount of first elements has been removed.
     * <p>
     * This will return an empty list if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the start of the list.
     * @return A new ImmutableIntList instance without the first elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance of the given length is equal or greater
     *         than the actual length of the list.
     */
    @Override
    public ImmutableIntList skip(int length) {
        return skip(length, length);
    }

    /**
     * Returns a new ImmutableIntList where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of the list.
     * @return A new ImmutableIntList instance just including the first elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length equals or greater than the
     *         actual size of this collection.
     */
    @Override
    public ImmutableIntList take(int length) {
        return take(0, length);
    }

    /**
     * Returns a new ImmutableIntList where the <code>length</code>
     * amount of last elements has been removed.
     * <p>
     * This will return an empty list if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the end of the list.
     * @return A new ImmutableIntList instance without the last elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance if the given length is equal or greater
     *         than the actual length of the list.
     */
    public ImmutableIntList skipLast(int length) {
        return skip(0, length);
    }

    /**
     * Returns a new ImmutableIntList where only the <code>length</code> amount of
     * last elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the end of this list.
     * @return A new ImmutableIntList instance just including the last elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length equals or greater than the
     *         actual size of this collection.
     */
    public ImmutableIntList takeLast(int length) {
        return take(_values.length - length, length);
    }

    @Override
    public <U> ImmutableList<U> map(IntFunction<? extends U> func) {
        final int size = _values.length;
        if (size == 0) {
            return ImmutableList.empty();
        }

        final Object[] newValues = new Object[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = func.apply(_values[i]);
        }
        return new ImmutableList<>(newValues);
    }

    @Override
    public ImmutableIntList mapToInt(IntToIntFunction func) {
        final int size = _values.length;
        if (size == 0) {
            return empty();
        }

        final int[] newValues = new int[size];
        for (int i = 0; i < size; i++) {
            newValues[i] = func.apply(_values[i]);
        }
        return new ImmutableIntList(newValues);
    }

    /**
     * Returns a new instance of a list, where all the items from this list has
     * been copied but in the reversed traversable order.
     *
     * This method has no effect for empty lists or lists with only one element,
     * and the same instance will be returned. For the rest of cases, a new
     * instance will be created and the values within this list will
     * be copied in the opposite order. In other words, the last item in the
     * list will become the first.
     *
     * @return A list where all items are in the opposite order of iteration.
     */
    public ImmutableIntList reverse() {
        final int size = _values.length;
        if (size < 2) {
            return this;
        }
        else {
            final int[] newValues = new int[size];
            for (int i = 0; i < size; i++) {
                newValues[size - i - 1] = _values[i];
            }
            return new ImmutableIntList(newValues);
        }
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

    public static class Builder implements IntListBuilder, ImmutableIntTransformableBuilder {
        private final MutableIntList _list;

        public Builder() {
            _list = MutableIntList.empty();
        }

        public Builder(ArrayLengthFunction arrayLengthFunction) {
            _list = MutableIntList.empty(arrayLengthFunction);
        }

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
     * Wraps the given array into an ImmutableIntList instance.
     *
     * In order to be efficient, this method will not copy the given array, but
     * the same copy of the given array will be used instead. The caller of
     * this method is responsible to do not modify directly the given array
     * once this method has been called.
     *
     * @param items array to be used be the new created ImmutableIntList
     * @return A new ImmutableIntList containing the same array given.
     */
    public static ImmutableIntList from(int[] items) {
        return new ImmutableIntList(items);
    }

    /**
     * Return a new created int array that contains a copy of the content of this collection.
     */
    public int[] toArray() {
        final int[] array = new int[_values.length];
        System.arraycopy(_values, 0, array, 0, _values.length);
        return array;
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

    /**
     * Creates a new {@link ImmutableIntList} instance where all items in the
     * given {@link Iterable} are appended to the items in this collection.
     *
     * This method assumes that none of the items in the given collection is null.
     *
     * This method will return the same instance if the given iterable is
     * null or empty.
     *
     * @param that A collection of numbers to be appended to the ones in this
     *             collection. This can be null, but none of the elements can
     *             be null.
     * @return A new instance with the resulting concatenation, or this
     *         instance if nothing has been appended.
     */
    public ImmutableIntList appendAll(Iterable<Integer> that) {
        if (that == null) {
            return this;
        }

        if (that instanceof Sizable) {
            final Sizable thatSizable = (Sizable) that;
            if (thatSizable.isEmpty()) {
                return this;
            }
            else if (isEmpty() && that instanceof IntList) {
                return ((IntList) that).toImmutable();
            }
            else {
                final int thisLength = _values.length;
                final int thatLength = thatSizable.size();

                final int[] newValues = new int[thisLength + thatLength];
                System.arraycopy(_values, 0, newValues, 0, thisLength);

                final java.util.Iterator<Integer> it = that.iterator();
                for (int index = thisLength; it.hasNext(); index++) {
                    newValues[index] = it.next();
                }

                return new ImmutableIntList(newValues);
            }
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

    @Override
    public ImmutableIntList removeAt(int index) {
        final int size = _values.length;
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        final int[] newValues = new int[size - 1];
        if (index > 0) {
            System.arraycopy(_values, 0, newValues, 0, index);
        }

        final int remaining = size - index - 1;
        if (remaining > 0) {
            System.arraycopy(_values, index + 1, newValues, index, remaining);
        }

        return new ImmutableIntList(newValues);
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

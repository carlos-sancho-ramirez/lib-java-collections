package sword.collections;

import sword.annotations.SignatureRequired;

import java.util.Collection;

public final class ImmutableList<T> extends AbstractImmutableTransformable<T> implements List<T> {

    private static final ImmutableList<Object> EMPTY = new ImmutableList<>(new Object[0]);

    @SuppressWarnings("unchecked")
    public static <E> ImmutableList<E> empty() {
        return (ImmutableList<E>) EMPTY;
    }

    public static <E> ImmutableList<E> singleton(E item) {
        return new ImmutableList<>(new Object[] { item });
    }

    private final Object[] _values;

    ImmutableList(Object[] values) {
        _values = values;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) _values[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public T valueAt(int index) {
        return (T) _values[index];
    }

    @Override
    public int size() {
        return _values.length;
    }

    @Override
    public T last() throws EmptyCollectionException {
        final int size = _values.length;
        if (size == 0) {
            throw new EmptyCollectionException();
        }

        return valueAt(size - 1);
    }

    @Override
    public int indexOf(T value) {
        return SortUtils.indexOf(_values, _values.length, value);
    }

    /**
     * Return true if an equivalent item is found in the list, this means
     * that it will be true if calling {@link Object#equals(Object)} with
     * this value returns true on any of the item.
     *
     * @param value Value to check
     */
    @Override
    public boolean contains(T value) {
        return indexOf(value) >= 0;
    }

    @Override
    public ImmutableList<T> toList() {
        return this;
    }

    @Override
    public ImmutableIntSet indexes() {
        final int size = size();
        return (size == 0)? ImmutableIntArraySet.empty() : new ImmutableIntRange(0, size - 1);
    }

    @Override
    public ImmutableList<T> filter(Predicate<? super T> predicate) {
        return (ImmutableList<T>) super.filter(predicate);
    }

    @Override
    public ImmutableList<T> filterNot(Predicate<? super T> predicate) {
        return (ImmutableList<T>) super.filterNot(predicate);
    }

    @Override
    public ImmutableList<T> slice(ImmutableIntRange range) {
        final int size = _values.length;
        final int min = range.min();
        final int max = range.max();
        if (min >= size || max < 0) {
            return ImmutableList.empty();
        }

        if (range.min() <= 0 && range.max() >= size - 1) {
            return this;
        }

        final ImmutableList.Builder<T> builder = new ImmutableList.Builder<>();
        for (int position = min; position <= max && position < size; position++) {
            builder.append(valueAt(position));
        }

        return builder.build();
    }

    private ImmutableList<T> skip(int index, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Unable to skip a negative number of elements");
        }
        else if (length == 0 || _values.length == 0) {
            return this;
        }
        else if (length >= _values.length) {
            return empty();
        }

        final int remain = _values.length - length;
        Object[] newValues = new Object[remain];
        System.arraycopy(_values, index, newValues, 0, remain);
        return new ImmutableList<>(newValues);
    }

    private ImmutableList<T> take(int index, int length) {
        final int size = _values.length;
        if (length >= size) {
            return this;
        }

        if (length == 0) {
            return ImmutableList.empty();
        }

        final Object[] newValues = new Object[length];
        System.arraycopy(_values, index, newValues, 0, length);
        return new ImmutableList<>(newValues);
    }

    /**
     * Returns a new ImmutableList of the same type where the
     * <code>length</code> amount of first elements has been removed.
     * <p>
     * This will return an empty list if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the start of the list.
     * @return A new ImmutableList instance without the first elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance of the given length is equal or greater
     *         than the actual length of the list.
     */
    @Override
    public ImmutableList<T> skip(int length) {
        return skip(length, length);
    }

    /**
     * Returns a new ImmutableList where only the <code>length</code> amount of
     * first elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the start of the list.
     * @return A new ImmutableList instance just including the first elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length equals or greater than the
     *         actual size of this collection.
     */
    public ImmutableList<T> take(int length) {
        return take(0, length);
    }

    /**
     * Returns a new ImmutableList of the same type where the
     * <code>length</code> amount of last elements has been removed.
     * <p>
     * This will return an empty list if the given parameter matches
     * or exceeds the length of this array.
     *
     * @param length the amount of elements to be removed from the end of the list.
     * @return A new ImmutableList instance without the last elements,
     *         the same instance in case the given length is 0,
     *         or the empty instance of the given length is equal or greater
     *         than the actual length of the list.
     */
    @Override
    public ImmutableList<T> skipLast(int length) {
        return skip(0, length);
    }

    /**
     * Returns a new ImmutableList where only the <code>length</code> amount of
     * last elements are included, and the rest is discarded if any.
     * <p>
     * If length is equal or greater than the actual size, the same instance will be returned.
     *
     * @param length the maximum number of elements to be included from the end of this list.
     * @return A new ImmutableList instance just including the last elements,
     *         the empty instance in case the given length is 0, or the same
     *         instance in case the given length equals or greater than the
     *         actual size of this collection.
     */
    @Override
    public ImmutableList<T> takeLast(int length) {
        return take(_values.length - length, length);
    }

    /**
     * Returns a new instance of a list, where all the items from this list has
     * been copied but in the reversed traversable order.
     *
     * This method has no effect for empty lists or lists with only one element,
     * and the same instance will be returned. For the rest of cases, a new
     * instance will be created and the object references within this list will
     * be copied in the opposite order. In other words, the last items in the
     * list will become the first.
     *
     * @return A list where all items are in the opposite order of iteration.
     */
    public ImmutableList<T> reverse() {
        final int size = _values.length;
        if (size < 2) {
            return this;
        }
        else {
            final Object[] newValues = new Object[size];
            for (int i = 0; i < size; i++) {
                newValues[size - i - 1] = _values[i];
            }
            return new ImmutableList<>(newValues);
        }
    }

    /**
     * Method that iterates only once the collection and creates a pair of lists.
     * The left list in the pair will contain all elements that satisfies the given predicate,
     * while the right list in the pair will contain all elements that will not satisfy the predicate.
     *
     * This method is equivalent to:
     * <br>ImmutableList&lt;U&gt; left = this.filter(predicate);
     * <br>ImmutableList&lt;U&gt; right = this.filterNot(predicate);
     *
     * However this method only iterates once the collection, which makes it more efficient,
     * especially for large collections.
     *
     * @param predicate Condition to evaluate for each element. If true, the element will be
     *                  included in the 'left' list, if false, it will be included in the 'right' list.
     * @return An {@link ImmutablePair} of lists. This will never be null.
     */
    public ImmutablePair<ImmutableList<T>, ImmutableList<T>> span(Predicate<? super T> predicate) {
        boolean allTrue = true;
        boolean allFalse = true;

        Builder<T> builderForTrue = newBuilder();
        Builder<T> builderForFalse = newBuilder();

        for (T item : this) {
            if (predicate.apply(item)) {
                allFalse = false;
                builderForTrue.add(item);
            }
            else {
                allTrue = false;
                builderForFalse.add(item);
            }
        }

        if (allTrue) {
            final ImmutableList<T> empty = empty();
            return new ImmutablePair<>(this, empty);
        }
        else if (allFalse) {
            final ImmutableList<T> empty = empty();
            return new ImmutablePair<>(empty, this);
        }
        else {
            return new ImmutablePair<>(builderForTrue.build(), builderForFalse.build());
        }
    }

    @Override
    public ImmutableIntList mapToInt(IntResultFunction<? super T> func) {
        final int length = _values.length;
        final int[] newValues = new int[length];
        for (int i = 0; i < length; i++) {
            newValues[i] = func.apply(valueAt(i));
        }

        return new ImmutableIntList(newValues);
    }

    @Override
    public <U> ImmutableList<U> map(Function<? super T, ? extends U> func) {
        final int length = _values.length;
        final Object[] newValues = new Object[length];
        for (int i = 0; i < length; i++) {
            newValues[i] = func.apply(valueAt(i));
        }

        return new ImmutableList<>(newValues);
    }

    @Override
    public ImmutableList<T> removeAt(int index) {
        final int size = _values.length;
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        final Object[] newValues = new Object[size - 1];
        if (index > 0) {
            System.arraycopy(_values, 0, newValues, 0, index);
        }

        final int remaining = size - index - 1;
        if (remaining > 0) {
            System.arraycopy(_values, index + 1, newValues, index, remaining);
        }

        return new ImmutableList<>(newValues);
    }

    @Override
    public Transformer<T> iterator() {
        return new Iterator();
    }

    @Override
    <U> Builder<U> newBuilder() {
        return new Builder<U>();
    }

    @Override
    public ImmutableHashSet<T> toSet() {
        final ImmutableHashSet.Builder<T> builder = new ImmutableHashSet.Builder<>();
        for (int i = 0; i < _values.length; i++) {
            builder.add(valueAt(i));
        }

        return builder.build();
    }

    @Override
    public ImmutableList<T> toImmutable() {
        return this;
    }

    @Override
    public MutableList<T> mutate(ArrayLengthFunction arrayLengthFunction) {
        final int size = _values.length;
        final int length = arrayLengthFunction.suitableArrayLength(0, size);
        final Object[] values = new Object[length];
        System.arraycopy(_values, 0, values, 0, size);
        return new MutableList<>(arrayLengthFunction, values, size);
    }

    @Override
    public MutableList<T> mutate() {
        return mutate(GranularityBasedArrayLengthFunction.getInstance());
    }

    /**
     * Creates a new list where all current elements are sorted following the given function.
     *
     * @param function Function the sort the elements within this collection.
     * @return A new list where all current elements are sorted following the given function.
     */
    @Override
    public ImmutableList<T> sort(SortFunction<? super T> function) {
        final int length = _values.length;
        if (length < 2) {
            return this;
        }

        final Object[] newValues = new Object[length];
        newValues[0] = _values[0];
        boolean changed = false;

        for (int i = 1; i < length; i++) {
            final int index = SortUtils.findSuitableIndex(function, newValues, i, (T) _values[i]);
            changed |= index != i;
            for (int j = i; j > index; j--) {
                newValues[j] = newValues[j - 1];
            }
            newValues[index] = _values[i];
        }

        return changed? new ImmutableList<>(newValues) : this;
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
    public <K> ImmutableMap<K, ImmutableList<T>> groupBy(Function<T, K> function) {
        MutableMap<K, ImmutableList<T>> map = MutableHashMap.empty();
        final int length = size();
        for (int i = 0; i < length; i++) {
            final T value = valueAt(i);
            final K group = function.apply(value);
            final ImmutableList<T> current = map.get(group, ImmutableList.empty());
            map.put(group, current.append(value));
        }

        return (map.size() != 1)? map.toImmutable() :
                new ImmutableHashMap.Builder<K, ImmutableList<T>>().put(map.keyAt(0), this).build();
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
    public ImmutableIntKeyMap<ImmutableList<T>> groupByInt(IntResultFunction<T> function) {
        MutableIntKeyMap<ImmutableList<T>> map = MutableIntKeyMap.empty();
        final int length = size();
        for (int i = 0; i < length; i++) {
            final T value = valueAt(i);
            final int group = function.apply(value);
            final ImmutableList<T> current = map.get(group, ImmutableList.empty());
            map.put(group, current.append(value));
        }

        return (map.size() != 1)? map.toImmutable() :
                new ImmutableIntKeyMap.Builder<ImmutableList<T>>().put(map.keyAt(0), this).build();
    }

    private class Iterator extends AbstractTransformer<T> {
        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _values.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            return (T) _values[_index++];
        }
    }

    public static class Builder<E> implements ListBuilder<E>, ImmutableTransformableBuilder<E> {
        private final MutableList<E> _list;

        public Builder() {
            _list = MutableList.empty();
        }

        public Builder(ArrayLengthFunction arrayLengthFunction) {
            _list = MutableList.empty(arrayLengthFunction);
        }

        @Override
        public Builder<E> append(E item) {
            _list.append(item);
            return this;
        }

        @Override
        public Builder<E> add(E item) {
            return append(item);
        }

        @Override
        public ImmutableList<E> build() {
            return _list.toImmutable();
        }
    }

    /**
     * Wraps the given array into an ImmutableList instance.
     *
     * In order to be efficient, this method will not copy the given array, but
     * the same copy of the given array will be used instead. The caller of
     * this method is responsible to do not modify directly the given array
     * once this method has been called.
     *
     * @param items array to be used be the new created ImmutableList
     * @param <E> type for the array
     * @return A new ImmutableList containing the same array given.
     */
    public static <E> ImmutableList<E> from(E[] items) {
        return new ImmutableList<>(items);
    }

    public static <E> ImmutableList<E> from(Collection<E> collection) {
        final int size = collection.size();
        final Object[] values = new Object[size];
        int index = 0;
        for (E token : collection) {
            values[index++] = token;
        }

        return new ImmutableList<>(values);
    }

    /**
     * Creates a new {@link ImmutableList} of the same type where the given
     * value is included at the end of the list.
     *
     * @param item item to be included at the end of the list.
     */
    public ImmutableList<T> append(T item) {
        final int length = _values.length;
        final Object[] newValues = new Object[length + 1];
        System.arraycopy(_values, 0, newValues, 0, length);
        newValues[length] = item;
        return new ImmutableList<T>(newValues);
    }

    /**
     * Creates a new {@link ImmutableList} of the same type where the given
     * value is included at the beginning of the list.
     *
     * @param item item to be included at the beginning of the list.
     */
    public ImmutableList<T> prepend(T item) {
        final int length = _values.length;
        final Object[] newValues = new Object[length + 1];
        System.arraycopy(_values, 0, newValues, 1, length);
        newValues[0] = item;
        return new ImmutableList<T>(newValues);
    }

    /**
     * Creates a new {@link ImmutableList} instance where all items in the
     * given {@link Iterable} are appended to the items in this collection.
     *
     * This method will return the same instance if the given iterable is
     * null or empty.
     *
     * @param that A collection of items to be appended to the ones in this
     *             collection. This can be null.
     * @return A new instance with the resulting concatenation, or this
     *         instance if nothing has been appended.
     */
    public ImmutableList<T> appendAll(Iterable<T> that) {
        if (that == null) {
            return this;
        }

        final int thisLength = _values.length;
        if (that instanceof Sizable) {
            final int thatSize = ((Sizable) that).size();
            if (thatSize == 0) {
                return this;
            }

            if (that instanceof ImmutableList) {
                final ImmutableList<T> list = (ImmutableList<T>) that;
                if (isEmpty()) {
                    return list;
                }
                else {
                    final Object[] newValues = new Object[thisLength + thatSize];
                    System.arraycopy(_values, 0, newValues, 0, thisLength);
                    System.arraycopy(list._values, 0, newValues, thisLength, thatSize);
                    return new ImmutableList<>(newValues);
                }
            }
        }

        final Builder<T> builder = new Builder<T>();

        for (int i = 0; i < thisLength; i++) {
            builder.append(get(i));
        }

        for (T item : that) {
            builder.append(item);
        }

        return builder.build();
    }

    @SignatureRequired
    public ImmutableList<T> appendAll(ImmutableList<T> that) {
        return appendAll((Iterable<T>) that);
    }

    @Override
    public int hashCode() {
        final int length = _values.length;
        int hash = length;

        for (int i = 0; i < length; i++) {
            final Object item = _values[i];
            hash = hash * 31 + ((item != null)? item.hashCode() : 0);
        }

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ImmutableList)) {
            return super.equals(object);
        }
        else if (this == object) {
            return true;
        }

        final ImmutableList that = (ImmutableList) object;
        final int length = that._values.length;
        if (length != _values.length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            final Object thisValue = _values[i];
            final Object thatValue = that._values[i];

            if (thisValue == null && thatValue != null || thisValue != null && !thisValue.equals(thatValue)) {
                return false;
            }
        }

        return true;
    }
}

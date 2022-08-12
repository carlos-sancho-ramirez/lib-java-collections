package sword.collections;

import static sword.collections.SortUtils.equal;
import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;

/**
 * Efficient implementation for map where keys are internally
 * sorted according to their hash codes in ascending order.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should create a builder in order to obtain
 * an instance of it.
 *
 * This implementation assumes that keys inserted are also immutable.
 * Mutation of the contained keys may result in duplicates within
 * the keys or wrong sorting of keys.
 * It is not guaranteed to work if keys are mutable.
 * This does no apply to values of the map, that can mutate without risk.
 *
 * @param <T> Type for the key elements within the Map
 */
public final class ImmutableIntValueHashMap<T> extends AbstractImmutableIntValueMap<T> {

    private static final ImmutableIntValueHashMap<Object> EMPTY = new ImmutableIntValueHashMap<>(new Object[0], new int[0], new int[0]);

    @SuppressWarnings("unchecked")
    public static <U> ImmutableIntValueHashMap<U> empty() {
        return (ImmutableIntValueHashMap<U>) EMPTY;
    }

    private final int[] _hashCodes;

    ImmutableIntValueHashMap(Object[] keys, int[] hashCodes, int[] values) {
        super(keys, values);
        _hashCodes = hashCodes;
    }

    @Override
    public int indexOfKey(T key) {
        return findKey(_hashCodes, _keys, _keys.length, key);
    }

    @Override
    public ImmutableHashSet<T> keySet() {
        return new ImmutableHashSet<>(_keys, _hashCodes);
    }

    @Override
    public ImmutableIntValueMap<T> put(T key, int value) {
        int index = findKey(_hashCodes, _keys, _keys.length, key);
        if (index >= 0) {
            if (equal(_values[index], value)) {
                return this;
            }
            else {
                final int length = _values.length;
                final int[] newValues = new int[length];
                for (int i = 0; i < length; i++) {
                    newValues[i] = (i == index)? value : _values[i];
                }

                return new ImmutableIntValueHashMap<>(_keys, _hashCodes, newValues);
            }
        }
        else {
            final int hashCode = SortUtils.hashCode(key);
            index = findSuitableIndex(_hashCodes, _hashCodes.length, hashCode);

            final int newLength = _values.length + 1;
            final int[] newHashCodes = new int[newLength];
            final Object[] newKeys = new Object[newLength];
            final int[] newValues = new int[newLength];

            for (int i = 0; i < newLength; i++) {
                newHashCodes[i] = (i < index)? _hashCodes[i] : (i == index)? hashCode : _hashCodes[i - 1];
                newKeys[i] = (i < index)? _keys[i] : (i == index)? key : _keys[i - 1];
                newValues[i] = (i < index)? _values[i] : (i == index)? value : _values[i - 1];
            }

            return new ImmutableIntValueHashMap<>(newKeys, newHashCodes, newValues);
        }
    }

    @Override
    public ImmutableIntValueHashMap<T> putAll(IntValueMap<? extends T> other) {
        return (ImmutableIntValueHashMap<T>) super.putAll(other);
    }

    @Override
    boolean entryLessThan(Entry<T> a, Entry<T> b) {
        return b != null && (a == null || SortUtils.compareByHashCode(a.key(), b.key()));
    }

    @Override
    public ImmutableIntValueHashMap<T> toImmutable() {
        return this;
    }

    @Override
    public MutableIntValueHashMap<T> mutate(ArrayLengthFunction arrayLengthFunction) {
        final int size = _keys.length;
        final int length = arrayLengthFunction.suitableArrayLength(0, size);
        Object[] newKeys = new Object[length];
        int[] newHashCodes = new int[length];
        int[] newValues = new int[length];

        System.arraycopy(_keys, 0, newKeys, 0, size);
        System.arraycopy(_hashCodes, 0, newHashCodes, 0, size);
        System.arraycopy(_values, 0, newValues, 0, size);

        return new MutableIntValueHashMap<>(arrayLengthFunction, newKeys, newHashCodes, newValues, size);
    }

    @Override
    public MutableIntValueHashMap<T> mutate() {
        return mutate(GranularityBasedArrayLengthFunction.getInstance());
    }

    @Override
    public ImmutableIntSet toSet() {
        return toList().toSet();
    }

    @Override
    public ImmutableIntValueHashMap<T> filter(IntPredicate predicate) {
        final Builder<T> builder = new Builder<>();
        final int length = _keys.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            int value = _values[i];
            if (predicate.apply(value)) {
                builder.put(keyAt(i), value);
            }
            else {
                changed = true;
            }
        }

        return changed? builder.build() : this;
    }

    @Override
    public ImmutableIntValueHashMap<T> filterNot(IntPredicate predicate) {
        final Builder<T> builder = new Builder<>();
        final int length = _keys.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            int value = _values[i];
            if (predicate.apply(value)) {
                changed = true;
            }
            else {
                builder.put(keyAt(i), value);
            }
        }

        return changed? builder.build() : this;
    }

    @Override
    public ImmutableIntValueHashMap<T> filterByKey(Predicate<? super T> predicate) {
        final Builder<T> builder = new Builder<>();
        final int length = _keys.length;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            final T key = keyAt(i);
            if (predicate.apply(key)) {
                builder.put(key, _values[i]);
            }
            else {
                changed = true;
            }
        }

        return changed? builder.build() : this;
    }

    @Override
    public ImmutableIntValueHashMap<T> filterByEntry(Predicate<IntValueMapEntry<T>> predicate) {
        final Builder<T> builder = new Builder<>();
        final int length = _keys.length;
        boolean changed = false;

        if (length > 0) {
            final ReusableIntValueMapEntry<T> entry = new ReusableIntValueMapEntry<>();
            for (int i = 0; i < length; i++) {
                final T key = keyAt(i);
                final int value = valueAt(i);
                entry.set(key, value);
                if (predicate.apply(entry)) {
                    builder.put(key, _values[i]);
                }
                else {
                    changed = true;
                }
            }
        }

        return changed? builder.build() : this;
    }

    @Override
    public ImmutableIntValueHashMap<T> mapToInt(IntToIntFunction mapFunc) {
        final int itemCount = _keys.length;
        final int[] newValues = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableIntValueHashMap<>(_keys, _hashCodes, newValues);
    }

    @Override
    public <U> ImmutableHashMap<T, U> map(IntFunction<? extends U> mapFunc) {
        final int itemCount = _keys.length;
        final Object[] newValues = new Object[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableHashMap<>(_keys, _hashCodes, newValues);
    }

    @Override
    public ImmutableIntValueHashMap<T> removeAt(int index) {
        final int size = _values.length;
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        final Object[] newKeys = new Object[size - 1];
        final int[] newHashCodes = new int[size - 1];
        final int[] newValues = new int[size - 1];
        if (index > 0) {
            System.arraycopy(_values, 0, newValues, 0, index);
            System.arraycopy(_keys, 0, newKeys, 0, index);
            System.arraycopy(_hashCodes, 0, newHashCodes, 0, index);
        }

        final int remaining = size - index - 1;
        if (remaining > 0) {
            System.arraycopy(_values, index + 1, newValues, index, remaining);
            System.arraycopy(_keys, index + 1, newKeys, index, remaining);
            System.arraycopy(_hashCodes, index + 1, newHashCodes, index, remaining);
        }

        return new ImmutableIntValueHashMap<>(newKeys, newHashCodes, newValues);
    }

    @Override
    public ImmutableIntValueHashMap<T> slice(ImmutableIntRange range) {
        final int size = _values.length;
        final int min = range.min();
        final int max = range.max();
        if (min <= 0 && max >= size - 1) {
            return this;
        }

        if (min >= size || max < 0) {
            return ImmutableIntValueHashMap.empty();
        }

        final int newSize = Math.min(max, size - 1) - min + 1;
        final Object[] newKeys = new Object[newSize];
        final int[] newHashCodes = new int[newSize];
        final int[] newValues = new int[newSize];
        System.arraycopy(_keys, min, newKeys, 0, newSize);
        System.arraycopy(_hashCodes, min, newHashCodes, 0, newSize);
        System.arraycopy(_values, min, newValues, 0, newSize);

        return new ImmutableIntValueHashMap<>(newKeys, newHashCodes, newValues);
    }

    public static class Builder<E> implements ImmutableIntValueMap.Builder<E> {
        final MutableIntValueHashMap<E> _map;

        public Builder() {
            _map = MutableIntValueHashMap.empty();
        }

        public Builder(ArrayLengthFunction arrayLengthFunction) {
            _map = MutableIntValueHashMap.empty(arrayLengthFunction);
        }

        public Builder<E> put(E key, int value) {
            _map.put(key, value);
            return this;
        }

        public Builder<E> putAll(IntValueMap<E> map) {
            final int length = map.size();
            for (int i = 0; i < length; i++) {
                put(map.keyAt(i), map.valueAt(i));
            }

            return this;
        }

        public ImmutableIntValueHashMap<E> build() {
            return _map.toImmutable();
        }
    }
}

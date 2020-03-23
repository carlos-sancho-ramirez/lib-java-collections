package sword.collections;

import static sword.collections.SortUtils.equal;
import static sword.collections.SortUtils.findSuitableIndex;
import static sword.collections.SortUtils.findValue;

/**
 * Efficient implementation for map where keys are internally
 * sorted using a {@link SortFunction} given in construction time.
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
public final class ImmutableIntValueSortedMap<T> extends AbstractImmutableIntValueMap<T> {

    private final SortFunction<T> _sortFunction;

    ImmutableIntValueSortedMap(SortFunction<T> sortFunction, Object[] keys, int[] values) {
        super(keys, values);
        _sortFunction = sortFunction;
    }

    @Override
    public int indexOfKey(T key) {
        return findValue(_sortFunction, _keys, _keys.length, key);
    }

    @Override
    public ImmutableSortedSet<T> keySet() {
        return new ImmutableSortedSet<>(_sortFunction, _keys);
    }

    @Override
    public ImmutableIntValueMap<T> put(T key, int value) {
        int index = findValue(_sortFunction, _keys, _keys.length, key);
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

                return new ImmutableIntValueSortedMap<>(_sortFunction, _keys, newValues);
            }
        }
        else {
            index = findSuitableIndex(_sortFunction, _keys, _keys.length, key);

            final int newLength = _values.length + 1;
            final Object[] newKeys = new Object[newLength];
            final int[] newValues = new int[newLength];

            for (int i = 0; i < newLength; i++) {
                newKeys[i] = (i < index)? _keys[i] : (i == index)? key : _keys[i - 1];
                newValues[i] = (i < index)? _values[i] : (i == index)? value : _values[i - 1];
            }

            return new ImmutableIntValueSortedMap<>(_sortFunction, newKeys, newValues);
        }
    }

    @Override
    boolean entryLessThan(Entry<T> a, Entry<T> b) {
        return b != null && (a == null || _sortFunction.lessThan(a.key(), b.key()));
    }

    @Override
    public ImmutableIntValueSortedMap<T> toImmutable() {
        return this;
    }

    @Override
    public MutableIntValueSortedMap<T> mutate() {
        final int length = _keys.length;
        final int newLength = MutableIntValueSortedMap.suitableArrayLength(length);
        Object[] newKeys = new Object[newLength];
        int[] newValues = new int[newLength];

        if (length > 0) {
            System.arraycopy(_keys, 0, newKeys, 0, length);
            System.arraycopy(_values, 0, newValues, 0, length);
        }

        return new MutableIntValueSortedMap<>(_sortFunction, newKeys, newValues, length);
    }

    @Override
    public ImmutableIntSet toSet() {
        return toList().toSet();
    }

    @Override
    public ImmutableIntValueSortedMap<T> filter(IntPredicate predicate) {
        final Builder<T> builder = new Builder<>(_sortFunction);
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
    public ImmutableIntValueSortedMap<T> filterNot(IntPredicate predicate) {
        final Builder<T> builder = new Builder<>(_sortFunction);
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
    public ImmutableIntValueSortedMap<T> mapToInt(IntToIntFunction mapFunc) {
        final int itemCount = _keys.length;
        final int[] newValues = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableIntValueSortedMap<>(_sortFunction, _keys, newValues);
    }

    @Override
    public <U> ImmutableSortedMap<T, U> map(IntFunction<U> mapFunc) {
        final int itemCount = _keys.length;
        final Object[] newValues = new Object[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableSortedMap<>(_sortFunction, _keys, newValues);
    }

    @Override
    public ImmutableIntValueSortedMap<T> removeAt(int index) {
        final int size = _values.length;
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        final Object[] newKeys = new Object[size - 1];
        final int[] newValues = new int[size - 1];
        if (index > 0) {
            System.arraycopy(_values, 0, newValues, 0, index);
            System.arraycopy(_keys, 0, newKeys, 0, index);
        }

        final int remaining = size - index - 1;
        if (remaining > 0) {
            System.arraycopy(_values, index + 1, newValues, index, remaining);
            System.arraycopy(_keys, index + 1, newKeys, index, remaining);
        }

        return new ImmutableIntValueSortedMap<>(_sortFunction, newKeys, newValues);
    }

    public static class Builder<E> implements ImmutableIntValueMap.Builder<E> {
        final MutableIntValueSortedMap<E> _map;

        Builder(SortFunction<E> sortFunction) {
            final int length = MutableIntValueSortedMap.suitableArrayLength(0);
            _map = new MutableIntValueSortedMap<>(sortFunction, new Object[length], new int[length], 0);
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

        public ImmutableIntValueSortedMap<E> build() {
            return _map.toImmutable();
        }
    }
}

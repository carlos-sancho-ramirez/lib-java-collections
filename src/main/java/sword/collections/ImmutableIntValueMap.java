package sword.collections;

import static sword.collections.SortUtils.findKey;
import static sword.collections.SortUtils.findSuitableIndex;
import static sword.collections.SortUtils.quickSort;

/**
 * Efficient implementation for an immutable Map where keys are arbitrary
 * data and values are integers. This class implementation must be understood
 * as the reversion of a {@link ImmutableIntKeyMap} class.
 *
 * This Map is immutable, that means that its content cannot be modified once
 * it is created. This also means that, as no algorithms to insert, modify and
 * remove are required, its memory layout can be simplified and its footprint
 * can be reduced in a more optimal way.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should create a builder in order to obtain
 * an instance of it.
 *
 * This implementation assumes that elements inserted are also immutable.
 * It is not guaranteed to work if any of the elements is mutable.
 *
 * This class also implements the {@link java.lang.Iterable} interface, which
 * ensures that the for-each construction can be used.
 *
 * @param <T> Type for the keys within the Map
 */
public final class ImmutableIntValueMap<T> extends AbstractIntValueMap<T> implements IterableImmutableIntCollection {

    private static final ImmutableIntValueMap<Object> EMPTY = new ImmutableIntValueMap<>(new Object[0], new int[0]);

    @SuppressWarnings("unchecked")
    public static <E> ImmutableIntValueMap<E> empty() {
        return (ImmutableIntValueMap<E>) EMPTY;
    }

    private final Object[] _keys;
    private final int[] _hashCodes;
    private final int[] _values;

    private static int[] extractHashCodes(Object[] keys) {
        final int length = keys.length;
        final int[] hashCodes = new int[length];

        for (int i = 0; i < length; i++) {
            final Object key = keys[i];
            hashCodes[i] = SortUtils.hashCode(key);
        }

        return hashCodes;
    }

    ImmutableIntValueMap(Object[] keys, int[] values) {
        this(keys, extractHashCodes(keys), values);
    }

    ImmutableIntValueMap(Object[] keys, int[] hashCodes, int[] values) {
        _keys = keys;
        _values = values;
        _hashCodes = hashCodes;
    }

    @Override
    public int get(T key) {
        final int index = findKey(_hashCodes, _keys, _keys.length, key);
        if (index < 0) {
            throw new UnmappedKeyException();
        }

        return _values[index];
    }

    @Override
    public int get(T key, int defaultValue) {
        final int index = findKey(_hashCodes, _keys, _keys.length, key);
        return (index >= 0)? _values[index] : defaultValue;
    }

    @Override
    public int size() {
        return _keys.length;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T keyAt(int index) {
        return (T) _keys[index];
    }

    @Override
    public int valueAt(int index) {
        return _values[index];
    }

    @Override
    public int indexOfKey(T key) {
        return findKey(_hashCodes, _keys, _keys.length, key);
    }

    @Override
    public ImmutableHashSet<T> keySet() {
        if (_keys.length != 0) {
            return new ImmutableHashSet<>(_keys, _hashCodes);
        }
        else {
            return ImmutableHashSet.empty();
        }
    }

    @Override
    public ImmutableHashSet<Entry<T>> entries() {
        final int length = _keys.length;
        final Entry[] entries = new Entry[length];
        final int[] hashCodes = new int[length];

        for (int index = 0; index < length; index++) {
            entries[index] = new Entry<>(index, _keys[index], _values[index]);
            hashCodes[index] = entries[index].hashCode();
        }

        return new ImmutableHashSet<>(entries, hashCodes);
    }

    @Override
    public ImmutableIntValueMap<T> toImmutable() {
        return this;
    }

    @Override
    public MutableIntValueMap<T> mutate() {
        final int newLength = MutableIntValueMap.suitableArrayLength(_keys.length);
        Object[] newKeys = new Object[newLength];
        int[] newHashCodes = new int[newLength];
        int[] newValues = new int[newLength];

        System.arraycopy(_keys, 0, newKeys, 0, _keys.length);
        System.arraycopy(_hashCodes, 0, newHashCodes, 0, _hashCodes.length);
        System.arraycopy(_values, 0, newValues, 0, _values.length);

        return new MutableIntValueMap<>(newKeys, newHashCodes, newValues, _keys.length);
    }

    @Override
    public ImmutableIntValueMap<T> filter(IntPredicate predicate) {
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
    public ImmutableIntValueMap<T> filterNot(IntPredicate predicate) {
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
    public ImmutableIntValueMap<T> map(IntToIntFunction mapFunc) {
        final int itemCount = _keys.length;
        final int[] newValues = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableIntValueMap<>(_keys, _hashCodes, newValues);
    }

    @Override
    public <U> ImmutableMap<T, U> map(IntFunction<U> mapFunc) {
        final int itemCount = _keys.length;
        final Object[] newValues = new Object[itemCount];
        for (int i = 0; i < itemCount; i++) {
            newValues[i] = mapFunc.apply(valueAt(i));
        }

        return new ImmutableMap<>(_keys, _hashCodes, newValues);
    }

    public ImmutableIntKeyMap<T> reverse() {
        // TODO: Ensure that no repeated keys are going inside the reversed version
        final int length = _values.length;
        if (length > 1) {
            final int[] values = new int[length];
            final Object[] keys = new Object[length];

            for (int i = 0; i < length; i++) {
                values[i] = _values[i];
                keys[i] = _keys[i];
            }

            quickSort(values, 0, length - 1, new SortUtils.SwapMethod() {
                @Override
                public void apply(int index1, int index2) {
                    int temp = values[index1];
                    values[index1] = values[index2];
                    values[index2] = temp;

                    Object aux = keys[index1];
                    keys[index1] = keys[index2];
                    keys[index2] = aux;
                }
            });
            return new ImmutableIntKeyMap<>(values, keys);
        }
        else {
            return new ImmutableIntKeyMap<>(_values, _keys);
        }
    }

    private class Iterator extends IteratorForImmutable<Integer> {

        private int _index;

        @Override
        public boolean hasNext() {
            return _index < _keys.length;
        }

        @Override
        public Integer next() {
            return _values[_index++];
        }
    }

    @Override
    public java.util.Iterator<Integer> iterator() {
        return new Iterator();
    }

    public static class Builder<E> implements IntValueMapBuilder<E> {
        private static final int GRANULARITY = 12;
        private int _size;

        private Object[] _keys = new Object[GRANULARITY];
        private int[] _hashCodes = new int[GRANULARITY];
        private int[] _values = new int[GRANULARITY];

        private void enlargeArrays() {
            Object[] oldKeys = _keys;
            int[] oldHashCodes = _hashCodes;
            int[] oldValues = _values;

            _keys = new Object[_size + GRANULARITY];
            _hashCodes = new int[_size + GRANULARITY];
            _values = new int[_size + GRANULARITY];

            for (int i = 0; i < _size; i++) {
                _keys[i] = oldKeys[i];
                _hashCodes[i] = oldHashCodes[i];
                _values[i] = oldValues[i];
            }
        }

        public Builder<E> put(E key, int value) {
            int index = findKey(_hashCodes, _keys, _size, key);
            if (index >= 0) {
                _values[index] = value;
            }
            else {
                if (_size != 0 && _size % GRANULARITY == 0) {
                    enlargeArrays();
                }

                final int hashCode = SortUtils.hashCode(key);
                index = findSuitableIndex(_hashCodes, _size, hashCode);
                for (int i = _size; i > index; i--) {
                    _keys[i] = _keys[i - 1];
                    _hashCodes[i] = _hashCodes[i - 1];
                    _values[i] = _values[i - 1];
                }

                _keys[index] = key;
                _values[index] = value;
                _hashCodes[index] = hashCode;

                _size++;
            }

            return this;
        }

        public Builder<E> putAll(ImmutableIntValueMap<E> map) {
            final int length = map.size();
            for (int i = 0; i < length; i++) {
                put(map.keyAt(i), map.valueAt(i));
            }

            return this;
        }

        public ImmutableIntValueMap<E> build() {
            final int length = _size;
            if (length == 0) {
                return ImmutableIntValueMap.empty();
            }
            else {
                final Object[] keys = new Object[length];
                final int[] hashCodes = new int[length];
                final int[] values = new int[length];

                for (int i = 0; i < length; i++) {
                    keys[i] = _keys[i];
                    hashCodes[i] = _hashCodes[i];
                    values[i] = _values[i];
                }

                return new ImmutableIntValueMap<E>(keys, hashCodes, values);
            }
        }
    }
}

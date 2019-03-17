package sword.collections;

import static sword.collections.SortUtils.DEFAULT_GRANULARITY;
import static sword.collections.SortUtils.findKey;

/**
 * Efficient implementation for small mutable Set where elements are internally sorted by its hash code.
 *
 * This implementation may not be efficient enough for big sets as insertion will
 * become slow as this increases.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should get a new instance through the empty method.
 *
 * This implementation assumes that elements inserted are immutable, then its hashCode will not change.
 * It is not guaranteed to work if any of the elements is mutable.
 *
 * @param <T> Type for the elements within the Set
 */
public final class MutableHashSet<T> extends AbstractMutableSet<T> {

    private static final int GRANULARITY = DEFAULT_GRANULARITY;

    public static <E> MutableHashSet<E> empty() {
        return new MutableHashSet<>(new Object[GRANULARITY], new int[GRANULARITY], 0);
    }

    static int suitableArrayLength(int size) {
        int s = ((size + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    private int[] _hashCodes;

    MutableHashSet(Object[] keys, int[] hashCodes, int size) {
        super(keys, size);
        _hashCodes = hashCodes;
    }

    @Override
    public int indexOf(T value) {
        return findKey(_hashCodes, values, _size, value);
    }

    @Override
    public <E> Map<T, E> assign(Function<T, E> function) {
        final int size = _size;
        if (size == 0) {
            return ImmutableHashMap.empty();
        }

        final Object[] keys = new Object[size];
        final int[] hashCodes = new int[size];
        final Object[] values = new Object[size];

        for (int i = 0; i < size; i++) {
            final T key = valueAt(i);
            keys[i] = key;
            hashCodes[i] = _hashCodes[i];
            values[i] = function.apply(key);
        }

        return new ImmutableHashMap<>(keys, hashCodes, values);
    }

    @Override
    public ImmutableHashSet<T> toImmutable() {
        Object[] keys = new Object[_size];
        int[] hashCodes = new int[_size];

        System.arraycopy(values, 0, keys, 0, _size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);

        return new ImmutableHashSet<>(keys, hashCodes);
    }

    @Override
    public MutableHashSet<T> mutate() {
        Object[] keys = new Object[values.length];
        int[] hashCodes = new int[_hashCodes.length];

        System.arraycopy(values, 0, keys, 0, _size);
        System.arraycopy(_hashCodes, 0, hashCodes, 0, _size);

        return new MutableHashSet<>(keys, hashCodes, _size);
    }

    private void enlargeArrays() {
        Object[] oldKeys = values;
        int[] oldHashCodes = _hashCodes;

        values = new Object[_size + GRANULARITY];
        _hashCodes = new int[_size + GRANULARITY];

        for (int i = 0; i < _size; i++) {
            values[i] = oldKeys[i];
            _hashCodes[i] = oldHashCodes[i];
        }
    }

    @Override
    int findSuitableIndex(T key) {
        return SortUtils.findSuitableIndex(_hashCodes, _size, SortUtils.hashCode(key));
    }

    @Override
    void insertAt(int index, T value) {
        if (_size != 0 && _size % GRANULARITY == 0) {
            enlargeArrays();
        }

        for (int i = _size; i > index; i--) {
            values[i] = values[i - 1];
            _hashCodes[i] = _hashCodes[i - 1];
        }

        values[index] = value;
        _hashCodes[index] = SortUtils.hashCode(value);
        _size++;
    }

    @Override
    public void removeAt(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException();
        }

        if (_size != 1 && (_size % GRANULARITY) == 1) {
            Object[] oldKeys = values;
            int[] oldHashCodes = _hashCodes;

            values = new Object[--_size];
            _hashCodes = new int[_size];

            if (index > 0) {
                System.arraycopy(oldKeys, 0, values, 0, index);
                System.arraycopy(oldHashCodes, 0, _hashCodes, 0, index);
            }

            if (_size > index) {
                System.arraycopy(oldKeys, index + 1, values, index, _size - index);
                System.arraycopy(oldHashCodes, index + 1, _hashCodes, index, _size - index);
            }
        }
        else {
            --_size;
            for (int i = index; i < _size; i++) {
                values[i] = values[i + 1];
                _hashCodes[i] = _hashCodes[i + 1];
            }
        }
    }

    @Override
    public boolean clear() {
        final int suitableLength = suitableArrayLength(0);
        if (values.length != suitableLength) {
            values = new Object[suitableLength];
            _hashCodes = new int[suitableLength];
        }
        else {
            for (int i = 0; i < _size; i++) {
                values[i] = null;
            }
        }

        final boolean changed = _size > 0;
        _size = 0;

        return changed;
    }

    public static class Builder<E> implements MutableSet.Builder<E> {
        private final MutableHashSet<E> _set = MutableHashSet.empty();

        @Override
        public Builder<E> add(E key) {
            _set.add(key);
            return this;
        }

        @Override
        public MutableHashSet<E> build() {
            return _set;
        }
    }

    @Override
    int itemHashCode(int index) {
        return _hashCodes[index];
    }
}

package sword.collections;

import java.util.Arrays;

/**
 * Efficient implementation for an immutable Set when few positive integers with lower value are included.
 * 'Set' must be understood as a collection where its elements cannot be repeated.
 *
 * This Set is immutable, that means that its content cannot be modified once
 * it is created. This also means that, as no algorithms to insert, modify and
 * remove are required, its memory layout can be simplified and its footprint
 * can be reduced in a more optimal way.
 *
 * Constructors of this class are intentionally private or package-protected.
 * Code using these collections should create a builder in order to obtain
 * an instance of it.
 *
 * This class also implements the {@link Iterable} interface, which
 * ensures that the for-each construction can be used.
 */
final class ImmutableBitSetImpl extends AbstractImmutableIntSet {

    public static ImmutableIntSet empty() {
        return ImmutableIntSetImpl.empty();
    }

    static final int OFFSET_BITS_IN_INDEX = 5; // int has 32 bits in Java. 1 << 5 == 32
    private static final int OFFSET_MASK = (1 << OFFSET_BITS_IN_INDEX) - 1;

    private final int[] _value;
    private transient int _size = -1;

    private ImmutableBitSetImpl(int[] value) {
        _value = value;
    }

    @Override
    public boolean contains(int value) {
        if (_value == null || value < 0) {
            return false;
        }

        final int wordIndex = value >>> OFFSET_BITS_IN_INDEX;
        if (wordIndex >= _value.length) {
            return false;
        }

        final int mask = 1 << (value & OFFSET_MASK);
        return (_value[wordIndex] & mask) != 0;
    }

    @Override
    public int size() {
        if (_size != -1) {
            return _size;
        }

        if (_value == null) {
            _size = 0;
            return 0;
        }

        final int length = _value.length;
        int sum = 0;
        for (int wordIndex = 0; wordIndex < length; wordIndex++) {
            final int wordValue = _value[wordIndex];
            int mask = 1;
            while (mask != 0) {
                if ((wordValue & mask) != 0) {
                    ++sum;
                }

                mask <<= 1;
            }
        }

        _size = sum;
        return sum;
    }

    @Override
    public ImmutableIntSet add(int value) {
        if (contains(value)) {
            return this;
        }

        final int currentSize = size();
        if (currentSize == 0) {
            int[] values = new int[1];
            values[0] = value;
            return new ImmutableIntSetImpl(values);
        }

        final int newMin = Math.min(min(), value);
        final int newMax = Math.max(max(), value);
        final int newSize = currentSize + 1;

        if (ImmutableIntSetBuilder.betterAsBitSet(newMin, newMax, newSize)) {
            final int bitsPerWord = 1 << OFFSET_BITS_IN_INDEX;
            final int newLength = newMax / bitsPerWord + 1;
            final int[] values = new int[newLength];

            if (_value != null) {
                System.arraycopy(_value, 0, values, 0, _value.length);
            }

            final int newWordMask = 1 << (value % bitsPerWord);
            final int valueIndex = value / bitsPerWord;
            values[valueIndex] |= newWordMask;
            return new ImmutableBitSetImpl(values);
        }
        else {
            int[] values = new int[newSize];
            int intSetIndex = 0;
            for (int v : this) {
                if (value < v) {
                    values[intSetIndex++] = value;
                }
                values[intSetIndex++] = v;
            }

            if (intSetIndex < newSize) {
                values[intSetIndex] = value;
            }
            return new ImmutableIntSetImpl(values);
        }
    }

    @Override
    public ImmutableBitSetImpl remove(int value) {
        if (value < 0) {
            throw new IllegalArgumentException();
        }

        if (!contains(value)) {
            return this;
        }

        final int wordIndex = value >> OFFSET_BITS_IN_INDEX;
        final int valueMask = 1 << (value & OFFSET_MASK);
        final int newWord = _value[wordIndex] & (~valueMask);

        int newLength = _value.length;
        if (wordIndex == newLength - 1 && newWord == 0) {
            --newLength;
            while (newLength > 0 && _value[newLength - 1] == 0) {
                --newLength;
            }
        }

        if (newLength > 0) {
            final int[] newValue = Arrays.copyOf(_value, newLength);
            if (wordIndex < newLength) {
                newValue[wordIndex] = newWord;
            }

            return new ImmutableBitSetImpl(newValue);
        }

        return new ImmutableBitSetImpl(null);
    }

    @Override
    public ImmutableIntList toList() {
        final ImmutableIntList.Builder builder = new ImmutableIntList.Builder();
        for (int value : this) {
            builder.add(value);
        }

        return builder.build();
    }

    @Override
    public int valueAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }

        int wordIndex = 0;
        int value = 0;
        int bitMask = 1;
        while (true) {
            if ((_value[wordIndex] & bitMask) != 0) {
                if (index == 0) {
                    return value;
                }
                else {
                    index--;
                }
            }
            ++value;
            if ((value & OFFSET_MASK) == 0) {
                ++wordIndex;
                bitMask = 1;
            }
            else {
                bitMask <<= 1;
            }
        }
    }

    @Override
    public int min() throws EmptyCollectionException {
        java.util.Iterator<Integer> it = iterator();
        if (!it.hasNext()) {
            throw new EmptyCollectionException();
        }

        return it.next();
    }

    @Override
    public int max() throws EmptyCollectionException {
        int wordIndex = _value.length - 1;
        int offset = OFFSET_MASK;
        while (wordIndex >= 0) {
            final int value = _value[wordIndex];
            while (offset >= 0) {
                if ((value & (1 << offset)) != 0) {
                    return wordIndex * (OFFSET_MASK + 1) + offset;
                }
                --offset;
            }
            --wordIndex;
        }

        throw new EmptyCollectionException();
    }

    @Override
    public ImmutableIntSet toImmutable() {
        return this;
    }

    @Override
    public MutableIntSet mutate() {
        return MutableIntSet.fromIntSet(this);
    }

    private class Iterator extends IteratorForImmutable<Integer> {

        private int _wordIndex;
        private int _offset;
        private int _nextValue;

        private Iterator() {
            findFirst();
        }

        private void findFirst() {
            boolean found = false;
            if (_value != null) {
                while (!found && _wordIndex < _value.length) {
                    final int value = _value[_wordIndex];
                    while (!found && _offset <= OFFSET_MASK) {
                        found = (value & (1 << _offset)) != 0;

                        if (!found) {
                            ++_offset;
                        }
                    }

                    if (_offset > OFFSET_MASK) {
                        _offset = 0;
                        ++_wordIndex;
                    }
                }
            }

            _nextValue = found? (_wordIndex << OFFSET_BITS_IN_INDEX) + _offset : -1;
        }

        @Override
        public boolean hasNext() {
            return _nextValue != -1;
        }

        @Override
        public Integer next() {
            final int result = _nextValue;
            ++_offset;
            findFirst();

            return result;
        }
    }

    @Override
    public java.util.Iterator<Integer> iterator() {
        return new Iterator();
    }

    static class Builder implements ImmutableIntSet.Builder {
        private int[] _value;

        @Override
        public Builder add(int value) {
            if (value < 0) {
                throw new IllegalArgumentException();
            }

            final int wordIndex = value >> OFFSET_BITS_IN_INDEX;
            if (_value == null) {
                _value = new int[wordIndex + 1];
            }
            else if (_value.length <= wordIndex) {
                _value = Arrays.copyOf(_value, wordIndex + 1);
            }

            _value[wordIndex] |= 1 << (value & OFFSET_MASK);
            return this;
        }

        @Override
        public ImmutableBitSetImpl build() {
            return new ImmutableBitSetImpl(_value);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof ImmutableBitSetImpl)) {
            return super.equals(object);
        }
        else if (this == object) {
            return true;
        }

        final ImmutableBitSetImpl that = (ImmutableBitSetImpl) object;
        final int[] thatValue = that._value;
        final int length = _value.length;
        if (length != thatValue.length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            if (_value[i] != thatValue[i]) {
                return false;
            }
        }

        return true;
    }
}

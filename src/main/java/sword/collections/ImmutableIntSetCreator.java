package sword.collections;

/**
 * Builder to create a new instance implementing ImmutableIntSet interface.
 *
 * This builder will automatically choose among all possible
 * implementations of ImmutableIntSet the more efficient in terms of memory
 * usage for the provided values.
 *
 * If it is desired an specific implementation, implementation's builder
 * should be used instead.
 */
public final class ImmutableIntSetCreator implements ImmutableIntSet.Builder {

    private final MutableIntSet _set = MutableIntSet.empty();

    @Override
    public ImmutableIntSet.Builder add(int value) {
        _set.add(value);
        return this;
    }

    private static boolean betterAsRange(IntSet set) {
        final int size = set.size();
        return size >= 3 && size == set.max() - set.min() + 1;
    }

    static boolean betterAsBitSet(int min, int max, int count) {
        if (min < 0) {
            return false;
        }

        final int bitsPerWord = 1 << ImmutableBitSetImpl.OFFSET_BITS_IN_INDEX;
        final int requiredWords = max / bitsPerWord + 1;
        return requiredWords < count;
    }

    static ImmutableIntSet fromMutableIntSet(MutableIntSet set) {
        if (betterAsRange(set)) {
            return new ImmutableIntRange(set.min(), set.max());
        }

        final int setSize = set.size();
        final ImmutableIntSet.Builder builder = (setSize > 0 && betterAsBitSet(set.min(), set.max(), setSize))?
                new ImmutableBitSetImpl.Builder() : new ImmutableIntSetImpl.Builder();
        for (int value : set) {
            builder.add(value);
        }

        return builder.build();
    }

    @Override
    public ImmutableIntSet build() {
        return fromMutableIntSet(_set);
    }
}

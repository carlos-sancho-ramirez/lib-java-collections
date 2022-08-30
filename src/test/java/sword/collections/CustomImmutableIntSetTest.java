package sword.collections;

final class CustomImmutableIntSetTest extends ImmutableIntSetTest {

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntSet.Builder>> procedure) {
        procedure.apply(CustomImmutableIntSetBuilder::new);
    }

    @Override
    public ImmutableIntSet.Builder newIntBuilder() {
        return new CustomImmutableIntSetBuilder();
    }

    private static final class CustomImmutableIntSetBuilder implements ImmutableIntSet.Builder {

        private final ImmutableIntSetCreator mBuilder = new ImmutableIntSetCreator();

        @Override
        public CustomImmutableIntSetBuilder add(int value) {
            mBuilder.add(value);
            return this;
        }

        @Override
        public ImmutableIntSet build() {
            return new CustomImmutableIntSet(mBuilder.build());
        }
    }

    private static final class CustomImmutableIntSet implements ImmutableIntSet {

        private final ImmutableIntSet mSet;

        CustomImmutableIntSet(ImmutableIntSet set) {
            mSet = set;
        }

        @Override
        public IntTransformer iterator() {
            return mSet.iterator();
        }

        @Override
        public ImmutableIntList toList() {
            return mSet.toList();
        }

        @Override
        public ImmutableIntSet toSet() {
            return this;
        }

        @Override
        public ImmutableIntPairMap count() {
            return mSet.count();
        }

        @Override
        public ImmutableIntSet filter(IntPredicate predicate) {
            final ImmutableIntSet newSet = mSet.filter(predicate);
            return (newSet == mSet)? this : new CustomImmutableIntSet(newSet);
        }

        @Override
        public ImmutableIntSet filterNot(IntPredicate predicate) {
            return filter(v -> !predicate.apply(v));
        }

        @Override
        public ImmutableIntList mapToInt(IntToIntFunction mapFunc) {
            return mSet.mapToInt(mapFunc);
        }

        @Override
        public ImmutableIntSet toImmutable() {
            return this;
        }

        @Override
        public MutableIntSet mutate() {
            return mSet.mutate();
        }

        @Override
        public <U> ImmutableList<U> map(IntFunction<? extends U> mapFunc) {
            return mSet.map(mapFunc);
        }

        @Override
        public <V> ImmutableIntKeyMap<V> assign(IntFunction<? extends V> function) {
            return mSet.assign(function);
        }

        @Override
        public ImmutableIntPairMap assignToInt(IntToIntFunction function) {
            return mSet.assignToInt(function);
        }

        @Override
        public ImmutableIntSet removeAt(int index) {
            return new CustomImmutableIntSet(mSet.removeAt(index));
        }

        @Override
        public ImmutableIntSet remove(int value) {
            final ImmutableIntSet newSet = mSet.remove(value);
            return (newSet == mSet)? this : new CustomImmutableIntSet(newSet);
        }

        @Override
        public ImmutableIntSet add(int value) {
            final ImmutableIntSet newSet = mSet.add(value);
            return (newSet == mSet)? this : new CustomImmutableIntSet(newSet);
        }

        @Override
        public int hashCode() {
            return mSet.hashCode();
        }

        @Override
        public boolean equalSet(IntSet set) {
            return mSet.equalSet(set);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof CustomImmutableIntSet)) {
                return false;
            }

            return mSet.equals(((CustomImmutableIntSet) other).mSet);
        }
    }
}

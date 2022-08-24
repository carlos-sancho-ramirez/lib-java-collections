package sword.collections;

import static sword.collections.TestUtils.withString;

final class CustomImmutableSetTest extends ImmutableSetTest<String, ImmutableSet.Builder<String>> {

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableSet.Builder<String>>> procedure) {
        procedure.apply(CustomImmutableSetBuilder::new);
    }

    private boolean hashCodeIsEven(Object value) {
        return value == null || (value.hashCode() & 1) == 0;
    }

    @Override
    boolean lessThan(String a, String b) {
        return b != null && (a == null || a.hashCode() < b.hashCode());
    }

    @Override
    public void withSortFunc(Procedure<SortFunction<String>> procedure) {
        procedure.apply(SortUtils::compareByHashCode);
    }

    @Override
    public void withValue(Procedure<String> procedure) {
        withString(procedure);
    }

    @Override
    public void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::hashCodeIsEven);
    }

    @Override
    public void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
        procedure.apply((a, b) -> a + b);
    }

    private String prefixUnderscore(String value) {
        return "_" + value;
    }

    private String charCounter(String value) {
        final int length = (value != null)? value.length() : 0;
        return Integer.toString(length);
    }

    @Override
    public void withMapFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(this::prefixUnderscore);
        procedure.apply(this::charCounter);
    }

    @Override
    public void withMapToIntFunc(Procedure<IntResultFunction<String>> procedure) {
        procedure.apply(str -> (str == null)? 0 : str.hashCode());
    }

    private static final class CustomImmutableSetBuilder<T> implements ImmutableSet.Builder<T> {

        private final ImmutableHashSet.Builder<T> mBuilder = new ImmutableHashSet.Builder<>();

        @Override
        public CustomImmutableSetBuilder<T> add(T value) {
            mBuilder.add(value);
            return this;
        }

        @Override
        public ImmutableSet<T> build() {
            return new CustomImmutableSet<>(mBuilder.build());
        }
    }

    private static final class CustomImmutableSet<T> implements ImmutableSet<T> {

        private final ImmutableHashSet<T> mSet;

        CustomImmutableSet(ImmutableHashSet<T> set) {
            mSet = set;
        }

        @Override
        public Transformer<T> iterator() {
            return mSet.iterator();
        }

        @Override
        public ImmutableList<T> toList() {
            return mSet.toList();
        }

        @Override
        public ImmutableSet<T> toSet() {
            return this;
        }

        @Override
        public ImmutableIntSet indexes() {
            return mSet.indexes();
        }

        @Override
        public ImmutableIntValueMap<T> count() {
            return mSet.count();
        }

        @Override
        public ImmutableSet<T> filter(Predicate<? super T> predicate) {
            final ImmutableHashSet<T> newSet = mSet.filter(predicate);
            return (newSet == mSet)? this : new CustomImmutableSet<>(newSet);
        }

        @Override
        public ImmutableIntList mapToInt(IntResultFunction<? super T> mapFunc) {
            return mSet.mapToInt(mapFunc);
        }

        @Override
        public ImmutableSet<T> toImmutable() {
            return this;
        }

        @Override
        public MutableSet<T> mutate() {
            return mSet.mutate();
        }

        @Override
        public MutableSet<T> mutate(ArrayLengthFunction arrayLengthFunction) {
            return mSet.mutate(arrayLengthFunction);
        }

        @Override
        public <U> ImmutableList<U> map(Function<? super T, ? extends U> mapFunc) {
            return mSet.map(mapFunc);
        }

        @Override
        public <V> ImmutableMap<T, V> assign(Function<? super T, ? extends V> function) {
            return mSet.assign(function);
        }

        @Override
        public ImmutableIntValueMap<T> assignToInt(IntResultFunction<? super T> function) {
            return mSet.assignToInt(function);
        }

        @Override
        public ImmutableSortedSet<T> sort(SortFunction<? super T> function) {
            return mSet.sort(function);
        }

        @Override
        public ImmutableSet<T> removeAt(int index) {
            return new CustomImmutableSet<>(mSet.removeAt(index));
        }

        @Override
        public ImmutableSet<T> remove(T value) {
            final ImmutableHashSet<T> newSet = mSet.remove(value);
            return (newSet == mSet)? this : new CustomImmutableSet<>(newSet);
        }

        @Override
        public ImmutableSet<T> add(T value) {
            final ImmutableHashSet<T> newSet = mSet.add(value);
            return (newSet == mSet)? this : new CustomImmutableSet<>(newSet);
        }

        @Override
        public int hashCode() {
            return mSet.hashCode();
        }

        @Override
        public boolean equalSet(Set set) {
            return mSet.equalSet(set);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof CustomImmutableSet)) {
                return false;
            }

            return mSet.equals(((CustomImmutableSet) other).mSet);
        }
    }
}

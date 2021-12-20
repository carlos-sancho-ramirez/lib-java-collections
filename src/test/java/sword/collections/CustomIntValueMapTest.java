package sword.collections;

import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class CustomIntValueMapTest implements IntValueMapTest<String, IntTransformableBuilder> {

    @Override
    public IntValueMap.Builder<String> newBuilder() {
        return new CustomIntValueMapBuilder<>();
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<String, IntValueMap.Builder<String>>> procedure) {
        procedure.apply(CustomIntValueMapBuilder::new);
    }

    @Override
    public void withKey(Procedure<String> procedure) {
        withString(procedure);
    }

    @Override
    public void withSortFunc(Procedure<SortFunction<String>> procedure) {
        procedure.apply((a, b) -> ("_" + a).hashCode() < ("_" + b).hashCode());
    }

    @Override
    public void withFilterByKeyFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(v -> v == null || (v.hashCode() & 1) == 0);
    }

    @Override
    public String keyFromInt(int value) {
        return "_" + value;
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<IntTransformableBuilder>> procedure) {
        procedure.apply(HashCodeKeyTraversableBuilder::new);
    }

    @Override
    public IntTransformableBuilder newIntBuilder() {
        return new HashCodeKeyTraversableBuilder();
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withInt(procedure);
    }

    @Override
    public void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
        procedure.apply(v -> "ax" + v);
    }

    @Override
    public void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(a -> a + 7);
    }

    private boolean valueIsEven(int value) {
        return (value & 1) == 0;
    }

    @Override
    public void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::valueIsEven);
    }

    private static final class HashCodeKeyTraversableBuilder implements IntTransformableBuilder {
        private final MutableIntValueMap<Integer> builder = MutableIntValueHashMap.empty();

        @Override
        public HashCodeKeyTraversableBuilder add(int element) {
            builder.put(element, element);
            return this;
        }

        @Override
        public CustomIntValueMap<Integer> build() {
            return new CustomIntValueMap<>(builder);
        }
    }

    private static final class CustomIntValueMapBuilder<E> implements IntValueMap.Builder<E> {

        private final MutableIntValueMap<E> mMap = MutableIntValueHashMap.empty();

        @Override
        public CustomIntValueMapBuilder<E> put(E key, int value) {
            mMap.put(key, value);
            return this;
        }

        @Override
        public CustomIntValueMap<E> build() {
            return new CustomIntValueMap<>(mMap);
        }
    }

    private static final class CustomIntValueMap<E> implements IntValueMap<E> {
        private final IntValueMap<E> mMap;

        CustomIntValueMap(IntValueMap<E> map) {
            mMap = map;
        }

        @Override
        public int get(E key) throws UnmappedKeyException {
            return mMap.get(key);
        }

        @Override
        public int get(E key, int defaultValue) {
            return mMap.get(key, defaultValue);
        }

        @Override
        public E keyAt(int index) {
            return mMap.keyAt(index);
        }

        @Override
        public int valueAt(int index) {
            return mMap.valueAt(index);
        }

        @Override
        public int indexOfKey(E key) {
            return mMap.indexOfKey(key);
        }

        @Override
        public Set<E> keySet() {
            return mMap.keySet();
        }

        @Override
        public Set<Entry<E>> entries() {
            return mMap.entries();
        }

        @Override
        public IntTransformer iterator() {
            return mMap.iterator();
        }

        @Override
        public IntList toList() {
            return mMap.toList();
        }

        @Override
        public IntValueMap<E> filter(IntPredicate predicate) {
            final IntValueMap<E> newMap = mMap.filter(predicate);
            return (newMap == mMap)? this : new CustomIntValueMap<>(newMap);
        }

        @Override
        public <U> Map<E, U> map(IntFunction<? extends U> func) {
            return mMap.map(func);
        }

        @Override
        public IntValueMap<E> mapToInt(IntToIntFunction func) {
            final IntValueMap<E> newMap = mMap.mapToInt(func);
            return (newMap == mMap)? this : new CustomIntValueMap<>(newMap);
        }

        @Override
        public ImmutableIntValueMap<E> toImmutable() {
            return mMap.toImmutable();
        }

        @Override
        public MutableIntValueMap<E> mutate() {
            return mMap.mutate();
        }

        @Override
        public MutableIntValueMap<E> mutate(ArrayLengthFunction arrayLengthFunction) {
            return mMap.mutate(arrayLengthFunction);
        }

        @Override
        public IntValueMap<E> sort(SortFunction<? super E> function) {
            return mMap.sort(function);
        }
    }
}

package sword.collections;

import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class CustomImmutableIntValueMapTest implements ImmutableIntValueMapTest<String, ImmutableIntTransformableBuilder> {

    @Override
    public ImmutableIntValueMap.Builder<String> newBuilder() {
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
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntTransformableBuilder>> procedure) {
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

    @Override
    public void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::valueIsEven);
    }

    private static final class HashCodeKeyTraversableBuilder implements ImmutableIntTransformableBuilder {
        private final MutableIntValueMap<Integer> map = MutableIntValueHashMap.empty();

        @Override
        public HashCodeKeyTraversableBuilder add(int element) {
            map.put(element, element);
            return this;
        }

        @Override
        public CustomImmutableIntValueMap<Integer> build() {
            return new CustomImmutableIntValueMap<>(map.toImmutable());
        }
    }

    private static final class CustomIntValueMapBuilder<E> implements ImmutableIntValueMap.Builder<E> {

        private final MutableIntValueMap<E> mMap = MutableIntValueHashMap.empty();

        @Override
        public CustomIntValueMapBuilder<E> put(E key, int value) {
            mMap.put(key, value);
            return this;
        }

        @Override
        public CustomImmutableIntValueMap<E> build() {
            return new CustomImmutableIntValueMap<>(mMap.toImmutable());
        }
    }

    private static final class CustomImmutableIntValueMap<E> implements ImmutableIntValueMap<E> {
        private final ImmutableIntValueMap<E> mMap;

        CustomImmutableIntValueMap(ImmutableIntValueMap<E> map) {
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
        public ImmutableSet<E> keySet() {
            return mMap.keySet();
        }

        @Override
        public ImmutableSet<Entry<E>> entries() {
            return mMap.entries();
        }

        @Override
        public ImmutableIntValueMap<E> put(E key, int value) {
            final ImmutableIntValueMap<E> newMap = mMap.put(key, value);
            return (newMap == mMap)? this : new CustomImmutableIntValueMap<>(newMap);
        }

        @Override
        public IntTransformer iterator() {
            return mMap.iterator();
        }

        @Override
        public ImmutableIntList toList() {
            return mMap.toList();
        }

        @Override
        public ImmutableIntSet toSet() {
            return mMap.toSet();
        }

        @Override
        public ImmutableIntValueMap<E> filter(IntPredicate predicate) {
            final ImmutableIntValueMap<E> newMap = mMap.filter(predicate);
            return (newMap == mMap)? this : new CustomImmutableIntValueMap<>(newMap);
        }

        @Override
        public <U> ImmutableMap<E, U> map(IntFunction<? extends U> func) {
            return mMap.map(func);
        }

        @Override
        public ImmutableIntValueMap<E> mapToInt(IntToIntFunction func) {
            final ImmutableIntValueMap<E> newMap = mMap.mapToInt(func);
            return (newMap == mMap)? this : new CustomImmutableIntValueMap<>(newMap);
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
        public ImmutableIntValueMap<E> sort(SortFunction<? super E> function) {
            return mMap.sort(function);
        }

        @Override
        public ImmutableIntValueMap<E> removeAt(int index) {
            return new CustomImmutableIntValueMap<>(mMap.removeAt(index));
        }

        @Override
        public ImmutableIntPairMap count() {
            return mMap.count();
        }

        @Override
        public ImmutableIntKeyMap<E> invert() {
            return mMap.invert();
        }
    }
}

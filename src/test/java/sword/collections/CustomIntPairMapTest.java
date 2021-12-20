package sword.collections;

public final class CustomIntPairMapTest implements IntPairMapTest<IntTransformableBuilder> {

    @Override
    public IntPairMapBuilder newBuilder() {
        return new CustomIntPairMapBuilder();
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntPairMapBuilderSupplier<IntPairMapBuilder>> procedure) {
        procedure.apply(CustomIntPairMapBuilder::new);
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<IntTransformableBuilder>> procedure) {
        procedure.apply(HashCodeKeyTraversableBuilder::new);
    }

    @Override
    public IntTransformableBuilder newIntBuilder() {
        return new HashCodeKeyTraversableBuilder();
    }

    private boolean valueIsEven(int value) {
        return (value & 1) == 0;
    }

    @Override
    public void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::valueIsEven);
    }

    private static final class HashCodeKeyTraversableBuilder implements IntTransformableBuilder {
        private final ImmutableIntPairMap.Builder builder = new ImmutableIntPairMap.Builder();

        @Override
        public HashCodeKeyTraversableBuilder add(int element) {
            builder.put(SortUtils.hashCode(element), element);
            return this;
        }

        @Override
        public CustomIntPairMap build() {
            return new CustomIntPairMap(builder.build());
        }
    }

    private static final class CustomIntPairMapBuilder implements IntPairMapBuilder {

        private final MutableIntPairMap mMap = MutableIntPairMap.empty();

        @Override
        public CustomIntPairMapBuilder put(int key, int value) {
            mMap.put(key, value);
            return this;
        }

        @Override
        public CustomIntPairMap build() {
            return new CustomIntPairMap(mMap);
        }
    }

    private static final class CustomIntPairMap implements IntPairMap {
        private final IntPairMap mMap;

        CustomIntPairMap(IntPairMap map) {
            mMap = map;
        }

        @Override
        public int get(int key) throws UnmappedKeyException {
            return mMap.get(key);
        }

        @Override
        public int get(int key, int defaultValue) {
            return mMap.get(key, defaultValue);
        }

        @Override
        public int keyAt(int index) {
            return mMap.keyAt(index);
        }

        @Override
        public int indexOfKey(int key) {
            return mMap.indexOfKey(key);
        }

        @Override
        public IntSet keySet() {
            return mMap.keySet();
        }

        @Override
        public Set<Entry> entries() {
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
        public IntPairMap filter(IntPredicate predicate) {
            final IntPairMap newMap = mMap.filter(predicate);
            return (newMap == mMap)? this : new CustomIntPairMap(newMap);
        }

        @Override
        public <U> IntKeyMap<U> map(IntFunction<? extends U> func) {
            return mMap.map(func);
        }

        @Override
        public IntPairMap mapToInt(IntToIntFunction func) {
            final IntPairMap newMap = mMap.mapToInt(func);
            return (newMap == mMap)? this : new CustomIntPairMap(newMap);
        }

        @Override
        public ImmutableIntPairMap toImmutable() {
            return mMap.toImmutable();
        }

        @Override
        public MutableIntPairMap mutate() {
            return mMap.mutate();
        }

        @Override
        public MutableIntPairMap mutate(ArrayLengthFunction arrayLengthFunction) {
            return mMap.mutate(arrayLengthFunction);
        }
    }
}

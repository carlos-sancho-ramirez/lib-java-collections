package sword.collections;

import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class CustomImmutableMapTest implements ImmutableMapTest<Integer, String, ImmutableTransformableBuilder<String>, ImmutableMap.Builder<Integer, String>> {
    @Override
    public ImmutableMap.Builder<Integer, String> newBuilder() {
        return new CustomImmutableMapBuilder<>();
    }

    @Override
    public void withKey(Procedure<Integer> procedure) {
        withInt(procedure::apply);
    }

    private boolean hashCodeIsEven(Object value) {
        return value == null || (value.hashCode() & 1) == 0;
    }

    @Override
    public void withFilterByKeyFunc(Procedure<Predicate<Integer>> procedure) {
        procedure.apply(this::hashCodeIsEven);
    }

    @Override
    public void withSortFunc(Procedure<SortFunction<Integer>> procedure) {
        procedure.apply((a, b) -> a < b);
        procedure.apply((a, b) -> a > b);
    }

    @Override
    public String getTestValue() {
        return "value";
    }

    @Override
    public Integer keyFromInt(int value) {
        return value;
    }

    @Override
    public String valueFromKey(Integer key) {
        return "_" + key;
    }

    @Override
    public void withMapBuilderSupplier(Procedure<MapBuilderSupplier<Integer, String, ImmutableMap.Builder<Integer, String>>> procedure) {
        procedure.apply(CustomImmutableMapBuilder::new);
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, ImmutableTransformableBuilder<String>>> procedure) {
        procedure.apply(HashCodeKeyTraversableBuilder::new);
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

    static final class HashCodeKeyTraversableBuilder<E> implements ImmutableTransformableBuilder<E> {
        private final ImmutableHashMap.Builder<Integer, E> builder = new ImmutableHashMap.Builder<>();

        @Override
        public HashCodeKeyTraversableBuilder<E> add(E element) {
            builder.put(SortUtils.hashCode(element), element);
            return this;
        }

        @Override
        public ImmutableTransformable<E> build() {
            return new CustomImmutableMap<>(builder.build());
        }
    }

    private static final class CustomImmutableMapBuilder<K, V> implements ImmutableMap.Builder<K, V> {

        private final ImmutableHashMap.Builder<K, V> mBuilder = new ImmutableHashMap.Builder<>();

        @Override
        public CustomImmutableMapBuilder<K, V> put(K key, V value) {
            mBuilder.put(key, value);
            return this;
        }

        @Override
        public ImmutableMap<K, V> build() {
            return new CustomImmutableMap<>(mBuilder.build());
        }
    }

    private static final class CustomImmutableMap<K, V> implements ImmutableMap<K, V> {

        private final ImmutableHashMap<K, V> mMap;

        CustomImmutableMap(ImmutableHashMap<K, V> map) {
            mMap = map;
        }

        @Override
        public TransformerWithKey<K, V> iterator() {
            return mMap.iterator();
        }

        @Override
        public K keyAt(int index) {
            return mMap.keyAt(index);
        }

        @Override
        public ImmutableSet<K> keySet() {
            return mMap.keySet();
        }

        @Override
        public ImmutableSet<Entry<K, V>> entries() {
            return mMap.entries();
        }

        @Override
        public ImmutableMap<K, V> put(K key, V value) {
            final ImmutableHashMap<K, V> newMap = mMap.put(key, value);
            return (newMap == mMap)? this : new CustomImmutableMap<>(newMap);
        }

        @Override
        public ImmutableList<V> toList() {
            return mMap.toList();
        }

        @Override
        public ImmutableSet<V> toSet() {
            return mMap.toSet();
        }

        @Override
        public ImmutableIntSet indexes() {
            return mMap.indexes();
        }

        @Override
        public ImmutableIntValueMap<V> count() {
            return mMap.count();
        }

        @Override
        public ImmutableMap<K, V> filter(Predicate<? super V> predicate) {
            final ImmutableHashMap<K, V> newMap = mMap.filter(predicate);
            return (newMap == mMap)? this : new CustomImmutableMap<>(newMap);
        }

        @Override
        public ImmutableIntValueMap<K> mapToInt(IntResultFunction<? super V> mapFunc) {
            return mMap.mapToInt(mapFunc);
        }

        @Override
        public ImmutableMap<K, V> toImmutable() {
            return this;
        }

        @Override
        public MutableMap<K, V> mutate() {
            return mMap.mutate();
        }

        @Override
        public MutableMap<K, V> mutate(ArrayLengthFunction arrayLengthFunction) {
            return mMap.mutate(arrayLengthFunction);
        }

        @Override
        public <U> ImmutableMap<K, U> map(Function<? super V, ? extends U> mapFunc) {
            return mMap.map(mapFunc);
        }

        @Override
        public ImmutableMap<K, V> sort(SortFunction<? super K> function) {
            return mMap.sort(function);
        }

        @Override
        public ImmutableMap<K, V> removeAt(int index) {
            return mMap.removeAt(index);
        }

        @Override
        public int hashCode() {
            return mMap.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof CustomImmutableMap)) {
                return false;
            }

            return mMap.equals(((CustomImmutableMap) other).mMap);
        }
    }
}

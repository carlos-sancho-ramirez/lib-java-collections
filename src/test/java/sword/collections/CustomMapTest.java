package sword.collections;

import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class CustomMapTest implements MapTest<Integer, String, TransformableBuilder<String>, MapBuilder<Integer, String>> {
    @Override
    public MapBuilder<Integer, String> newBuilder() {
        return new CustomMapBuilder<>();
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
    public void withMapBuilderSupplier(Procedure<MapBuilderSupplier<Integer, String, MapBuilder<Integer, String>>> procedure) {
        procedure.apply(CustomMapBuilder::new);
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, TransformableBuilder<String>>> procedure) {
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

    static final class HashCodeKeyTraversableBuilder<E> implements TransformableBuilder<E> {
        private final ImmutableHashMap.Builder<Integer, E> builder = new ImmutableHashMap.Builder<>();

        @Override
        public HashCodeKeyTraversableBuilder<E> add(E element) {
            builder.put(SortUtils.hashCode(element), element);
            return this;
        }

        @Override
        public Transformable<E> build() {
            return new CustomMap<>(builder.build());
        }
    }

    private static final class CustomMapBuilder<K, V> implements MapBuilder<K, V> {

        private final ImmutableHashMap.Builder<K, V> mBuilder = new ImmutableHashMap.Builder<>();

        @Override
        public CustomMapBuilder<K, V> put(K key, V value) {
            mBuilder.put(key, value);
            return this;
        }

        @Override
        public Map<K, V> build() {
            return new CustomMap<>(mBuilder.build());
        }
    }

    private static final class CustomMap<K, V> implements Map<K, V> {

        private final Map<K, V> mMap;

        CustomMap(Map<K, V> map) {
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
        public Set<K> keySet() {
            return mMap.keySet();
        }

        @Override
        public Set<Entry<K, V>> entries() {
            return mMap.entries();
        }

        @Override
        public List<V> toList() {
            return mMap.toList();
        }

        @Override
        public Set<V> toSet() {
            return mMap.toSet();
        }

        @Override
        public IntSet indexes() {
            return mMap.indexes();
        }

        @Override
        public IntValueMap<V> count() {
            return mMap.count();
        }

        @Override
        public Map<K, V> filter(Predicate<? super V> predicate) {
            final Map<K, V> newMap = mMap.filter(predicate);
            return (newMap == mMap)? this : new CustomMap<>(newMap);
        }

        @Override
        public IntValueMap<K> mapToInt(IntResultFunction<? super V> mapFunc) {
            return mMap.mapToInt(mapFunc);
        }

        @Override
        public ImmutableMap<K, V> toImmutable() {
            return mMap.toImmutable();
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
        public <U> Map<K, U> map(Function<? super V, ? extends U> mapFunc) {
            return mMap.map(mapFunc);
        }

        @Override
        public Map<K, V> sort(SortFunction<? super K> function) {
            return mMap.sort(function);
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

            if (!(other instanceof CustomMap)) {
                return false;
            }

            return mMap.equals(((CustomMap) other).mMap);
        }
    }
}

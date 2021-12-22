package sword.collections;

import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public final class CustomMutableMapTest implements MutableMapTest<Integer, String, MutableTransformableBuilder<String>, MutableMap.Builder<Integer, String>> {
    @Override
    public MutableMap.Builder<Integer, String> newMapBuilder() {
        return new CustomMutableMapBuilder<>();
    }

    @Override
    public MutableMap.Builder<Integer, String> newBuilder() {
        return new CustomMutableMapBuilder<>();
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
    public void withMapBuilderSupplier(Procedure<MapBuilderSupplier<Integer, String, MutableMap.Builder<Integer, String>>> procedure) {
        procedure.apply(MutableHashMap.Builder::new);
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, MutableTransformableBuilder<String>>> procedure) {
        procedure.apply(MutableHashMapTest.HashCodeKeyTraversableBuilder::new);
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

    private static final class CustomMutableMapBuilder<K, V> implements MutableMap.Builder<K, V> {

        private final MutableMap<K, V> mMap = MutableHashMap.empty();

        @Override
        public CustomMutableMapBuilder<K, V> put(K key, V value) {
            mMap.put(key, value);
            return this;
        }

        @Override
        public CustomMutableMap<K, V> build() {
            return new CustomMutableMap<>(mMap);
        }
    }

    private static final class CustomMutableMap<K, V> implements MutableMap<K, V> {
        private final MutableMap<K, V> mMap;

        private CustomMutableMap(MutableMap<K, V> map) {
            mMap = map;
        }

        @Override
        public boolean put(K key, V value) {
            return mMap.put(key, value);
        }

        @Override
        public boolean remove(K key) {
            return mMap.remove(key);
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
        public Map<K, V> filter(Predicate<? super V> predicate) {
            final Map<K, V> result = mMap.filter(predicate);
            return (result == mMap)? this : new CustomMutableMap<>(result.mutate());
        }

        @Override
        public <E> Map<K, E> map(Function<? super V, ? extends E> func) {
            final Map<K, E> result = mMap.map(func);
            return new CustomMutableMap<>(result.mutate());
        }

        @Override
        public IntValueMap<K> mapToInt(IntResultFunction<? super V> func) {
            return mMap.mapToInt(func);
        }

        @Override
        public ImmutableMap<K, V> toImmutable() {
            return mMap.toImmutable();
        }

        @Override
        public MutableMap<K, V> mutate() {
            return new CustomMutableMap<>(mMap.mutate());
        }

        @Override
        public MutableMap<K, V> mutate(ArrayLengthFunction arrayLengthFunction) {
            return new CustomMutableMap<>(mMap.mutate(arrayLengthFunction));
        }

        @Override
        public Map<K, V> sort(SortFunction<? super K> function) {
            return mMap.sort(function);
        }

        @Override
        public void removeAt(int index) throws IndexOutOfBoundsException {
            mMap.removeAt(index);
        }

        @Override
        public boolean clear() {
            return mMap.clear();
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

            if (!(other instanceof CustomMutableMap)) {
                return false;
            }

            return mMap.equals(((CustomMutableMap) other).mMap);
        }
    }
}

package sword.collections;

import static sword.collections.TestUtils.withString;

public final class CustomIntKeyMapTest implements IntKeyMapTest<String, TransformableBuilder<String>, IntKeyMapBuilder<String>> {

    @Override
    public IntKeyMapBuilder<String> newMapBuilder() {
        return new CustomIntKeyMapBuilder<>();
    }

    @Override
    public String getTestValue() {
        return "value";
    }

    @Override
    public String getTestValue2() {
        return "value2";
    }

    @Override
    public String valueFromKey(int key) {
        return "_" + key;
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntKeyMapBuilderSupplier<String, IntKeyMapBuilder<String>>> procedure) {
        procedure.apply(CustomIntKeyMapBuilder::new);
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, TransformableBuilder<String>>> procedure) {
        procedure.apply(HashCodeKeyTraversableBuilder::new);
    }

    @Override
    public void withValue(Procedure<String> procedure) {
        withString(procedure);
    }

    private boolean hashCodeIsEven(Object value) {
        return value == null || (value.hashCode() & 1) == 0;
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
        private final ImmutableIntKeyMap.Builder<E> builder = new ImmutableIntKeyMap.Builder<>();

        @Override
        public HashCodeKeyTraversableBuilder<E> add(E element) {
            builder.put(SortUtils.hashCode(element), element);
            return this;
        }

        @Override
        public CustomIntKeyMap<E> build() {
            return new CustomIntKeyMap<>(builder.build());
        }
    }

    private static final class CustomIntKeyMapBuilder<T> implements IntKeyMapBuilder<T> {

        private final MutableIntKeyMap<T> mMap = MutableIntKeyMap.empty();

        @Override
        public CustomIntKeyMapBuilder<T> put(int key, T value) {
            mMap.put(key, value);
            return this;
        }

        @Override
        public CustomIntKeyMap<T> build() {
            return new CustomIntKeyMap<>(mMap);
        }
    }

    private static final class CustomIntKeyMap<T> implements IntKeyMap<T> {
        private final IntKeyMap<T> mMap;

        CustomIntKeyMap(IntKeyMap<T> map) {
            mMap = map;
        }

        @Override
        public Transformer<T> iterator() {
            return mMap.iterator();
        }

        @Override
        public IntKeyMap<T> filter(Predicate<? super T> predicate) {
            final IntKeyMap<T> newMap = mMap.filter(predicate);
            return (newMap == mMap)? this : new CustomIntKeyMap<>(newMap);
        }

        @Override
        public <E> IntKeyMap<E> map(Function<? super T, ? extends E> func) {
            return new CustomIntKeyMap<>(mMap.map(func));
        }

        @Override
        public IntPairMap mapToInt(IntResultFunction<? super T> func) {
            return mMap.mapToInt(func);
        }

        @Override
        public T get(int key) {
            return mMap.get(key);
        }

        @Override
        public T get(int key, T defaultValue) {
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
        public Set<Entry<T>> entries() {
            return mMap.entries();
        }

        @Override
        public ImmutableIntKeyMap<T> toImmutable() {
            return mMap.toImmutable();
        }

        @Override
        public MutableIntKeyMap<T> mutate() {
            return mMap.mutate();
        }

        @Override
        public MutableIntKeyMap<T> mutate(ArrayLengthFunction arrayLengthFunction) {
            return mMap.mutate(arrayLengthFunction);
        }
    }
}

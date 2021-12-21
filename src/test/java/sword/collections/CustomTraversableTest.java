package sword.collections;

import static sword.collections.TestUtils.withString;

public final class CustomTraversableTest implements TraversableTest<String, TraversableBuilder<String>> {

    private boolean hashCodeIsEven(Object value) {
        return value == null || (value.hashCode() & 1) == 0;
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, TraversableBuilder<String>>> procedure) {
        procedure.apply(CustomTraversableBuilder::new);
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

    private static final class CustomTraversableBuilder<E> implements TraversableBuilder<E> {

        private final MutableList<E> mList = MutableList.empty();

        @Override
        public CustomTraversableBuilder<E> add(E element) {
            mList.append(element);
            return this;
        }

        @Override
        public CustomTraversable<E> build() {
            return new CustomTraversable<>(mList);
        }
    }

    private static final class CustomTraversable<V> implements Traversable<V> {

        private final Traversable<V> mTraversable;

        CustomTraversable(Traversable<V> traversable) {
            mTraversable = traversable;
        }

        @Override
        public Traverser<V> iterator() {
            return mTraversable.iterator();
        }

        @Override
        public int hashCode() {
            return mTraversable.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof CustomTraversable)) {
                return false;
            }

            return mTraversable.equals(((CustomTraversable) other).mTraversable);
        }
    }
}

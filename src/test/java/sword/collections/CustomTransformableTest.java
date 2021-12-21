package sword.collections;

import static sword.collections.TestUtils.withString;

public final class CustomTransformableTest implements TransformableTest<String, TransformableBuilder<String>> {

    private boolean hashCodeIsEven(Object value) {
        return value == null || (value.hashCode() & 1) == 0;
    }

    @Override
    public void withBuilderSupplier(Procedure<BuilderSupplier<String, TransformableBuilder<String>>> procedure) {
        procedure.apply(CustomTransformableBuilder::new);
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

    private static final class CustomTransformableBuilder<E> implements TransformableBuilder<E> {

        private final MutableList<E> mList = MutableList.empty();

        @Override
        public CustomTransformableBuilder<E> add(E element) {
            mList.append(element);
            return this;
        }

        @Override
        public CustomTransformable<E> build() {
            return new CustomTransformable<>(mList);
        }
    }

    private static final class CustomTransformable<V> implements Transformable<V> {

        private final Transformable<V> mTransformable;

        CustomTransformable(Transformable<V> transformable) {
            mTransformable = transformable;
        }

        @Override
        public Transformer<V> iterator() {
            return mTransformable.iterator();
        }

        @Override
        public Transformable<V> filter(Predicate<? super V> predicate) {
            final Transformable<V> newMap = mTransformable.filter(predicate);
            return (newMap == mTransformable)? this : new CustomTransformable<>(newMap);
        }

        @Override
        public int hashCode() {
            return mTransformable.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof CustomTransformable)) {
                return false;
            }

            return mTransformable.equals(((CustomTransformable) other).mTransformable);
        }
    }
}

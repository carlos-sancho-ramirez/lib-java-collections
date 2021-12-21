package sword.collections;

public final class CustomIntTransformableTest implements IntTransformableTest<IntTransformableBuilder> {

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<IntTransformableBuilder>> procedure) {
        procedure.apply(CustomIntTransformableBuilder::new);
    }

    @Override
    public IntTransformableBuilder newIntBuilder() {
        return new CustomIntTransformableBuilder();
    }

    private static final class CustomIntTransformableBuilder implements IntTransformableBuilder {

        private final MutableIntList mList = MutableIntList.empty();

        @Override
        public CustomIntTransformableBuilder add(int element) {
            mList.append(element);
            return this;
        }

        @Override
        public CustomIntTransformable build() {
            return new CustomIntTransformable(mList);
        }
    }

    private static final class CustomIntTransformable implements IntTransformable {
        private final IntTransformable mList;

        CustomIntTransformable(IntTransformable list) {
            mList = list;
        }

        @Override
        public IntTransformer iterator() {
            return mList.iterator();
        }

        @Override
        public IntTransformable filter(IntPredicate predicate) {
            final IntTransformable newList = mList.filter(predicate);
            return (newList == mList)? this : new CustomIntTransformable(newList);
        }
    }
}

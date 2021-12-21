package sword.collections;

public final class CustomIntTraversableTest implements IntTraversableTest<IntTraversableBuilder> {

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<IntTraversableBuilder>> procedure) {
        procedure.apply(CustomIntTraversableBuilder::new);
    }

    @Override
    public IntTraversableBuilder newIntBuilder() {
        return new CustomIntTraversableBuilder();
    }

    private static final class CustomIntTraversableBuilder implements IntTraversableBuilder {

        private final MutableIntList mList = MutableIntList.empty();

        @Override
        public CustomIntTraversableBuilder add(int element) {
            mList.append(element);
            return this;
        }

        @Override
        public CustomIntTraversable build() {
            return new CustomIntTraversable(mList);
        }
    }

    private static final class CustomIntTraversable implements IntTraversable {
        private final IntTraversable mList;

        CustomIntTraversable(IntTraversable list) {
            mList = list;
        }

        @Override
        public IntTraverser iterator() {
            return mList.iterator();
        }
    }
}

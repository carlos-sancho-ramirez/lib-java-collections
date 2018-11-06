package sword.collections;

abstract class AbstractIntTransformer implements IntTransformer {

    @Override
    public IntList toList() {
        final ImmutableIntList.Builder builder = new ImmutableIntList.Builder();
        while (hasNext()) {
            builder.add(next());
        }

        return builder.build();
    }

    @Override
    public IntSet toSet() {
        final ImmutableIntSetBuilder builder = new ImmutableIntSetBuilder();
        while (hasNext()) {
            builder.add(next());
        }

        return builder.build();
    }

    @Override
    public IntTransformer indexes() {
        return new IndexesTransformer<>(this);
    }
}

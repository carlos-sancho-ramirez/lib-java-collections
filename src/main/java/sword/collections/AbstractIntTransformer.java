package sword.collections;

abstract class AbstractIntTransformer implements IntTransformer {

    @Override
    public IntSet toSet() {
        final ImmutableIntSetBuilder builder = new ImmutableIntSetBuilder();
        while (hasNext()) {
            builder.add(next());
        }

        return builder.build();
    }
}

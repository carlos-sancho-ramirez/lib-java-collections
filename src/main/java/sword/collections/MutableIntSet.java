package sword.collections;

public interface MutableIntSet extends IntSet, MutableIntTransformable {

    interface Builder extends IntSet.Builder, MutableIntTransformableBuilder {
        @Override
        Builder add(int value);

        @Override
        MutableIntSet build();
    }
}

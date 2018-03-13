package sword.collections;

public interface ImmutableIntCollectionBuilder<T extends IterableImmutableIntCollection> extends IntCollectionBuilder<T> {
    ImmutableIntCollectionBuilder<T> add(int value);
    T build();
}

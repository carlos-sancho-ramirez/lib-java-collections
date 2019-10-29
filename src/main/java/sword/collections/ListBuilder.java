package sword.collections;

public interface ListBuilder<T> extends TransformableBuilder<T> {
    @Override
    ListBuilder<T> add(T element);

    @Override
    List<T> build();
}

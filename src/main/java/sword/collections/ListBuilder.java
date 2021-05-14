package sword.collections;

public interface ListBuilder<T> extends TransformableBuilder<T> {
    @Override
    ListBuilder<T> add(T element);

    default ListBuilder<T> append(T element) {
        return add(element);
    }

    @Override
    List<T> build();
}

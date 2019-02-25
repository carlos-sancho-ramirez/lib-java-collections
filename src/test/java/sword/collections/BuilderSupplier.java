package sword.collections;

public interface BuilderSupplier<T, B extends TraversableBuilder<T>> {
    B newBuilder();
}

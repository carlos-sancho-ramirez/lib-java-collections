package sword.collections;

public interface IntBuilderSupplier<C extends IntTraversable, B extends IntTraversableBuilder<C>> {
    B newBuilder();
}

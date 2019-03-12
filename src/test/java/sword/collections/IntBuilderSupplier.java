package sword.collections;

public interface IntBuilderSupplier<B extends IntTraversableBuilder> {
    B newBuilder();
}

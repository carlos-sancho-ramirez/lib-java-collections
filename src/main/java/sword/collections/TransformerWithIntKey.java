package sword.collections;

public interface TransformerWithIntKey<T> extends Transformer<T> {

    /**
     * Key attached to the last value returned in {@link #next()}
     */
    int key();
}

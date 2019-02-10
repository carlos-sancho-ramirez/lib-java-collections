package sword.collections;

public interface IntTransformerWithKey<K> extends IntTransformer {

    @Override
    IntTransformerWithKey<K> filter(IntPredicate predicate);

    @Override
    IntTransformerWithKey<K> filterNot(IntPredicate predicate);

    @Override
    IntTransformerWithKey<K> mapToInt(IntToIntFunction mapFunc);

    @Override
    <U> TransformerWithKey<K, U> map(IntFunction<U> mapFunc);

    /**
     * Build a new map containing all elements given on traversing this collection
     */
    IntValueMap<K> toMap();
}

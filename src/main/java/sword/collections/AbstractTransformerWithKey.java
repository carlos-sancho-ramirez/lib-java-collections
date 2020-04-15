package sword.collections;

public abstract class AbstractTransformerWithKey<K, V> extends AbstractTransformer<V> implements TransformerWithKey<K, V> {

    @Override
    public <U> TransformerWithKey<K, U> map(Function<? super V, ? extends U> mapFunc) {
        return new MapTransformerWithKey<>(this, mapFunc);
    }

    @Override
    public Map<K, V> toMap() {
        final ImmutableMap.Builder<K, V> builder = new ImmutableHashMap.Builder<>();
        while (hasNext()) {
            final V value = next();
            final K key = key();
            builder.put(key, value);
        }

        return builder.build();
    }
}

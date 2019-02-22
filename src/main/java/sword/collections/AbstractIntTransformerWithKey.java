package sword.collections;

public abstract class AbstractIntTransformerWithKey<K> extends AbstractIntTransformer implements IntTransformerWithKey<K> {

    @Override
    public IntValueMap<K> toMap() {
        final ImmutableIntValueMap.Builder<K> builder = new ImmutableIntValueHashMap.Builder<>();
        while (hasNext()) {
            final int value = next();
            final K key = key();
            builder.put(key, value);
        }

        return builder.build();
    }
}

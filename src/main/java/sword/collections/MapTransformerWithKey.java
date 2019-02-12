package sword.collections;

final class MapTransformerWithKey<K, T, R> extends AbstractTransformerWithKey<K, R> {

    private final TransformerWithKey<K, T> _source;
    private final Function<T, R> _func;

    MapTransformerWithKey(TransformerWithKey<K, T> source, Function<T, R> func) {
        if (source == null || func == null) {
            throw new IllegalArgumentException();
        }

        _source = source;
        _func = func;
    }

    @Override
    public boolean hasNext() {
        return _source.hasNext();
    }

    @Override
    public R next() {
        return _func.apply(_source.next());
    }

    @Override
    public K key() {
        return _source.key();
    }
}

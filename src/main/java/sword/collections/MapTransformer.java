package sword.collections;

final class MapTransformer<T, R> extends AbstractTransformer<R> {

    private final Transformer<T> _source;
    private final Function<T, R> _func;

    MapTransformer(Transformer<T> source, Function<T, R> func) {
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
}

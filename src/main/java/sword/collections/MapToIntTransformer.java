package sword.collections;

final class MapToIntTransformer<T> extends AbstractIntTransformer {

    private final Transformer<T> _source;
    private final IntResultFunction<? super T> _func;

    MapToIntTransformer(Transformer<T> source, IntResultFunction<? super T> func) {
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
    public Integer next() {
        return _func.apply(_source.next());
    }
}

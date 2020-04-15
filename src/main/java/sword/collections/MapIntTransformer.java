package sword.collections;

final class MapIntTransformer<T> extends AbstractTransformer {

    private final IntTransformer _source;
    private final IntFunction<? extends T> _func;

    MapIntTransformer(IntTransformer source, IntFunction<? extends T> func) {
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
    public T next() {
        return _func.apply(_source.next());
    }
}

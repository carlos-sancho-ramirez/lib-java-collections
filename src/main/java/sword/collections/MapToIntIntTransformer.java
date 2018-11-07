package sword.collections;

final class MapToIntIntTransformer<T> extends AbstractIntTransformer {

    private final IntTransformer _source;
    private final IntToIntFunction _func;

    MapToIntIntTransformer(IntTransformer source, IntToIntFunction func) {
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

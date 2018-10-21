package sword.collections;

final class IndexesTransformer<T> extends AbstractIntTransformer {

    private final Transformer<T> _source;
    private int _index;

    IndexesTransformer(Transformer<T> source) {
        if (source == null) {
            throw new IllegalArgumentException();
        }

        _source = source;
    }

    @Override
    public boolean hasNext() {
        return _source.hasNext();
    }

    @Override
    public Integer next() {
        _source.next();
        return _index++;
    }
}

package sword.collections;

final class FilterTransformer<T> extends AbstractTransformer<T> {

    private final Transformer<T> _source;
    private final Predicate<? super T> _func;

    private boolean _hasNext;
    private T _next;

    FilterTransformer(Transformer<T> source, Predicate<? super T> func) {
        if (source == null || func == null) {
            throw new IllegalArgumentException();
        }

        _source = source;
        _func = func;
        findNext();
    }

    private void findNext() {
        while (_source.hasNext()) {
            final T next = _source.next();
            if (_func.apply(next)) {
                _hasNext = true;
                _next = next;
                break;
            }
        }
    }

    @Override
    public boolean hasNext() {
        return _hasNext;
    }

    @Override
    public T next() {
        final T result = _next;
        _hasNext = false;
        findNext();
        return result;
    }
}

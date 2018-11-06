package sword.collections;

final class FilterIntTransformer extends AbstractIntTransformer {

    private final IntTransformer _source;
    private final IntPredicate _func;

    private boolean _hasNext;
    private int _next;

    FilterIntTransformer(IntTransformer source, IntPredicate func) {
        if (source == null || func == null) {
            throw new IllegalArgumentException();
        }

        _source = source;
        _func = func;
        findNext();
    }

    private void findNext() {
        while (_source.hasNext()) {
            final int next = _source.next();
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
    public Integer next() {
        final int result = _next;
        _hasNext = false;
        findNext();
        return result;
    }
}

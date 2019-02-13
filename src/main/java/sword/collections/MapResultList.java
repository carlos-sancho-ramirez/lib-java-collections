package sword.collections;

final class MapResultList<S, T> extends AbstractTraversable<T> implements List<T> {

    private final Transformable<S> _transformable;
    private final Function<S, T> _function;

    MapResultList(Transformable<S> transformable, Function<S, T> function) {
        if (transformable == null || function == null) {
            throw new IllegalArgumentException();
        }

        _transformable = transformable;
        _function = function;
    }

    @Override
    public ImmutableList<T> toImmutable() {
        return iterator().toList().toImmutable();
    }

    @Override
    public MutableList<T> mutate() {
        return iterator().toList().mutate();
    }

    @Override
    public Transformer<T> iterator() {
        return new MapTransformer<>(_transformable.iterator(), _function);
    }
}

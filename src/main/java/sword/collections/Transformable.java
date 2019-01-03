package sword.collections;

public interface Transformable<T> extends IterableCollection<T> {

    Transformer<T> iterator();

    /**
     * Return all indexes within this collection in ascending order.
     */
    default IntSet indexes() {
        return iterator().indexes().toSet();
    }

    default IterableIntCollection mapToInt(IntResultFunction<T> func) {
        return iterator().mapToInt(func).toList();
    }

    default <E> IterableCollection<E> map(Function<T, E> func) {
        return iterator().map(func).toList();
    }
}

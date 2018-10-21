package sword.collections;

public interface Transformable<T> extends IterableCollection<T> {

    Transformer<T> iterator();

    /**
     * Return all indexes within this collection in ascending order.
     */
    default IntSet indexes() {
        return iterator().indexes().toSet();
    }
}

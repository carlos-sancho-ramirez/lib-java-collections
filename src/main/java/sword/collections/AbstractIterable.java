package sword.collections;

abstract class AbstractIterable<T> extends AbstractSizable implements IterableCollection<T> {

    @Override
    public boolean contains(T value) {
        for (T item : this) {
            if (value == null && item == null || value != null && value.equals(item)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean anyMatch(Predicate<T> predicate) {
        for (T item : this) {
            if (predicate.apply(item)) {
                return true;
            }
        }

        return false;
    }
}

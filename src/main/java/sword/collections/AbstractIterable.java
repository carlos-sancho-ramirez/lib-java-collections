package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class AbstractIterable<T> extends AbstractSizable implements IterableCollection<T> {

    @Override
    public int indexOf(T value) {
        int index = 0;
        for (T item : this) {
            if (value == null && item == null || value != null && value.equals(item)) {
                return index;
            }

            index++;
        }

        return -1;
    }

    @Override
    public boolean contains(T value) {
        return indexOf(value) >= 0;
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

    @Override
    public T findFirst(Predicate<T> predicate, T defaultValue) {
        for (T item : this) {
            if (predicate.apply(item)) {
                return item;
            }
        }

        return defaultValue;
    }

    @Override
    public int hashCode() {
        return size();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('(');
        boolean itemAdded = false;

        for (T value : this) {
            if (itemAdded) {
                sb.append(',');
            }

            sb.append(String.valueOf(value));
            itemAdded = true;
        }

        return sb.append(')').toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof AbstractIterable)) {
            return false;
        }

        final AbstractIterable that = (AbstractIterable) other;
        final Iterator thisIt = iterator();
        final Iterator thatIt = that.iterator();

        while (thisIt.hasNext()) {
            if (!thatIt.hasNext() || !equal(thisIt.next(), thatIt.next())) {
                return false;
            }
        }

        return !thatIt.hasNext();
    }
}

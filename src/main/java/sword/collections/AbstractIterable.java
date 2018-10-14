package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class AbstractIterable<T> implements IterableCollection<T>, Sizable {

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

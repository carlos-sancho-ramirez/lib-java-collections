package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class AbstractIntIterable implements IterableIntCollection, Sizable {

    @Override
    public int size() {
        final Iterator<Integer> it = iterator();
        int size = 0;
        while (it.hasNext()) {
            it.next();
            ++size;
        }

        return size;
    }

    @Override
    public int hashCode() {
        return size();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('(');
        boolean itemAdded = false;

        for (int value : this) {
            if (itemAdded) {
                sb.append(',');
            }

            sb.append(value);
            itemAdded = true;
        }

        return sb.append(')').toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof AbstractIntIterable)) {
            return false;
        }

        final AbstractIntIterable that = (AbstractIntIterable) other;
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

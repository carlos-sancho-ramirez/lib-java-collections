package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

abstract class AbstractIntIterable extends AbstractSizable implements IterableIntCollection {

    @Override
    public int valueAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }

        final Iterator<Integer> it = iterator();
        int value = 0;
        while (index-- >= 0) {
            if (!it.hasNext()) {
                throw new IndexOutOfBoundsException();
            }
            value = it.next();
        }

        return value;
    }

    @Override
    public boolean contains(int value) {
        for (int item : this) {
            if (value == item) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean anyMatch(IntPredicate predicate) {
        for (int item : this) {
            if (predicate.apply(item)) {
                return true;
            }
        }

        return false;
    }

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

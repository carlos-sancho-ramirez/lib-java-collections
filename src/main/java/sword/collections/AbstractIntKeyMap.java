package sword.collections;

import static sword.collections.SortUtils.equal;

abstract class AbstractIntKeyMap<T> extends AbstractTraversable<T> implements IntKeyMap<T> {

    @Override
    public int hashCode() {
        return entries().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof IntKeyMap)) {
            return false;
        }

        final IntKeyMap that = (IntKeyMap) other;
        final int size = size();
        if (that.size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (keyAt(i) != that.keyAt(i) || !equal(valueAt(i), that.valueAt(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('(');
        boolean itemAdded = false;

        for (Entry value : entries()) {
            if (itemAdded) {
                sb.append(',');
            }

            sb.append(value.toString());
            itemAdded = true;
        }

        return sb.append(')').toString();
    }
}

package sword.collections;

import static sword.collections.SortUtils.equal;

abstract class AbstractIntValueMap<T> extends AbstractIntTraversable implements IntValueMap<T> {

    @Override
    public int hashCode() {
        return entries().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof IntValueMap)) {
            return false;
        }

        final IntValueMap that = (IntValueMap) other;
        final int size = size();
        if (that.size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (valueAt(i) != that.valueAt(i) || !equal(keyAt(i), that.keyAt(i))) {
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

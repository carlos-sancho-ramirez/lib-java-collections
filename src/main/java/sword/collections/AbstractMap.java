package sword.collections;

import static sword.collections.SortUtils.equal;

abstract class AbstractMap<K, V> extends AbstractTraversable<V> implements Map<K, V> {

    @Override
    public int hashCode() {
        return entries().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Map)) {
            return false;
        }

        final Map that = (Map) other;
        final int size = size();
        if (that.size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (!equal(keyAt(i), that.keyAt(i)) || !equal(valueAt(i), that.valueAt(i))) {
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

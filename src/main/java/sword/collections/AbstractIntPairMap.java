package sword.collections;

abstract class AbstractIntPairMap extends AbstractIntTraversable implements IntPairMap {

    @Override
    public int hashCode() {
        return entries().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof IntPairMap)) {
            return false;
        }

        final IntPairMap that = (IntPairMap) other;
        final int size = size();
        if (that.size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (keyAt(i) != that.keyAt(i) || valueAt(i) != that.valueAt(i)) {
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

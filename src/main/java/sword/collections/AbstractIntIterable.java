package sword.collections;

abstract class AbstractIntIterable extends AbstractSizable implements IterableIntCollection {

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
}

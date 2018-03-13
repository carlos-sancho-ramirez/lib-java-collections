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
}

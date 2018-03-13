package sword.collections;

abstract class AbstractSizable implements Sizable {

    @Override
    public final boolean isEmpty() {
        return size() == 0;
    }
}

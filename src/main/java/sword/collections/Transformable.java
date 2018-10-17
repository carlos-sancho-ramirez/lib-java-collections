package sword.collections;

public interface Transformable<T> extends IterableCollection<T> {

    Transformer<T> iterator();
}

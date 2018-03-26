package sword.collections;

public interface ReduceFunction<T> {
    T apply(T left, T right);
}

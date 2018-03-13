package sword.collections;

/**
 * A structure of data holding 2 instances of arbitrary types.
 *
 * This class is immutable, what means that its references cannot be updated once the instance is
 * created.
 *
 * @param <A> Type for left value
 * @param <B> Type for right value
 */
public class ImmutablePair<A, B> {

    public final A left;
    public final B right;

    public ImmutablePair(A left, B right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int hashCode() {
        final int leftHashCode = (left != null)? left.hashCode() : 0;
        final int rightHashCode = (right != null)? right.hashCode() : 0;
        return leftHashCode ^ rightHashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ImmutablePair)) {
            return false;
        }

        ImmutablePair that = (ImmutablePair) other;
        return (left == null && that.left == null || left != null && left.equals(that.left)) &&
                (right == null && that.right == null || right != null && right.equals(that.right));
    }
}

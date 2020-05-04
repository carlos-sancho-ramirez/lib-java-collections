package sword.collections;

public final class GranularityBasedArrayLengthFunction implements ArrayLengthFunction {

    private static final int GRANULARITY = 4;
    private static final GranularityBasedArrayLengthFunction mInstance = new GranularityBasedArrayLengthFunction();

    static GranularityBasedArrayLengthFunction getInstance() {
        return mInstance;
    }

    @Override
    public int suitableArrayLength(int currentSize, int newSize) {
        int s = ((newSize + GRANULARITY - 1) / GRANULARITY) * GRANULARITY;
        return (s > 0)? s : GRANULARITY;
    }

    private GranularityBasedArrayLengthFunction() {
    }
}

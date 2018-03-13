package sword.collections;

final class TestUtils {
    private static final int[] INT_VALUES = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 13, 17, 22, 46, 124, 1023, 16678, 65535,
            -1, -2, -7, -24, -65535, Integer.MAX_VALUE, Integer.MIN_VALUE
    };

    private static final String[] STRING_VALUES = {
            null, "", "a", "x", "!$%&/`", "\\", "23", " ", "Hello", "hEll0"
    };

    public static void withInt(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    public static void withString(Procedure<String> procedure) {
        for (String value : STRING_VALUES) {
            procedure.apply(value);
        }
    }
}

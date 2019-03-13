package sword.collections;

public final class SimpleTest {

    private static void testBuildIntSetAscending(int amount) {
        final MutableIntArraySet set = MutableIntArraySet.empty();
        for (int i = 0; i < amount; i++) {
            set.add(i);
        }
    }

    private static void testBuildIntSetDescending(int amount) {
        final MutableIntArraySet set = MutableIntArraySet.empty();
        for (int i = amount - 1; i >= 0; i--) {
            set.add(i);
        }
    }

    private static void testBuildIntSetSeeded(int amount) {
        final MutableIntArraySet set = MutableIntArraySet.empty();
        int seed = 567;
        for (int i = 0; i < amount; i++) {
            set.add(seed);
            seed *= 31;
        }
    }

    private static void testBuildIntTreeSetAscending(int amount) {
        final MutableIntTreeSet set = new MutableIntTreeSet();
        for (int i = 0; i < amount; i++) {
            set.add(i);
        }
    }

    private static void testBuildIntTreeSetDescending(int amount) {
        final MutableIntTreeSet set = new MutableIntTreeSet();
        for (int i = amount - 1; i >= 0; i--) {
            set.add(i);
        }
    }

    private static void testBuildIntTreeSetSeeded(int amount) {
        final MutableIntTreeSet set = new MutableIntTreeSet();
        int seed = 567;
        for (int i = 0; i < amount; i++) {
            set.add(seed);
            seed *= 31;
        }
    }

    private static int performSingleTest(IntProcedure procedure, int amount) {
        final long startTime = System.currentTimeMillis();
        try {
            procedure.apply(amount);
        }
        catch (Throwable t) {
            t.printStackTrace();
            return -1;
        }

        return (int) (System.currentTimeMillis() - startTime);
    }

    private static void performTest(String name, IntProcedure procedure, int amount) {
        final ImmutableIntList.Builder builder = new ImmutableIntList.Builder();
        boolean anyFailed = false;
        for (int i = 0; i < 5; i++) {
            final int result = performSingleTest(procedure, amount);
            if (result < 0) {
                anyFailed = true;
                break;
            }

            builder.add(result);
        }
        final ImmutableIntList times = builder.build();
        final IntFunction<String> func = Integer::toString;
        final String strTimes = "[" + times.map(func).reduce((a, b) -> a + ", " + b, "") + ']';

        final String status = anyFailed? "FAILED" : "OK";
        System.out.println(name + ": " + status + ' ' + strTimes + " millis");
    }

    public static void main(String[] args) {
        final int amount = 100000;
        System.out.println("Running test with amount " + amount);

        performTest("BuildIntSetAscending", SimpleTest::testBuildIntSetAscending, amount);
        performTest("BuildIntSetDescending", SimpleTest::testBuildIntSetDescending, amount);
        performTest("BuildIntSetSeeded", SimpleTest::testBuildIntSetSeeded, amount);

        performTest("BuildIntTreeSetAscending", SimpleTest::testBuildIntTreeSetAscending, amount);
        performTest("BuildIntTreeSetDescending", SimpleTest::testBuildIntTreeSetDescending, amount);
        performTest("BuildIntTreeSetSeeded", SimpleTest::testBuildIntTreeSetSeeded, amount);
    }
}

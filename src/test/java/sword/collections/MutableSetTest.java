package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

public class MutableSetTest extends AbstractIterableTest<String> {

    private static final String[] STRING_VALUES = {
            null, "", "_", "0", "abcd"
    };

    @Override
    void withValue(Procedure<String> procedure) {
        for (String str : STRING_VALUES) {
            procedure.apply(str);
        }
    }

    private String prefixUnderscore(String value) {
        return "_" + value;
    }

    private String charCounter(String value) {
        final int length = (value != null)? value.length() : 0;
        return Integer.toString(length);
    }

    @Override
    void withMapFunc(Procedure<Function<String, String>> procedure) {
        procedure.apply(this::prefixUnderscore);
        procedure.apply(this::charCounter);
    }

    private boolean filterFunc(String value) {
        return value != null && !value.isEmpty();
    }

    @Override
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::filterFunc);
    }

    MutableSet.Builder<String> newBuilder() {
        return new MutableSet.Builder<>();
    }

    @Override
    MutableSet.Builder<String> newIterableBuilder() {
        return newBuilder();
    }

    public void testSizeForTwoElements() {
        withValue(a -> withValue(b -> {
            final MutableSet<String> list = newBuilder().add(a).add(b).build();
            final int size = list.size();
            if (equal(a, b)) {
                assertEquals(1, size);
            }
            else {
                assertEquals(2, size);
            }
        }));
    }

    public void testIteratingForMultipleElements() {
        withValue(a -> withValue(b -> {
            final MutableSet<String> set = newBuilder().add(a).add(b).build();
            final Iterator<String> iterator = set.iterator();

            assertTrue(iterator.hasNext());
            final String first = iterator.next();
            final boolean sameValue = equal(a, b);

            if (sameValue) {
                assertEquals(a, first);
            }
            else if (equal(first, a)) {
                assertTrue(iterator.hasNext());
                assertEquals(b, iterator.next());
            }
            else if (equal(first, b)) {
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.next());
            }
            else {
                fail("Expected value " + a + " or " + b + " but iterator returned " + first);
            }

            assertFalse(iterator.hasNext());
        }));
    }

    public void testToImmutableForEmpty() {
        assertTrue(newBuilder().build().toImmutable().isEmpty());
    }

    public void testMutateForEmpty() {
        final MutableSet<String> set1 = newBuilder().build();
        final MutableSet<String> set2 = set1.mutate();

        assertEquals(set1, set2);
        assertNotSame(set1, set2);

        set1.add("");
        assertFalse(set2.contains(""));
    }

    public void testToImmutable() {
        withValue(a -> withValue(b -> {
            final MutableSet<String> set = newBuilder().add(a).add(b).build();
            final ImmutableSet<String> set2 = set.toImmutable();

            final Iterator<String> it1 = set.iterator();
            final Iterator<String> it2 = set2.iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                assertEquals(it1.next(), it2.next());
            }
            assertFalse(it2.hasNext());
        }));
    }

    public void testMutate() {
        withValue(a -> withValue(b -> {
            final MutableSet<String> set1 = newBuilder().add(a).add(b).build();
            final MutableSet<String> set2 = set1.mutate();

            final Iterator<String> it1 = set1.iterator();
            final Iterator<String> it2 = set2.iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                assertEquals(it1.next(), it2.next());
            }
            assertFalse(it2.hasNext());

            set2.remove(b);
            assertTrue(set1.contains(b));
            assertFalse(set2.contains(b));
        }));
    }
}

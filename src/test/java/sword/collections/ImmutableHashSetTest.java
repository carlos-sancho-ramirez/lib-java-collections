package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.HASH_FOR_NULL;
import static sword.collections.SortUtils.equal;

public class ImmutableHashSetTest extends AbstractIterableImmutableTest<String> {

    private static final String[] STRING_VALUES = {
            null, "", "_", "0", "abcd"
    };

    @Override
    void withValue(Procedure<String> procedure) {
        for (String str : STRING_VALUES) {
            procedure.apply(str);
        }
    }

    private String reduceFunc(String left, String right) {
        return String.valueOf(left) + '-' + String.valueOf(right);
    }

    @Override
    void withReduceFunction(Procedure<ReduceFunction<String>> procedure) {
        procedure.apply(this::reduceFunc);
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

    @Override
    void withMapToIntFunc(Procedure<IntResultFunction<String>> procedure) {
        procedure.apply(str -> (str == null)? 0 : str.hashCode());
    }

    private boolean filterFunc(String value) {
        return value != null && !value.isEmpty();
    }

    @Override
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::filterFunc);
    }

    ImmutableHashSet.Builder<String> newBuilder() {
        return new ImmutableHashSet.Builder<>();
    }

    @Override
    ImmutableHashSet.Builder<String> newIterableBuilder() {
        return newBuilder();
    }

    @Override
    ImmutableIntSetBuilder newIntIterableBuilder() {
        return new ImmutableIntSetBuilder();
    }

    @Override
    <E> ImmutableHashSet<E> emptyCollection() {
        return ImmutableHashSet.empty();
    }

    public void testSizeForTwoElements() {
        withValue(a -> withValue(b -> {
            final ImmutableHashSet<String> list = newBuilder().add(a).add(b).build();
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
            final ImmutableHashSet<String> set = newBuilder().add(a).add(b).build();
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

    @Override
    public void testMapForMultipleElements() {
        withMapFunc(f -> withValue(a -> withValue(b -> {
            final ImmutableHashSet<String> collection = newBuilder().add(a).add(b).build();
            final ImmutableHashSet<String> mapped = collection.map(f);
            final Iterator<String> iterator = mapped.iterator();

            final String mappedA = f.apply(a);
            final String mappedB = f.apply(b);

            assertTrue(iterator.hasNext());
            final boolean sameMappedValue = equal(mappedA, mappedB);
            final String first = iterator.next();

            if (sameMappedValue) {
                assertEquals(mappedA, first);
            }
            else if (equal(mappedA, first)) {
                assertTrue(iterator.hasNext());
                assertEquals(mappedB, iterator.next());
            }
            else if (equal(mappedB, first)) {
                assertTrue(iterator.hasNext());
                assertEquals(mappedA, iterator.next());
            }
            else {
                fail("Expected either " + mappedA + " or " + mappedB + " but found " + first);
            }

            assertFalse(iterator.hasNext());
        })));
    }

    @Override
    public void testMapToIntForMultipleElements() {
        withMapToIntFunc(f -> withValue(a -> withValue(b -> {
            final ImmutableHashSet<String> collection = newBuilder().add(a).add(b).build();
            final ImmutableIntSet mapped = collection.map(f);
            final Iterator<Integer> iterator = mapped.iterator();

            final int mappedA = f.apply(a);
            final int mappedB = f.apply(b);

            assertTrue(iterator.hasNext());
            final boolean sameMappedValue = equal(mappedA, mappedB);
            final int first = iterator.next();

            if (sameMappedValue) {
                assertEquals(mappedA, first);
            }
            else if (equal(mappedA, first)) {
                assertTrue(iterator.hasNext());
                assertEquals(mappedB, (int) iterator.next());
            }
            else if (equal(mappedB, first)) {
                assertTrue(iterator.hasNext());
                assertEquals(mappedA, (int) iterator.next());
            }
            else {
                fail("Expected either " + mappedA + " or " + mappedB + " but found " + first);
            }

            assertFalse(iterator.hasNext());
        })));
    }

    public void testToImmutableForEmpty() {
        assertSame(ImmutableHashSet.empty(), ImmutableHashSet.empty().toImmutable());
    }

    public void testMutateForEmpty() {
        final ImmutableHashSet<String> set1 = newBuilder().build();
        final MutableHashSet<String> set2 = set1.mutate();

        assertTrue(set2.isEmpty());

        set2.add("");
        assertFalse(set1.contains(""));
    }

    public void testToImmutable() {
        withValue(a -> withValue(b -> {
            final ImmutableHashSet<String> set = newBuilder().add(a).add(b).build();
            assertSame(set, set.toImmutable());
        }));
    }

    public void testMutate() {
        withValue(a -> withValue(b -> {
            final ImmutableHashSet<String> set1 = newBuilder().add(a).add(b).build();
            final MutableHashSet<String> set2 = set1.mutate();

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

    @Override
    public void testIndexOfForMultipleElements() {
        withValue(a -> withValue(b -> withValue(value -> {
            final int aHash = (a != null)? a.hashCode() : HASH_FOR_NULL;
            final int bHash = (b != null)? b.hashCode() : HASH_FOR_NULL;
            final boolean reversedOrder = bHash < aHash;
            final IterableCollection<String> set = newIterableBuilder().add(a).add(b).build();
            final int index = set.indexOf(value);

            if (aHash == bHash) {
                if (equal(a, value) || equal(b, value)) {
                    assertTrue(index == 0 || index == 1);
                }
                else {
                    assertEquals(-1, index);
                }
            }
            else {
                final int expectedIndex = (!reversedOrder && equal(a, value) || reversedOrder && equal(b, value)) ? 0 :
                        (!reversedOrder && equal(b, value) || reversedOrder && equal(a, value)) ? 1 : -1;
                assertEquals(expectedIndex, index);
            }
        })));
    }

    @Override
    public void testFindFirstForMultipleElements() {
        withFilterFunc(f -> withValue(defaultValue -> withValue(a -> withValue(b -> {
            final int aHash = (a != null)? a.hashCode() : HASH_FOR_NULL;
            final int bHash = (b != null)? b.hashCode() : HASH_FOR_NULL;
            final boolean reversedOrder = bHash < aHash;
            final IterableCollection<String> collection = newIterableBuilder().add(a).add(b).build();
            final String first = collection.findFirst(f, defaultValue);

            if (aHash == bHash) {
                if (f.apply(a) || f.apply(b)) {
                    assertTrue(equal(a, first) || equal(b, first));
                }
                else {
                    assertSame(defaultValue, first);
                }
            }
            else {
                final String expected = (!reversedOrder && f.apply(a) || reversedOrder && !f.apply(b) && f.apply(a))? a :
                        (reversedOrder && f.apply(b) || !reversedOrder && !f.apply(a) && f.apply(b))? b : defaultValue;
                assertSame(expected, first);
            }
        }))));
    }

    public void testToListWhenEmpty() {
        final ImmutableHashSet<String> set = newBuilder().build();
        assertTrue(set.isEmpty());
        assertTrue(set.toList().isEmpty());
    }

    public void testToList() {
        withValue(a -> withValue(b -> {
            final ImmutableHashSet<String> set = newBuilder().add(a).add(b).build();
            final ImmutableList<String> list = set.toList();

            if (equal(a, b)) {
                assertEquals(1, list.size());
                assertEquals(a, list.get(0));
            }
            else {
                assertEquals(2, list.size());

                if (equal(list.get(0), a)) {
                    assertEquals(b, list.get(1));
                }
                else {
                    assertEquals(a, list.get(1));
                    assertEquals(b, list.get(0));
                }
            }
        }));
    }
}

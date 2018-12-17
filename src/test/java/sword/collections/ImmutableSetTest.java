package sword.collections;

import java.util.Iterator;

import static sword.collections.SortUtils.equal;

public class ImmutableSetTest extends AbstractIterableImmutableTest<String> {

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

    boolean lessThan(String a, String b) {
        return b != null && (a == null || a.hashCode() < b.hashCode());
    }

    boolean sortByLength(String a, String b) {
        return b != null && (a == null || a.length() < b.length());
    }

    ImmutableSet.Builder<String> newBuilder() {
        return new ImmutableSet.Builder<>(this::lessThan);
    }

    @Override
    ImmutableSet.Builder<String> newIterableBuilder() {
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
            final ImmutableSet<String> list = newBuilder().add(a).add(b).build();
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
            final ImmutableSet<String> set = newBuilder().add(a).add(b).build();
            final Iterator<String> iterator = set.iterator();

            assertTrue(iterator.hasNext());
            final String first = iterator.next();

            if (lessThan(b, a)) {
                assertEquals(b, first);
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.next());
            }
            else {
                assertEquals(a, first);
                if (!equal(a, b)) {
                    assertTrue(iterator.hasNext());
                    assertEquals(b, iterator.next());
                }
            }

            assertFalse(iterator.hasNext());
        }));
    }

    @Override
    public void testMapForMultipleElements() {
        withMapFunc(f -> withValue(a -> withValue(b -> {
            final ImmutableSet<String> collection = newBuilder().add(a).add(b).build();
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
            final ImmutableSet<String> collection = newBuilder().add(a).add(b).build();
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
        final ImmutableSet set = newBuilder().build();
        assertSame(set, set.toImmutable());
    }

    public void testMutateForEmpty() {
        final ImmutableSet<String> set1 = newBuilder().build();
        final MutableSet<String> set2 = set1.mutate();

        assertTrue(set2.isEmpty());

        set2.add("");
        assertFalse(set1.contains(""));
    }

    public void testToImmutable() {
        withValue(a -> withValue(b -> {
            final ImmutableSet<String> set = newBuilder().add(a).add(b).build();
            assertSame(set, set.toImmutable());
        }));
    }

    public void testMutate() {
        withValue(a -> withValue(b -> {
            final ImmutableSet<String> set1 = newBuilder().add(a).add(b).build();
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

    @Override
    public void testIndexOfForMultipleElements() {
        withValue(a -> withValue(b -> withValue(value -> {
            final IterableCollection<String> set = newIterableBuilder().add(a).add(b).build();
            final int index = set.indexOf(value);

            final int expectedIndex;
            if (lessThan(b, a)) {
                expectedIndex = equal(value, b)? 0 : equal(value, a)? 1 : -1;
            }
            else {
                expectedIndex = equal(value, a)? 0 : equal(value, b)? 1 : -1;
            }
            assertEquals(expectedIndex, index);
        })));
    }

    @Override
    public void testFindFirstForMultipleElements() {
        withFilterFunc(f -> withValue(defaultValue -> withValue(a -> withValue(b -> {
            final IterableCollection<String> collection = newIterableBuilder().add(a).add(b).build();

            final String expected;
            if (lessThan(b, a)) {
                expected = f.apply(b)? b : f.apply(a)? a : defaultValue;
            }
            else {
                expected = f.apply(a)? a : f.apply(b)? b : defaultValue;
            }
            assertSame(expected, collection.findFirst(f, defaultValue));
        }))));
    }

    public void testToListWhenEmpty() {
        final ImmutableSet<String> set = newBuilder().build();
        assertTrue(set.isEmpty());
        assertTrue(set.toList().isEmpty());
    }

    public void testToList() {
        withValue(a -> withValue(b -> {
            final ImmutableSet<String> set = newBuilder().add(a).add(b).build();
            final ImmutableList<String> list = set.toList();

            if (equal(a, b)) {
                assertEquals(1, list.size());
                assertEquals(a, list.get(0));
            }
            else {
                assertEquals(2, list.size());

                if (lessThan(b, a)) {
                    assertEquals(b, list.get(0));
                    assertEquals(a, list.get(1));
                }
                else {
                    assertEquals(a, list.get(0));
                    assertEquals(b, list.get(1));
                }
            }
        }));
    }

    public void testHashCodeAndEquals() {
        withValue(a -> withValue(b -> {
            final ImmutableSet<String> set = newBuilder().add(a).add(b).build();
            final ImmutableSet<String> set1 = new ImmutableSet.Builder<String>(this::lessThan).add(a).add(b).build();
            final ImmutableSet<String> set2 = new ImmutableSet.Builder<String>(this::sortByLength).add(a).add(b).build();
            final ImmutableSet<String> set3 = new ImmutableHashSet.Builder<String>().add(a).add(b).build();

            assertEquals(set.hashCode(), set1.hashCode());
            assertEquals(set.hashCode(), set2.hashCode());
            assertEquals(set.hashCode(), set3.hashCode());

            assertEquals(set, set1);
            assertEquals(set, set2);
            assertEquals(set, set3);

            assertEquals(set.hashCode(), set1.mutate().hashCode());
            assertEquals(set.hashCode(), set2.mutate().hashCode());
            assertEquals(set.hashCode(), set3.mutate().hashCode());

            assertEquals(set, set1.mutate());
            assertEquals(set, set2.mutate());
            assertEquals(set, set3.mutate());
        }));
    }

    public void testSort() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableSet<String> set = newBuilder().add(a).add(b).add(c).build();
            final ImmutableSet.Builder<String> builder = new ImmutableSet.Builder<>(this::sortByLength);
            for (String v : set) {
                builder.add(v);
            }

            final ImmutableSet<String> sortedSet = set.sort(this::sortByLength);
            final Iterator<String> it = sortedSet.iterator();
            for (String v : builder.build()) {
                assertTrue(it.hasNext());
                assertSame(v, it.next());
            }
            assertFalse(it.hasNext());
        })));
    }

    public void testAdd() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableSet<String> set = newBuilder().add(a).add(b).build();
            final ImmutableSet<String> expectedSet = newBuilder().add(a).add(b).add(c).build();
            final ImmutableSet<String> unionSet = set.add(c);
            assertEquals(expectedSet, unionSet);

            if (expectedSet.equals(set)) {
                assertSame(unionSet, set);
            }
        })));
    }

    public void testAddAll() {
        withValue(a -> withValue(b -> withValue(c -> withValue(d -> {
            final ImmutableSet<String> set1 = newBuilder().add(a).add(b).build();
            final ImmutableSet<String> set2 = newBuilder().add(c).add(d).build();
            final ImmutableSet<String> expectedSet = newBuilder().add(a).add(b).add(c).add(d).build();
            final ImmutableSet<String> unionSet = set1.addAll(set2);
            assertEquals(expectedSet, unionSet);

            if (expectedSet.equals(set1)) {
                assertSame(unionSet, set1);
            }
        }))));
    }
}

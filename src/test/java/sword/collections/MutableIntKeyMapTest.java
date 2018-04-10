package sword.collections;

import java.util.Iterator;

import static sword.collections.TestUtils.withInt;
import static sword.collections.TestUtils.withString;

public class MutableIntKeyMapTest extends IntKeyMapTest<String> {

    @Override
    MutableIntKeyMap.Builder<String> newMapBuilder() {
        return new MutableIntKeyMap.Builder<>();
    }

    @Override
    void withValue(Procedure<String> procedure) {
        withString(procedure);
    }

    private boolean filterFunc(String value) {
        return value != null && !value.isEmpty();
    }

    @Override
    void withFilterFunc(Procedure<Predicate<String>> procedure) {
        procedure.apply(this::filterFunc);
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
    String getTestValue() {
        return "value";
    }

    @Override
    String getTestValue2() {
        return "value2";
    }

    @Override
    String valueForKey(int key) {
        return Integer.toString(key);
    }

    public void testToImmutableForEmpty() {
        assertTrue(newMapBuilder().build().toImmutable().isEmpty());
    }

    public void testMutateForEmpty() {
        final MutableIntKeyMap<String> map1 = new MutableIntKeyMap.Builder<String>().build();
        final MutableIntKeyMap<String> map2 = map1.mutate();

        assertEquals(map1, map2);
        assertNotSame(map1, map2);

        map1.put(1, "");
        assertEquals(null, map2.get(1, null));
    }

    public void testToImmutable() {
        withInt(a -> withInt(b -> {
            final MutableIntKeyMap.Builder<String> builder = newMapBuilder();
            final MutableIntKeyMap<String> map1 = builder.put(a, "").put(b, "").build();
            final ImmutableIntKeyMap<String> map2 = map1.toImmutable();

            final Iterator<IntKeyMap.Entry<String>> it1 = map1.entries().iterator();
            final Iterator<IntKeyMap.Entry<String>> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                final IntKeyMap.Entry<String> item1 = it1.next();
                final IntKeyMap.Entry<String> item2 = it2.next();
                assertEquals(item1.getKey(), item2.getKey());
                assertEquals(item1.getValue(), item2.getValue());
            }
            assertFalse(it2.hasNext());
        }));
    }

    public void testMutate() {
        final String defValue = "notFound!";
        withInt(a -> withInt(b -> {
            final MutableIntKeyMap.Builder<String> builder = newMapBuilder();
            final MutableIntKeyMap<String> map1 = builder.put(a, "").put(b, "").build();
            final MutableIntKeyMap<String> map2 = map1.mutate();

            final Iterator<IntKeyMap.Entry<String>> it1 = map1.entries().iterator();
            final Iterator<IntKeyMap.Entry<String>> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                final IntKeyMap.Entry<String> item1 = it1.next();
                final IntKeyMap.Entry<String> item2 = it2.next();
                assertEquals(item1.getKey(), item2.getKey());
                assertEquals(item1.getValue(), item2.getValue());
            }
            assertFalse(it2.hasNext());

            map2.remove(b);
            assertEquals("", map1.get(b, defValue));
            assertEquals(defValue, map2.get(b, defValue));
        }));
    }
}

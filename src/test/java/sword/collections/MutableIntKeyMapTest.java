package sword.collections;

import java.util.Iterator;

import static sword.collections.TestUtils.withInt;

public class MutableIntKeyMapTest extends IntKeyMapTest {

    @Override
    <E> MutableIntKeyMap.Builder<E> newBuilder() {
        return new MutableIntKeyMap.Builder<>();
    }

    public void testToImmutableForEmpty() {
        assertTrue(newBuilder().build().toImmutable().isEmpty());
    }

    public void testMutateForEmpty() {
        final MutableIntKeyMap<String> map1 = new MutableIntKeyMap.Builder<String>().build();
        final MutableIntKeyMap<String> map2 = map1.mutate();

        assertEquals(map1, map2);
        assertNotSame(map1, map2);

        map1.put(1, "");
        assertEquals(null, map2.get(1));
    }

    public void testToImmutable() {
        withInt(a -> withInt(b -> {
            final MutableIntKeyMap.Builder<String> builder = newBuilder();
            final MutableIntKeyMap<String> map1 = builder.put(a, "").put(b, "").build();
            final ImmutableIntKeyMap<String> map2 = map1.toImmutable();

            final Iterator<IntKeyMap.Entry<String>> it1 = map1.iterator();
            final Iterator<IntKeyMap.Entry<String>> it2 = map2.iterator();
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
        withInt(a -> withInt(b -> {
            final MutableIntKeyMap.Builder<String> builder = newBuilder();
            final MutableIntKeyMap<String> map1 = builder.put(a, "").put(b, "").build();
            final MutableIntKeyMap<String> map2 = map1.mutate();

            final Iterator<IntKeyMap.Entry<String>> it1 = map1.iterator();
            final Iterator<IntKeyMap.Entry<String>> it2 = map2.iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                final IntKeyMap.Entry<String> item1 = it1.next();
                final IntKeyMap.Entry<String> item2 = it2.next();
                assertEquals(item1.getKey(), item2.getKey());
                assertEquals(item1.getValue(), item2.getValue());
            }
            assertFalse(it2.hasNext());

            map2.remove(b);
            assertEquals("", map1.get(b));
            assertEquals(null, map2.get(b));
        }));
    }
}

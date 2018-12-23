package sword.collections;

import java.util.Iterator;

import static sword.collections.TestUtils.withInt;

public final class MutableIntPairMapTest extends IntPairMapTest {

    @Override
    MutableIntPairMap.Builder newBuilder() {
        return new MutableIntPairMap.Builder();
    }

    public void testToImmutableForEmpty() {
        assertTrue(newBuilder().build().toImmutable().isEmpty());
    }

    public void testMutateForEmpty() {
        final MutableIntPairMap map1 = newBuilder().build();
        final MutableIntPairMap map2 = map1.mutate();

        assertEquals(map1, map2);
        assertNotSame(map1, map2);

        map1.put(1, 1);
        assertEquals(0, map2.get(1, 0));
    }

    public void testToImmutable() {
        withInt(a -> withInt(b -> {
            final MutableIntPairMap map1 = newBuilder().put(a, 1).put(b, 2).build();
            final ImmutableIntPairMap map2 = map1.toImmutable();

            final Iterator<IntPairMap.Entry> it1 = map1.entries().iterator();
            final Iterator<IntPairMap.Entry> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                final IntPairMap.Entry item1 = it1.next();
                final IntPairMap.Entry item2 = it2.next();
                assertEquals(item1.key(), item2.key());
                assertEquals(item1.value(), item2.value());
            }
            assertFalse(it2.hasNext());
        }));
    }

    public void testMutate() {
        final int defValue = -2;
        withInt(a -> withInt(b -> {
            final MutableIntPairMap map1 = newBuilder().put(a, 1).put(b, 2).build();
            final MutableIntPairMap map2 = map1.mutate();

            final Iterator<IntPairMap.Entry> it1 = map1.entries().iterator();
            final Iterator<IntPairMap.Entry> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                final IntPairMap.Entry item1 = it1.next();
                final IntPairMap.Entry item2 = it2.next();
                assertEquals(item1.key(), item2.key());
                assertEquals(item1.value(), item2.value());
            }
            assertFalse(it2.hasNext());

            map2.remove(b);
            assertEquals(2, map1.get(b, defValue));
            assertEquals(defValue, map2.get(b, defValue));
        }));
    }

    public void testHashCode() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntPairMap mutable = newBuilder()
                    .put(a, b)
                    .put(b, c)
                    .put(c, a)
                    .build();
            final IntPairMap immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable.hashCode(), immutable.hashCode());
        })));
    }

    public void testEquals() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntPairMap mutable = newBuilder()
                    .put(a, b)
                    .put(b, c)
                    .put(c, a)
                    .build();
            final IntPairMap immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable, immutable);
            assertEquals(immutable, mutable);
        })));
    }

    public void testClearWhenEmpty() {
        final MutableIntPairMap collection = newBuilder().build();
        assertFalse(collection.clear());
        assertTrue(collection.isEmpty());
    }

    public void testClearForSingleItem() {
        withInt(value -> {
            final MutableIntPairMap collection = newBuilder()
                    .put(value, value)
                    .build();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        });
    }

    public void testClearForMultipleItems() {
        withInt(a -> withInt(b -> {
            final MutableIntPairMap collection = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .build();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        }));
    }
}

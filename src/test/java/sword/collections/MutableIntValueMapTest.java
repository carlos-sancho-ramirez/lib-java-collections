package sword.collections;

import static sword.collections.TestUtils.withInt;

public final class MutableIntValueMapTest extends IntValueMapTest {

    @Override
    IntValueMapBuilder<String> newBuilder() {
        return new MutableIntValueMap.Builder<>();
    }

    public void testHashCode() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntValueMap<String> mutable = newBuilder()
                    .put(Integer.toString(a), b)
                    .put(Integer.toString(b), c)
                    .put(Integer.toString(c), a)
                    .build();
            final IntValueMap<String> immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable.hashCode(), immutable.hashCode());
        })));
    }

    public void testEquals() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntValueMap<String> mutable = newBuilder()
                    .put(Integer.toString(a), b)
                    .put(Integer.toString(b), c)
                    .put(Integer.toString(c), a)
                    .build();
            final IntValueMap<String> immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable, immutable);
            assertEquals(immutable, mutable);
        })));
    }

    public void testClearWhenEmpty() {
        final MutableIntValueMap<String> collection = newBuilder().build().mutate();
        assertFalse(collection.clear());
        assertTrue(collection.isEmpty());
    }

    public void testClearForSingleItem() {
        withInt(value -> {
            final MutableIntValueMap<String> collection = newBuilder()
                    .put(Integer.toString(value), value)
                    .build().mutate();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        });
    }

    public void testClearForMultipleItems() {
        withInt(a -> withInt(b -> {
            final MutableIntValueMap<String> collection = newBuilder()
                    .put(Integer.toString(a), a)
                    .put(Integer.toString(b), b)
                    .build().mutate();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        }));
    }
}

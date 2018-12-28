package sword.collections;

import static sword.collections.TestUtils.withInt;

abstract class MutableIntValueMapTest<T> extends IntValueMapTest<T> {

    @Override
    abstract MutableIntValueMap.Builder<T> newBuilder();

    public void testClearWhenEmpty() {
        final MutableIntValueMap<T> collection = newBuilder().build().mutate();
        assertFalse(collection.clear());
        assertTrue(collection.isEmpty());
    }

    public void testClearForSingleItem() {
        withInt(value -> {
            final MutableIntValueMap<T> collection = newBuilder()
                    .put(keyFromInt(value), value)
                    .build().mutate();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        });
    }

    public void testClearForMultipleItems() {
        withInt(a -> withInt(b -> {
            final MutableIntValueMap<T> collection = newBuilder()
                    .put(keyFromInt(a), a)
                    .put(keyFromInt(b), b)
                    .build().mutate();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        }));
    }
}

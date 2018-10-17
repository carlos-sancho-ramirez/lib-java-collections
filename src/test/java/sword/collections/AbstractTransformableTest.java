package sword.collections;

import java.util.Iterator;

abstract class AbstractTransformableTest<T> extends AbstractIterableTest<T> {

    abstract TransformableBuilder<T> newIterableBuilder();

    public void testIndexesWhenEmpty() {
        assertTrue(newIterableBuilder().build().indexes().isEmpty());
    }

    public void testIndexesForSingleValue() {
        withValue(value -> {
            final Iterator<Integer> indexIterator = newIterableBuilder().add(value).build().indexes().iterator();
            assertTrue(indexIterator.hasNext());
            assertEquals(0, indexIterator.next().intValue());
            assertFalse(indexIterator.hasNext());
        });
    }

    public void testIndexesForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> {
            final Transformable<T> transformable = newIterableBuilder().add(a).add(b).add(c).build();
            final Iterator<T> it = transformable.iterator();
            int length = 0;
            while (it.hasNext()) {
                length++;
                it.next();
            }

            final Iterator<Integer> indexIterator = transformable.indexes().iterator();
            for (int i = 0; i < length; i++) {
                assertTrue(indexIterator.hasNext());
                assertEquals(i, indexIterator.next().intValue());
            }
            assertFalse(indexIterator.hasNext());
        })));
    }
}

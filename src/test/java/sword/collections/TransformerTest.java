package sword.collections;

import java.util.Iterator;

abstract class TransformerTest<T, B extends TransformableBuilder<T>> extends TraverserTest<T, B> {

    public void testIndexesWhenEmpty() {
        withBuilder(builder -> assertTrue(builder.build().iterator().indexes().isEmpty()));
    }

    public void testIndexesForSingleValue() {
        withValue(value -> withBuilder(builder -> {
            final Iterator<Integer> indexIterator = builder.add(value).build().iterator().indexes().iterator();
            assertTrue(indexIterator.hasNext());
            assertEquals(0, indexIterator.next().intValue());
            assertFalse(indexIterator.hasNext());
        }));
    }

    public void testIndexesForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final Transformable<T> transformable = builder.add(a).add(b).add(c).build();
            final Iterator<T> it = transformable.iterator();
            int length = 0;
            while (it.hasNext()) {
                length++;
                it.next();
            }

            final Iterator<Integer> indexIterator = transformable.iterator().indexes().iterator();
            for (int i = 0; i < length; i++) {
                assertTrue(indexIterator.hasNext());
                assertEquals(i, indexIterator.next().intValue());
            }
            assertFalse(indexIterator.hasNext());
        }))));
    }
}

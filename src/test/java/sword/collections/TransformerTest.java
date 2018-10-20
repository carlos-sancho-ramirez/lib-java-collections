package sword.collections;

import java.util.Iterator;

abstract class TransformerTest<T, B extends TransformableBuilder<T>> extends TraverserTest<T, B> {

    abstract void withMapToIntFunc(Procedure<IntResultFunction<T>> procedure);

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

    public void testMapToIntWhenEmpty() {
        final IntResultFunction<T> func = v -> {
            throw new AssertionError("This method should not be called");
        };
        withBuilder(builder -> assertTrue(builder.build().iterator().mapToInt(func).isEmpty()));
    }

    public void testMapToIntForSingleValue() {
        withMapToIntFunc(func -> withValue(a -> withBuilder(builder -> {
            final Transformable<T> transformable = builder.add(a).build();
            final ImmutableIntList expectedList = new ImmutableIntList.Builder().add(func.apply(a)).build();
            assertEquals(expectedList, transformable.iterator().mapToInt(func).toImmutable());
        })));
    }

    public void testMapToIntForMultipleValues() {
        withMapToIntFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final Transformable<T> transformable = builder.add(a).add(b).add(c).build();
            final ImmutableIntList.Builder expectedBuilder = new ImmutableIntList.Builder();
            for (T value : transformable) {
                expectedBuilder.add(func.apply(value));
            }
            assertEquals(expectedBuilder.build(), transformable.iterator().mapToInt(func).toImmutable());
        })))));
    }
}

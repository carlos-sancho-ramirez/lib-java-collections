package sword.collections;

import java.util.Iterator;

abstract class TransformerTest<T, B extends TransformableBuilder<T>> extends TraverserTest<T, B> {

    abstract void withMapToIntFunc(Procedure<IntResultFunction<T>> procedure);

    public void testToListWhenEmpty() {
        withBuilder(builder -> assertTrue(builder.build().iterator().toList().isEmpty()));
    }

    public void testToListForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final Transformable<T> transformable = builder.add(value).build();
            final List<T> expected = new ImmutableList.Builder<T>().add(value).build();
            assertEquals(expected, transformable.iterator().toList().toImmutable());
        }));
    }

    public void testToListForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final Transformable<T> transformable = builder.add(a).add(b).add(c).build();
            final ImmutableList.Builder<T> listBuilder = new ImmutableList.Builder<>();
            for (T value : transformable) {
                listBuilder.add(value);
            }
            assertEquals(listBuilder.build(), transformable.iterator().toList().toImmutable());
        }))));
    }

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
        withBuilder(builder -> assertFalse(builder.build().iterator().mapToInt(func).hasNext()));
    }

    public void testMapToIntForSingleValue() {
        withMapToIntFunc(func -> withValue(a -> withBuilder(builder -> {
            final IntTransformer transformer = builder.add(a).build().iterator().mapToInt(func);
            assertTrue(transformer.hasNext());
            assertEquals(func.apply(a), transformer.next().intValue());
            assertFalse(transformer.hasNext());
        })));
    }

    public void testMapToIntForMultipleValues() {
        withMapToIntFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final Transformable<T> transformable = builder.add(a).add(b).add(c).build();
            final ImmutableIntList.Builder expectedBuilder = new ImmutableIntList.Builder();
            for (T value : transformable) {
                expectedBuilder.add(func.apply(value));
            }

            final IntTransformer transformer = transformable.iterator().mapToInt(func);
            for (int newValue : expectedBuilder.build()) {
                assertTrue(transformer.hasNext());
                assertEquals(newValue, transformer.next().intValue());
            }
            assertFalse(transformer.hasNext());
        })))));
    }
}

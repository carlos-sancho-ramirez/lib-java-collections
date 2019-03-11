package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

abstract class IntTransformerTest<C extends IntTransformable, B extends IntTraversableBuilder<C>> extends IntTraverserTest<C, B> {

    abstract void withMapToIntFunc(Procedure<IntToIntFunction> procedure);
    abstract void withMapFunc(Procedure<IntFunction<Object>> procedure);

    @Test
    public void testToListWhenEmpty() {
        withBuilder(builder -> assertTrue(builder.build().iterator().toList().isEmpty()));
    }

    @Test
    public void testToListForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final C transformable = builder.add(value).build();
            final IntList expected = new ImmutableIntList.Builder().add(value).build();
            assertEquals(expected, transformable.iterator().toList().toImmutable());
        }));
    }

    @Test
    public void testToListForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final C transformable = builder.add(a).add(b).add(c).build();
            final ImmutableIntList.Builder listBuilder = new ImmutableIntList.Builder();
            for (int value : transformable) {
                listBuilder.add(value);
            }
            assertEquals(listBuilder.build(), transformable.iterator().toList().toImmutable());
        }))));
    }

    @Test
    public void testToSetWhenEmpty() {
        withBuilder(builder -> assertTrue(builder.build().iterator().toSet().isEmpty()));
    }

    @Test
    public void testToSetForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final IntSet set = builder.add(value).build().iterator().toSet();
            assertEquals(1, set.size());
            assertEquals(value, set.valueAt(0));
        }));
    }

    @Test
    public void testToSetForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final C transformable = builder.add(a).add(b).add(c).build();
            final ImmutableIntSetCreator setBuilder = new ImmutableIntSetCreator();
            for (int value : transformable) {
                setBuilder.add(value);
            }
            assertEquals(setBuilder.build(), transformable.iterator().toSet().toImmutable());
        }))));
    }

    @Test
    public void testIndexesWhenEmpty() {
        withBuilder(builder -> assertFalse(builder.build().iterator().indexes().hasNext()));
    }

    @Test
    public void testIndexesForSingleValue() {
        withValue(value -> withBuilder(builder -> {
            final Iterator<Integer> indexIterator = builder.add(value).build().iterator().indexes();
            assertTrue(indexIterator.hasNext());
            assertEquals(0, indexIterator.next().intValue());
            assertFalse(indexIterator.hasNext());
        }));
    }

    @Test
    public void testIndexesForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final C transformable = builder.add(a).add(b).add(c).build();
            final IntTransformer it = transformable.iterator();
            int length = 0;
            while (it.hasNext()) {
                length++;
                it.next();
            }

            final IntTransformer indexIterator = transformable.iterator().indexes();
            for (int i = 0; i < length; i++) {
                assertTrue(indexIterator.hasNext());
                assertEquals(i, indexIterator.next().intValue());
            }
            assertFalse(indexIterator.hasNext());
        }))));
    }

    @Test
    public void testFilterWhenEmpty() {
        final IntPredicate func = value -> {
            throw new AssertionError("Should never be called");
        };
        withBuilder(builder -> assertFalse(builder.build().iterator().filter(func).hasNext()));
    }

    @Test
    public void testFilterForSingleElement() {
        withFilterFunc(func -> withValue(value -> withBuilder(builder -> {
            final IntTransformer transformer = builder.add(value).build().iterator().filter(func);
            if (func.apply(value)) {
                assertTrue(transformer.hasNext());
                assertEquals(value, transformer.next().intValue());
            }
            assertFalse(transformer.hasNext());
        })));
    }

    @Test
    public void testFilterForMultipleElements() {
        withFilterFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final C transformable = builder.add(a).add(b).add(c).build();
            final IntTransformer transformer = transformable.iterator().filter(func);
            for (int value : transformable) {
                if (func.apply(value)) {
                    assertTrue(transformer.hasNext());
                    assertEquals(value, transformer.next().intValue());
                }
            }
            assertFalse(transformer.hasNext());
        })))));
    }

    @Test
    public void testFilterNotWhenEmpty() {
        final IntPredicate func = value -> {
            throw new AssertionError("Should never be called");
        };
        withBuilder(builder -> assertFalse(builder.build().iterator().filterNot(func).hasNext()));
    }

    @Test
    public void testFilterNotForSingleElement() {
        withFilterFunc(func -> withValue(value -> withBuilder(builder -> {
            final IntTransformer transformer = builder.add(value).build().iterator().filterNot(func);
            if (!func.apply(value)) {
                assertTrue(transformer.hasNext());
                assertEquals(value, transformer.next().intValue());
            }
            assertFalse(transformer.hasNext());
        })));
    }

    @Test
    public void testFilterNotForMultipleElements() {
        withFilterFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final C transformable = builder.add(a).add(b).add(c).build();
            final IntTransformer transformer = transformable.iterator().filterNot(func);
            for (int value : transformable) {
                if (!func.apply(value)) {
                    assertTrue(transformer.hasNext());
                    assertEquals(value, transformer.next().intValue());
                }
            }
            assertFalse(transformer.hasNext());
        })))));
    }

    @Test
    public void testMapToIntWhenEmpty() {
        final IntToIntFunction func = v -> {
            throw new AssertionError("This method should not be called");
        };
        withBuilder(builder -> assertFalse(builder.build().iterator().mapToInt(func).hasNext()));
    }

    @Test
    public void testMapToIntForSingleValue() {
        withMapToIntFunc(func -> withValue(a -> withBuilder(builder -> {
            final IntTransformer transformer = builder.add(a).build().iterator().mapToInt(func);
            assertTrue(transformer.hasNext());
            assertEquals(func.apply(a), transformer.next().intValue());
            assertFalse(transformer.hasNext());
        })));
    }

    @Test
    public void testMapToIntForMultipleValues() {
        withMapToIntFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final C transformable = builder.add(a).add(b).add(c).build();
            final IntTransformer transformer = transformable.iterator().mapToInt(func);
            for (int value : transformable) {
                assertTrue(transformer.hasNext());
                assertEquals(func.apply(value), transformer.next().intValue());
            }
            assertFalse(transformer.hasNext());
        })))));
    }

    @Test
    public void testMapWhenEmpty() {
        final IntFunction func = v -> {
            throw new AssertionError("This method should not be called");
        };
        withBuilder(builder -> assertFalse(builder.build().iterator().map(func).hasNext()));
    }

    @Test
    public void testMapForSingleValue() {
        withMapFunc(func -> withValue(a -> withBuilder(builder -> {
            final Transformer transformer = builder.add(a).build().iterator().map(func);
            assertTrue(transformer.hasNext());
            assertEquals(func.apply(a), transformer.next());
            assertFalse(transformer.hasNext());
        })));
    }

    @Test
    public void testMapForMultipleValues() {
        withMapFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final C transformable = builder.add(a).add(b).add(c).build();
            final Transformer transformer = transformable.iterator().map(func);
            for (int value : transformable) {
                assertTrue(transformer.hasNext());
                assertEquals(func.apply(value), transformer.next());
            }
            assertFalse(transformer.hasNext());
        })))));
    }
}

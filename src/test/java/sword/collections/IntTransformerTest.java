package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class IntTransformerTest<B extends IntTransformableBuilder> extends IntTraverserTest<B> {

    abstract void withMapToIntFunc(Procedure<IntToIntFunction> procedure);
    abstract void withMapFunc(Procedure<IntFunction<Object>> procedure);

    @Test
    void testToListWhenEmpty() {
        withBuilder(builder -> assertTrue(builder.build().iterator().toList().isEmpty()));
    }

    @Test
    void testToListForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final IntTransformable transformable = builder.add(value).build();
            final IntList expected = new ImmutableIntList.Builder().add(value).build();
            assertEquals(expected, transformable.iterator().toList().toImmutable());
        }));
    }

    @Test
    void testToListForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTransformable transformable = builder.add(a).add(b).add(c).build();
            final ImmutableIntList.Builder listBuilder = new ImmutableIntList.Builder();
            for (int value : transformable) {
                listBuilder.add(value);
            }
            assertEquals(listBuilder.build(), transformable.iterator().toList().toImmutable());
        }))));
    }

    @Test
    void testToSetWhenEmpty() {
        withBuilder(builder -> assertTrue(builder.build().iterator().toSet().isEmpty()));
    }

    @Test
    void testToSetForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final IntSet set = builder.add(value).build().iterator().toSet();
            assertEquals(1, set.size());
            assertEquals(value, set.valueAt(0));
        }));
    }

    @Test
    void testToSetForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTransformable transformable = builder.add(a).add(b).add(c).build();
            final ImmutableIntSetCreator setBuilder = new ImmutableIntSetCreator();
            for (int value : transformable) {
                setBuilder.add(value);
            }
            assertEquals(setBuilder.build(), transformable.iterator().toSet().toImmutable());
        }))));
    }

    @Test
    void testIndexesWhenEmpty() {
        withBuilder(builder -> assertFalse(builder.build().iterator().indexes().hasNext()));
    }

    @Test
    void testIndexesForSingleValue() {
        withValue(value -> withBuilder(builder -> {
            final Iterator<Integer> indexIterator = builder.add(value).build().iterator().indexes();
            assertTrue(indexIterator.hasNext());
            assertEquals(0, indexIterator.next().intValue());
            assertFalse(indexIterator.hasNext());
        }));
    }

    @Test
    void testIndexesForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTransformable transformable = builder.add(a).add(b).add(c).build();
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
    void testFilterWhenEmpty() {
        final IntPredicate func = value -> {
            throw new AssertionError("Should never be called");
        };
        withBuilder(builder -> assertFalse(builder.build().iterator().filter(func).hasNext()));
    }

    @Test
    void testFilterForSingleElement() {
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
    void testFilterForMultipleElements() {
        withFilterFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTransformable transformable = builder.add(a).add(b).add(c).build();
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
    void testFilterNotWhenEmpty() {
        final IntPredicate func = value -> {
            throw new AssertionError("Should never be called");
        };
        withBuilder(builder -> assertFalse(builder.build().iterator().filterNot(func).hasNext()));
    }

    @Test
    void testFilterNotForSingleElement() {
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
    void testFilterNotForMultipleElements() {
        withFilterFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTransformable transformable = builder.add(a).add(b).add(c).build();
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
    void testMapToIntWhenEmpty() {
        final IntToIntFunction func = v -> {
            throw new AssertionError("This method should not be called");
        };
        withBuilder(builder -> assertFalse(builder.build().iterator().mapToInt(func).hasNext()));
    }

    @Test
    void testMapToIntForSingleValue() {
        withMapToIntFunc(func -> withValue(a -> withBuilder(builder -> {
            final IntTransformer transformer = builder.add(a).build().iterator().mapToInt(func);
            assertTrue(transformer.hasNext());
            assertEquals(func.apply(a), transformer.next().intValue());
            assertFalse(transformer.hasNext());
        })));
    }

    @Test
    void testMapToIntForMultipleValues() {
        withMapToIntFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTransformable transformable = builder.add(a).add(b).add(c).build();
            final IntTransformer transformer = transformable.iterator().mapToInt(func);
            for (int value : transformable) {
                assertTrue(transformer.hasNext());
                assertEquals(func.apply(value), transformer.next().intValue());
            }
            assertFalse(transformer.hasNext());
        })))));
    }

    @Test
    void testMapWhenEmpty() {
        final IntFunction func = v -> {
            throw new AssertionError("This method should not be called");
        };
        withBuilder(builder -> assertFalse(builder.build().iterator().map(func).hasNext()));
    }

    @Test
    void testMapForSingleValue() {
        withMapFunc(func -> withValue(a -> withBuilder(builder -> {
            final Transformer transformer = builder.add(a).build().iterator().map(func);
            assertTrue(transformer.hasNext());
            assertEquals(func.apply(a), transformer.next());
            assertFalse(transformer.hasNext());
        })));
    }

    @Test
    void testMapForMultipleValues() {
        withMapFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTransformable transformable = builder.add(a).add(b).add(c).build();
            final Transformer transformer = transformable.iterator().map(func);
            for (int value : transformable) {
                assertTrue(transformer.hasNext());
                assertEquals(func.apply(value), transformer.next());
            }
            assertFalse(transformer.hasNext());
        })))));
    }

    @Test
    void testCountWhenEmpty() {
        withBuilder(builder -> {
            final IntPairMap map = builder.build().iterator().count();
            assertTrue(map.isEmpty());
        });
    }

    @Test
    void testCountForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final IntPairMap map = builder.add(value).build().iterator().count();
            assertEquals(1, map.size());
            assertEquals(value, map.keyAt(0));
            assertEquals(1, map.valueAt(0));
        }));
    }

    @Test
    void testCountForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final IntTransformable transformable = builder.add(a).add(b).add(c).build();
            final IntPairMap map = transformable.iterator().count();

            final MutableIntPairMap expected = MutableIntPairMap.empty();
            for (int value : transformable) {
                final int count = expected.get(value, 0);
                expected.put(value, count + 1);
            }

            assertEquals(expected.size(), map.size());
            for (int value : expected.keySet()) {
                assertEquals(expected.get(value), map.get(value));
            }
        }))));
    }
}

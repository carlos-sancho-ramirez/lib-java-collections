package sword.collections;

import java.util.Iterator;

abstract class TransformerTest<T, B extends TransformableBuilder<T>> extends TraverserTest<T, B> {

    abstract void withMapToIntFunc(Procedure<IntResultFunction<T>> procedure);
    abstract void withMapFunc(Procedure<Function<T, Object>> procedure);

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

    public void testToSetWhenEmpty() {
        withBuilder(builder -> assertTrue(builder.build().iterator().toSet().isEmpty()));
    }

    public void testToSetForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final Set<T> set = builder.add(value).build().iterator().toSet();
            assertEquals(1, set.size());
            assertEquals(value, set.valueAt(0));
        }));
    }

    public void testToSetForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final Transformable<T> transformable = builder.add(a).add(b).add(c).build();
            final ImmutableHashSet.Builder<T> setBuilder = new ImmutableHashSet.Builder<>();
            for (T value : transformable) {
                setBuilder.add(value);
            }
            assertEquals(setBuilder.build(), transformable.iterator().toSet().toImmutable());
        }))));
    }

    public void testIndexesWhenEmpty() {
        withBuilder(builder -> assertFalse(builder.build().iterator().indexes().hasNext()));
    }

    public void testIndexesForSingleValue() {
        withValue(value -> withBuilder(builder -> {
            final Iterator<Integer> indexIterator = builder.add(value).build().iterator().indexes();
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

            final IntTransformer indexIterator = transformable.iterator().indexes();
            for (int i = 0; i < length; i++) {
                assertTrue(indexIterator.hasNext());
                assertEquals(i, indexIterator.next().intValue());
            }
            assertFalse(indexIterator.hasNext());
        }))));
    }

    public void testFilterWhenEmpty() {
        final Predicate<T> func = value -> {
            throw new AssertionError("Should never be called");
        };
        withBuilder(builder -> assertFalse(builder.build().iterator().filter(func).hasNext()));
    }

    public void testFilterForSingleElement() {
        withFilterFunc(func -> withValue(value -> withBuilder(builder -> {
            final Transformer<T> transformer = builder.add(value).build().iterator().filter(func);
            if (func.apply(value)) {
                assertTrue(transformer.hasNext());
                assertEquals(value, transformer.next());
            }
            assertFalse(transformer.hasNext());
        })));
    }

    public void testFilterForMultipleElements() {
        withFilterFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final Transformable<T> transformable = builder.add(a).add(b).add(c).build();
            final Transformer<T> transformer = transformable.iterator().filter(func);
            for (T value : transformable) {
                if (func.apply(value)) {
                    assertTrue(transformer.hasNext());
                    assertEquals(value, transformer.next());
                }
            }
            assertFalse(transformer.hasNext());
        })))));
    }

    public void testFilterNotWhenEmpty() {
        final Predicate<T> func = value -> {
            throw new AssertionError("Should never be called");
        };
        withBuilder(builder -> assertFalse(builder.build().iterator().filterNot(func).hasNext()));
    }

    public void testFilterNotForSingleElement() {
        withFilterFunc(func -> withValue(value -> withBuilder(builder -> {
            final Transformer<T> transformer = builder.add(value).build().iterator().filterNot(func);
            if (!func.apply(value)) {
                assertTrue(transformer.hasNext());
                assertEquals(value, transformer.next());
            }
            assertFalse(transformer.hasNext());
        })));
    }

    public void testFilterNotForMultipleElements() {
        withFilterFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final Transformable<T> transformable = builder.add(a).add(b).add(c).build();
            final Transformer<T> transformer = transformable.iterator().filterNot(func);
            for (T value : transformable) {
                if (!func.apply(value)) {
                    assertTrue(transformer.hasNext());
                    assertEquals(value, transformer.next());
                }
            }
            assertFalse(transformer.hasNext());
        })))));
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
            final IntTransformer transformer = transformable.iterator().mapToInt(func);
            for (T value : transformable) {
                assertTrue(transformer.hasNext());
                assertEquals(func.apply(value), transformer.next().intValue());
            }
            assertFalse(transformer.hasNext());
        })))));
    }

    public void testMapWhenEmpty() {
        final Function<T, T> func = v -> {
            throw new AssertionError("This method should not be called");
        };
        withBuilder(builder -> assertFalse(builder.build().iterator().map(func).hasNext()));
    }

    public void testMapForSingleValue() {
        withMapFunc(func -> withValue(a -> withBuilder(builder -> {
            final Transformer transformer = builder.add(a).build().iterator().map(func);
            assertTrue(transformer.hasNext());
            assertEquals(func.apply(a), transformer.next());
            assertFalse(transformer.hasNext());
        })));
    }

    public void testMapForMultipleValues() {
        withMapFunc(func -> withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final Transformable<T> transformable = builder.add(a).add(b).add(c).build();
            final Transformer transformer = transformable.iterator().map(func);
            for (T value : transformable) {
                assertTrue(transformer.hasNext());
                assertEquals(func.apply(value), transformer.next());
            }
            assertFalse(transformer.hasNext());
        })))));
    }
}

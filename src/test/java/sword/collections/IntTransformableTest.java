package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

abstract class IntTransformableTest<B extends IntTransformableBuilder> extends IntTraversableTest<B> {

    abstract IntTransformableBuilder newIntBuilder();
    abstract void withMapFunc(Procedure<IntFunction<String>> procedure);
    abstract void withMapToIntFunc(Procedure<IntToIntFunction> procedure);

    @Test
    public void testToListWhenEmpty() {
        final IntTransformable transformable = newIntBuilder().build();
        assertTrue(transformable.isEmpty());
        assertTrue(transformable.toList().isEmpty());
    }

    @Test
    public void testToListForASingleElement() {
        withValue(value -> {
            final IntTransformable transformable = newIntBuilder().add(value).build();
            final IntList list = transformable.toList();

            final IntTransformer transformer = transformable.iterator();
            for (int v : list) {
                assertTrue(transformer.hasNext());
                assertEquals(v, transformer.next());
            }
            assertFalse(transformer.hasNext());
        });
    }

    @Test
    public void testToListForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> {
            final IntTransformable transformable = newIntBuilder().add(a).add(b).add(c).build();
            final IntList list = transformable.toList();

            final IntTransformer transformer = transformable.iterator();
            for (int value : list) {
                assertTrue(transformer.hasNext());
                assertEquals(value, transformer.next());
            }
            assertFalse(transformer.hasNext());
        })));
    }

    @Test
    public void testToSetWhenEmpty() {
        assertTrue(newIntBuilder().build().toSet().isEmpty());
    }

    @Test
    public void testToSetForASingleElement() {
        withValue(a -> {
            final IntTransformable transformable = newIntBuilder().add(a).build();
            final IntSet set = transformable.toSet();
            assertEquals(1, set.size());
            assertEquals(a, set.valueAt(0));
        });
    }

    @Test
    public void testToSetForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> {
            final IntTransformable transformable = newIntBuilder().add(a).add(b).add(c).build();
            final IntSet set = transformable.toSet();
            int count = 0;
            for (int setValue : set) {
                boolean found = false;
                for (int transValue : transformable) {
                    if (setValue == transValue) {
                        count++;
                        found = true;
                    }
                }
                assertTrue(found);
            }

            assertEquals(count, transformable.size());
        })));
    }

    @Test
    public void testIndexesWhenEmpty() {
        assertTrue(newIntBuilder().build().indexes().isEmpty());
    }

    @Test
    public void testIndexesForSingleValue() {
        withValue(value -> {
            final IntTransformer indexIterator = newIntBuilder().add(value).build().indexes().iterator();
            assertTrue(indexIterator.hasNext());
            assertEquals(0, indexIterator.next().intValue());
            assertFalse(indexIterator.hasNext());
        });
    }

    @Test
    public void testIndexesForMultipleValues() {
        withValue(a -> withValue(b -> withValue(c -> {
            final IntTransformable transformable = newIntBuilder().add(a).add(b).add(c).build();
            final IntTransformer transformer = transformable.iterator();
            int length = 0;
            while (transformer.hasNext()) {
                length++;
                transformer.next();
            }

            final Iterator<Integer> indexIterator = transformable.indexes().iterator();
            for (int i = 0; i < length; i++) {
                assertTrue(indexIterator.hasNext());
                assertEquals(i, indexIterator.next().intValue());
            }
            assertFalse(indexIterator.hasNext());
        })));
    }

    public void testFilterWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().filter(f).iterator().hasNext());
    }

    @Test
    public void testFilterForSingleElement() {
        withFilterFunc(f -> withValue(value -> {
            final IntTransformable transformable = newIntBuilder().add(value).build();
            final IntTransformable filtered = transformable.filter(f);

            if (f.apply(value)) {
                assertEquals(transformable, filtered);
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        }));
    }

    @Test
    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withValue(a -> withValue(b -> {
            final IntTransformable transformable = newIntBuilder().add(a).add(b).build();
            final IntTransformable filtered = transformable.filter(f);

            final boolean aPassed = f.apply(a);
            final boolean bPassed = f.apply(b);

            if (aPassed && bPassed) {
                assertEquals(transformable, filtered);
            }
            else if (aPassed) {
                Iterator<Integer> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.next().intValue());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<Integer> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(b, iterator.next().intValue());
                assertFalse(iterator.hasNext());
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        })));
    }

    @Test
    public void testFilterNotWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().filterNot(f).iterator().hasNext());
    }

    @Test
    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withValue(value -> {
            final IntTransformable transformable = newIntBuilder().add(value).build();
            final IntTransformable filtered = transformable.filterNot(f);

            if (f.apply(value)) {
                assertFalse(filtered.iterator().hasNext());
            }
            else {
                assertEquals(transformable, filtered);
            }
        }));
    }

    @Test
    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withValue(a -> withValue(b -> {
            final IntTransformable transformable = newIntBuilder().add(a).add(b).build();
            final IntTransformable filtered = transformable.filterNot(f);

            final boolean aRemoved = f.apply(a);
            final boolean bRemoved = f.apply(b);

            if (aRemoved && bRemoved) {
                assertFalse(filtered.iterator().hasNext());
            }
            else if (aRemoved) {
                Iterator<Integer> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(b, iterator.next().intValue());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<Integer> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.next().intValue());
                assertFalse(iterator.hasNext());
            }
            else {
                assertEquals(transformable, filtered);
            }
        })));
    }

    @Test
    public void testMapWhenEmpty() {
        final IntFunction<String> func = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().map(func).iterator().hasNext());
    }

    @Test
    public void testMapForSingleElement() {
        withMapFunc(f -> withValue(value -> {
            final Transformer<String> transformer = newIntBuilder().add(value).build().map(f).iterator();
            assertTrue(transformer.hasNext());
            assertEquals(f.apply(value), transformer.next());
            assertFalse(transformer.hasNext());
        }));
    }

    @Test
    public void testMapForMultipleElements() {
        withMapFunc(f -> withValue(a -> withValue(b -> {
            final IntTransformable transformable = newIntBuilder().add(a).add(b).build();
            final IntTransformer transformer = transformable.iterator();
            final Transformer<String> mappedIterator = transformable.map(f).iterator();
            while (transformer.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(transformer.next()), mappedIterator.next());
            }

            assertFalse(mappedIterator.hasNext());
        })));
    }

    @Test
    public void testMapToIntWhenEmpty() {
        final IntToIntFunction func = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().mapToInt(func).iterator().hasNext());
    }

    @Test
    public void testMapToIntForSingleElement() {
        withMapToIntFunc(f -> withValue(value -> {
            final IntTransformer transformer = newIntBuilder().add(value).build().mapToInt(f).iterator();
            assertTrue(transformer.hasNext());
            assertEquals(f.apply(value), transformer.next().intValue());
            assertFalse(transformer.hasNext());
        }));
    }

    @Test
    public void testMapToIntForMultipleElements() {
        withMapToIntFunc(f -> withValue(a -> withValue(b -> {
            final IntTransformable transformable = newIntBuilder().add(a).add(b).build();
            final IntTransformer transformer = transformable.iterator();
            final IntTransformer mappedIterator = transformable.mapToInt(f).iterator();
            while (transformer.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(transformer.next()), mappedIterator.next().intValue());
            }

            assertFalse(mappedIterator.hasNext());
        })));
    }
}

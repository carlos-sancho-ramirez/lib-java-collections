package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class IntTransformableTest<B extends IntTransformableBuilder> implements IntTraversableTest<B> {

    @Override
    public abstract IntTransformableBuilder newIntBuilder();
    abstract void withMapFunc(Procedure<IntFunction<String>> procedure);
    abstract void withMapToIntFunc(Procedure<IntToIntFunction> procedure);

    @Test
    void testToListWhenEmpty() {
        final IntTransformable transformable = newIntBuilder().build();
        assertTrue(transformable.isEmpty());
        assertTrue(transformable.toList().isEmpty());
    }

    @Test
    void testToListForASingleElement() {
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
    void testToListForMultipleElements() {
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
    void testToSetWhenEmpty() {
        assertTrue(newIntBuilder().build().toSet().isEmpty());
    }

    @Test
    void testToSetForASingleElement() {
        withValue(a -> {
            final IntTransformable transformable = newIntBuilder().add(a).build();
            final IntSet set = transformable.toSet();
            assertEquals(1, set.size());
            assertEquals(a, set.valueAt(0));
        });
    }

    @Test
    void testToSetForMultipleElements() {
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
    void testIndexesWhenEmpty() {
        assertTrue(newIntBuilder().build().indexes().isEmpty());
    }

    @Test
    void testIndexesForSingleValue() {
        withValue(value -> {
            final IntTransformer indexIterator = newIntBuilder().add(value).build().indexes().iterator();
            assertTrue(indexIterator.hasNext());
            assertEquals(0, indexIterator.next().intValue());
            assertFalse(indexIterator.hasNext());
        });
    }

    @Test
    void testIndexesForMultipleValues() {
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

    @Test
    void testFilterWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().filter(f).iterator().hasNext());
    }

    @Test
    void testFilterForSingleElement() {
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
    void testFilterForMultipleElements() {
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
    void testFilterNotWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().filterNot(f).iterator().hasNext());
    }

    @Test
    void testFilterNotForSingleElement() {
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
    void testFilterNotForMultipleElements() {
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
    void testMapWhenEmpty() {
        final IntFunction<String> func = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().map(func).iterator().hasNext());
    }

    @Test
    void testMapForSingleElement() {
        withMapFunc(f -> withValue(value -> {
            final Transformer<String> transformer = newIntBuilder().add(value).build().map(f).iterator();
            assertTrue(transformer.hasNext());
            assertEquals(f.apply(value), transformer.next());
            assertFalse(transformer.hasNext());
        }));
    }

    @Test
    void testMapForMultipleElements() {
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
    void testMapToIntWhenEmpty() {
        final IntToIntFunction func = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().mapToInt(func).iterator().hasNext());
    }

    @Test
    void testMapToIntForSingleElement() {
        withMapToIntFunc(f -> withValue(value -> {
            final IntTransformer transformer = newIntBuilder().add(value).build().mapToInt(f).iterator();
            assertTrue(transformer.hasNext());
            assertEquals(f.apply(value), transformer.next().intValue());
            assertFalse(transformer.hasNext());
        }));
    }

    @Test
    void testMapToIntForMultipleElements() {
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

    @Test
    void testCountWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntPairMap map = supplier.newBuilder().build().count();
            assertTrue(map.isEmpty());
        });
    }

    @Test
    void testCountForSingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final IntPairMap map = supplier.newBuilder().add(value).build().count();
            assertEquals(1, map.size());
            assertEquals(value, map.keyAt(0));
            assertEquals(1, map.valueAt(0));
        }));
    }

    @Test
    void testCountForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntTransformable transformable = supplier.newBuilder().add(a).add(b).add(c).build();
            final IntPairMap map = transformable.count();

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

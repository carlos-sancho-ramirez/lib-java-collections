package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

interface IntTransformableTest<B extends IntTransformableBuilder> extends IntTraversableTest<B> {

    @Override
    IntTransformableBuilder newIntBuilder();

    default void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    default void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v * v);
        procedure.apply(v -> v + 1);
    }

    @Test
    default void testToListWhenEmpty() {
        final IntTransformable transformable = newIntBuilder().build();
        assertTrue(transformable.isEmpty());
        assertTrue(transformable.toList().isEmpty());
    }

    @Test
    default void testToListForASingleElement() {
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
    default void testToListForMultipleElements() {
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
    default void testToSetWhenEmpty() {
        assertTrue(newIntBuilder().build().toSet().isEmpty());
    }

    @Test
    default void testToSetForASingleElement() {
        withValue(a -> {
            final IntTransformable transformable = newIntBuilder().add(a).build();
            final IntSet set = transformable.toSet();
            assertEquals(1, set.size());
            assertEquals(a, set.valueAt(0));
        });
    }

    @Test
    default void testToSetForMultipleElements() {
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
    default void testIndexesWhenEmpty() {
        assertTrue(newIntBuilder().build().indexes().isEmpty());
    }

    @Test
    default void testIndexesForSingleValue() {
        withValue(value -> {
            final IntTransformer indexIterator = newIntBuilder().add(value).build().indexes().iterator();
            assertTrue(indexIterator.hasNext());
            assertEquals(0, indexIterator.next().intValue());
            assertFalse(indexIterator.hasNext());
        });
    }

    @Test
    default void testIndexesForMultipleValues() {
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
    default void testFilterWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().filter(f).iterator().hasNext());
    }

    @Test
    default void testFilterForSingleElement() {
        withFilterFunc(f -> withValue(value -> {
            final IntTransformable transformable = newIntBuilder().add(value).build();
            final IntTransformable filtered = transformable.filter(f);

            if (f.apply(value)) {
                assertTrue(transformable.equalTraversable(filtered));
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        }));
    }

    @Test
    default void testFilterForMultipleElements() {
        withFilterFunc(f -> withValue(a -> withValue(b -> {
            final IntTransformable transformable = newIntBuilder().add(a).add(b).build();
            final IntTransformable filtered = transformable.filter(f);

            final boolean aPassed = f.apply(a);
            final boolean bPassed = f.apply(b);

            if (aPassed && bPassed) {
                assertTrue(transformable.equalTraversable(filtered));
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
    default void testFilterNotWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().filterNot(f).iterator().hasNext());
    }

    @Test
    default void testFilterNotForSingleElement() {
        withFilterFunc(f -> withValue(value -> {
            final IntTransformable transformable = newIntBuilder().add(value).build();
            final IntTransformable filtered = transformable.filterNot(f);

            if (f.apply(value)) {
                assertFalse(filtered.iterator().hasNext());
            }
            else {
                assertTrue(transformable.equalTraversable(filtered));
            }
        }));
    }

    @Test
    default void testFilterNotForMultipleElements() {
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
                assertTrue(transformable.equalTraversable(filtered));
            }
        })));
    }

    @Test
    default void testMapWhenEmpty() {
        final IntFunction<String> func = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().map(func).iterator().hasNext());
    }

    @Test
    default void testMapForSingleElement() {
        withMapFunc(f -> withValue(value -> {
            final Transformer<String> transformer = newIntBuilder().add(value).build().map(f).iterator();
            assertTrue(transformer.hasNext());
            assertEquals(f.apply(value), transformer.next());
            assertFalse(transformer.hasNext());
        }));
    }

    @Test
    default void testMapForMultipleElements() {
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
    default void testMapToIntWhenEmpty() {
        final IntToIntFunction func = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().mapToInt(func).iterator().hasNext());
    }

    @Test
    default void testMapToIntForSingleElement() {
        withMapToIntFunc(f -> withValue(value -> {
            final IntTransformer transformer = newIntBuilder().add(value).build().mapToInt(f).iterator();
            assertTrue(transformer.hasNext());
            assertEquals(f.apply(value), transformer.next().intValue());
            assertFalse(transformer.hasNext());
        }));
    }

    @Test
    default void testMapToIntForMultipleElements() {
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
    default void testCountWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntPairMap map = supplier.newBuilder().build().count();
            assertTrue(map.isEmpty());
        });
    }

    @Test
    default void testCountForSingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final IntPairMap map = supplier.newBuilder().add(value).build().count();
            assertEquals(1, map.size());
            assertEquals(value, map.keyAt(0));
            assertEquals(1, map.valueAt(0));
        }));
    }

    @Test
    default void testCountForMultipleElements() {
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

    @Test
    default void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntTransformable set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final int first = set.valueAt(0);
            final int second = (size >= 2)? set.valueAt(1) : 0;
            final int third = (size >= 3)? set.valueAt(2) : 0;

            final IntTransformable sliceA = set.slice(new ImmutableIntRange(0, 0));
            assertEquals(1, sliceA.size());
            assertEquals(first, sliceA.valueAt(0));

            final IntTransformable sliceB = set.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertEquals(second, sliceB.valueAt(0));
            }
            else {
                assertTrue(sliceB.isEmpty());
            }

            final IntTransformable sliceC = set.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertEquals(third, sliceC.valueAt(0));
            }
            else {
                assertTrue(sliceC.isEmpty());
            }

            final IntTransformable sliceAB = set.slice(new ImmutableIntRange(0, 1));
            if (size >= 2) {
                assertEquals(2, sliceAB.size());
                assertEquals(second, sliceAB.valueAt(1));
            }
            else {
                assertEquals(1, sliceAB.size());
            }
            assertEquals(first, sliceAB.valueAt(0));

            final IntTransformable sliceBC = set.slice(new ImmutableIntRange(1, 2));
            assertEquals(size - 1, sliceBC.size());
            if (size == 2) {
                assertEquals(second, sliceBC.valueAt(0));
            }
            else if (size == 3) {
                assertEquals(second, sliceBC.valueAt(0));
                assertEquals(third, sliceBC.valueAt(1));
            }

            final IntTransformable sliceABC = set.slice(new ImmutableIntRange(0, 2));
            assertEquals(size, sliceABC.size());
            assertEquals(first, sliceABC.valueAt(0));
            if (size >= 2) {
                assertEquals(second, sliceABC.valueAt(1));
                if (size >= 3) {
                    assertEquals(third, sliceABC.valueAt(2));
                }
            }

            final IntTransformable sliceABCD = set.slice(new ImmutableIntRange(0, 3));
            assertEquals(size, sliceABCD.size());
            assertEquals(first, sliceABCD.valueAt(0));
            if (size >= 2) {
                assertEquals(second, sliceABCD.valueAt(1));
                if (size >= 3) {
                    assertEquals(third, sliceABCD.valueAt(2));
                }
            }
        }))));
    }

    @Test
    default void testSkipWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntTransformable transformable = supplier.newBuilder().build();
            assertSame(transformable, transformable.skip(0));
            assertTrue(transformable.skip(1).isEmpty());
            assertTrue(transformable.skip(20).isEmpty());
        });
    }

    @Test
    default void testSkip() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntTransformable transformable = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = transformable.size();
            final int second = (size >= 2)? transformable.valueAt(1) : 0;
            final int third = (size == 3)? transformable.valueAt(2) : 0;

            assertSame(transformable, transformable.skip(0));

            final IntTransformable skip1 = transformable.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertEquals(second, skip1.valueAt(0));
                if (size == 3) {
                    assertEquals(third, skip1.valueAt(1));
                }
            }

            final IntTransformable skip2 = transformable.skip(2);
            if (size == 3) {
                assertEquals(third, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertEquals(0, skip2.size());
            }

            assertTrue(transformable.skip(3).isEmpty());
            assertTrue(transformable.skip(4).isEmpty());
            assertTrue(transformable.skip(24).isEmpty());
        }))));
    }

    @Test
    default void testTakeWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntTransformable transformable = supplier.newBuilder().build();
            assertTrue(transformable.take(0).isEmpty());
            assertTrue(transformable.take(1).isEmpty());
            assertTrue(transformable.take(2).isEmpty());
            assertTrue(transformable.take(24).isEmpty());
        });
    }

    @Test
    default void testTake() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntTransformable transformable = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = transformable.size();
            final int first = transformable.valueAt(0);

            assertTrue(transformable.take(0).isEmpty());

            final IntTransformable take1 = transformable.take(1);
            assertEquals(1, take1.size());
            assertEquals(first, take1.valueAt(0));

            final IntTransformable take2 = transformable.take(2);
            if (size == 1) {
                assertEquals(1, take2.size());
            }
            else {
                assertEquals(2, take2.size());
                assertEquals(transformable.valueAt(1), take2.valueAt(1));
            }
            assertEquals(first, take2.valueAt(0));

            final IntTransformable take3 = transformable.take(3);
            assertEquals(size, take3.size());
            assertEquals(first, take3.valueAt(0));
            if (size >= 2) {
                assertEquals(transformable.valueAt(1), take3.valueAt(1));
                if (size == 3) {
                    assertEquals(transformable.valueAt(2), take3.valueAt(2));
                }
            }

            final IntTransformable take4 = transformable.take(4);
            assertEquals(size, take4.size());
            assertEquals(first, take4.valueAt(0));
            if (size >= 2) {
                assertEquals(transformable.valueAt(1), take4.valueAt(1));
                if (size == 3) {
                    assertEquals(transformable.valueAt(2), take4.valueAt(2));
                }
            }
        }))));
    }

    @Test
    default void testSkipLastWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntTransformable transformable = supplier.newBuilder().build();
            assertSame(transformable, transformable.skipLast(0));
            assertTrue(transformable.skipLast(1).isEmpty());
            assertTrue(transformable.skipLast(2).isEmpty());
            assertTrue(transformable.skipLast(24).isEmpty());
        });
    }

    @Test
    default void testSkipLast() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntTransformable transformable = supplier.newBuilder().add(a).add(b).add(c).build();
            assertSame(transformable, transformable.skipLast(0));

            final int size = transformable.size();
            final int first = transformable.valueAt(0);
            final int second = (size >= 2)? transformable.valueAt(1) : 0;

            final IntTransformable transformable1 = transformable.skipLast(1);
            assertEquals(size - 1, transformable1.size());
            if (size >= 2) {
                assertEquals(first, transformable1.valueAt(0));
                if (size == 3) {
                    assertEquals(second, transformable1.valueAt(1));
                }
            }

            final IntTransformable transformable2 = transformable.skipLast(2);
            if (size < 3) {
                assertTrue(transformable2.isEmpty());
            }
            else {
                assertEquals(1, transformable2.size());
                assertEquals(first, transformable2.valueAt(0));
            }

            assertTrue(transformable.skipLast(3).isEmpty());
            assertTrue(transformable.skipLast(4).isEmpty());
            assertTrue(transformable.skipLast(24).isEmpty());
        }))));
    }

    @Test
    default void testTakeLastWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntTransformable transformable = supplier.newBuilder().build();
            assertSame(transformable, transformable.takeLast(0));
            assertTrue(transformable.takeLast(1).isEmpty());
            assertTrue(transformable.takeLast(2).isEmpty());
            assertTrue(transformable.takeLast(24).isEmpty());
        });
    }

    @Test
    default void testTakeLast() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntTransformable transformable = supplier.newBuilder().add(a).add(b).add(c).build();
            assertTrue(transformable.takeLast(0).isEmpty());

            final int size = transformable.size();
            final int first = transformable.valueAt(0);
            final int second = (size >= 2)? transformable.valueAt(1) : 0;
            final int third = (size >= 3)? transformable.valueAt(2) : 0;

            final IntTransformable take1 = transformable.takeLast(1);
            assertEquals(1, take1.size());
            assertEquals((size == 1)? first : (size == 2)? second : third, take1.valueAt(0));

            final IntTransformable take2 = transformable.takeLast(2);
            assertEquals(Math.min(size, 2), take2.size());
            if (size <= 2) {
                assertEquals(first, take2.valueAt(0));
                if (size == 2) {
                    assertEquals(second, take2.valueAt(1));
                }
            }
            else {
                assertEquals(second, take2.valueAt(0));
                assertEquals(third, take2.valueAt(1));
            }

            final IntTransformable take3 = transformable.takeLast(3);
            assertEquals(size, take3.size());
            assertEquals(first, take3.valueAt(0));
            if (size >= 2) {
                assertEquals(second, take3.valueAt(1));
                if (size == 3) {
                    assertEquals(third, take3.valueAt(2));
                }
            }

            final IntTransformable take4 = transformable.takeLast(3);
            assertEquals(size, take4.size());
            assertEquals(first, take4.valueAt(0));
            if (size >= 2) {
                assertEquals(second, take4.valueAt(1));
                if (size == 3) {
                    assertEquals(third, take4.valueAt(2));
                }
            }
        }))));
    }
}

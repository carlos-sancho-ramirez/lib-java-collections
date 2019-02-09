package sword.collections;

import java.util.Iterator;

import static sword.collections.TestUtils.withInt;

public final class MutableIntSetTest extends AbstractIntTransformableTest {

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE, -500, -2, -1, 0, 1, 3, 127, 128, Integer.MAX_VALUE
    };

    @Override
    MutableIntSet.Builder newIntBuilder() {
        return new MutableIntSet.Builder();
    }

    @Override
    void assertEmptyCollection(IntTransformable transformable) {
        assertFalse(transformable.iterator().hasNext());
    }

    @Override
    void assertNotChanged(Object expected, Object given) {
        assertEquals(expected, given);
    }

    @Override
    void withItem(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    @Override
    MutableIntSet emptyCollection() {
        return MutableIntSet.empty();
    }

    private boolean isPositiveValue(int value) {
        return value >= 0;
    }

    @Override
    void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::isPositiveValue);
    }

    private void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    private void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v * v);
        procedure.apply(v -> v + 1);
    }

    public void testSizeForMultipleElements() {
        withItem(a -> withItem(b -> {
            final MutableIntSet set = newIntBuilder().add(a).add(b).build();
            if (a == b) {
                assertEquals("Expected size 1 after building it adding twice value " + a, 1, set.size());
            }
            else {
                assertEquals("Expected size 2 after building it adding two different values " + a + " and " + b, 2, set.size());
            }
        }));
    }

    public void testIteratingForMultipleElements() {
        withItem(a -> withItem(b -> {
            final MutableIntSet set = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> iterator = set.iterator();

            assertTrue(iterator.hasNext());
            final int first = iterator.next();

            if (a == b) {
                assertFalse(iterator.hasNext());
                assertEquals(a, first);
            }
            else {
                assertTrue(iterator.hasNext());
                final int second = iterator.next();

                assertFalse(iterator.hasNext());
                if (a < b) {
                    assertEquals(a, first);
                    assertEquals(b, second);
                }
                else {
                    assertEquals(a, second);
                    assertEquals(b, first);
                }
            }
        }));
    }

    public void testAdd() {
        withItem(a -> withItem(b -> {
            final MutableIntSet set = MutableIntSet.empty();
            assertTrue(set.add(a));
            assertFalse(set.isEmpty());

            if (a == b) {
                assertFalse(set.add(b));
                assertEquals(1, set.size());
                assertTrue(set.contains(b));
            }
            else {
                assertTrue(set.add(b));
                assertEquals(2, set.size());
                assertTrue(set.contains(a));
                assertTrue(set.contains(b));
            }
        }));
    }

    public void testAddAll() {
        withItem(a -> withItem(b -> {
            final ImmutableIntSet values = new ImmutableIntSetBuilder().add(a).add(b).build();
            withItem(c -> {
                final MutableIntSet set = MutableIntSet.empty();
                set.add(c);

                if (c == a && c == b) {
                    assertFalse(set.addAll(values));
                    assertEquals(1, set.size());
                    assertTrue(set.contains(c));
                }
                else {
                    assertTrue(set.addAll(values));
                    assertTrue(set.contains(a));
                    assertTrue(set.contains(b));
                    assertTrue(set.contains(c));
                    if (a == b || a == c || b == c) {
                        assertEquals(2, set.size());
                    }
                    else {
                        assertEquals(3, set.size());
                    }
                }
            });
        }));
    }

    public void testRemoveForEmptySet() {
        final MutableIntSet set = newIntBuilder().build();
        withItem(value -> {
            assertFalse(set.remove(value));
            assertTrue(set.isEmpty());
        });
    }

    public void testRemoveForASingleElement() {
        withItem(included -> {
            withItem(value -> {
                final MutableIntSet set = newIntBuilder().add(included).build();
                if (included == value) {
                    assertTrue(set.remove(value));
                    assertTrue(set.isEmpty());
                }
                else {
                    assertFalse(set.remove(value));
                    assertFalse(set.isEmpty());
                }
            });
        });
    }

    public void testValueAt() {
        withItem(a -> withItem(b -> withItem(c -> {
            final MutableIntSet set = newIntBuilder().add(a).add(b).add(c).build();
            final Iterator<Integer> it = set.iterator();
            int index = 0;
            while (it.hasNext()) {
                assertEquals(set.valueAt(index++), it.next().intValue());
            }
        })));
    }

    public void testToImmutableMethodReturnSameInstance() {
        withItem(a -> withItem(b -> {
            final MutableIntSet set = new MutableIntSet.Builder().add(a).add(b).build();
            final ImmutableIntSet set2 = set.toImmutable();
            assertEquals(set.size(), set2.size());
            for (int value : set) {
                assertTrue(set2.contains(value));
            }
        }));
    }

    public void testMutate() {
        withItem(a -> withItem(b -> {
            final MutableIntSet set = new MutableIntSet.Builder().add(a).add(b).build();
            withItem(c -> {
                final MutableIntSet set2 = set.mutate();
                assertNotSame(set, set2);
                set2.add(c);
                if (a == c || b == c) {
                    assertEquals(set.size(), set2.size());
                }
                else {
                    assertEquals(set.size() + 1, set2.size());
                    assertTrue(set2.contains(c));
                    assertFalse(set.contains(c));
                }

                for (int value : set) {
                    assertTrue(set2.contains(value));
                }
            });
        }));
    }

    @Override
    public void testFindFirstForMultipleElements() {
        withFilterFunc(f -> withItem(defaultValue -> withItem(a -> withItem(b -> {
            final IterableIntCollection collection = newIntBuilder().add(a).add(b).build();
            final boolean reversed = b < a;
            final int first = collection.findFirst(f, defaultValue);

            if (f.apply(a) && (!reversed || !f.apply(b))) {
                assertEquals(a, first);
            }
            else if (f.apply(b) && (reversed || !f.apply(a))) {
                assertEquals(b, first);
            }
            else {
                assertEquals(defaultValue, first);
            }
        }))));
    }

    public void testMapWhenEmpty() {
        final IntFunction func = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertSame(ImmutableList.empty(), newIntBuilder().build().map(func));
    }

    public void testMapForSingleElement() {
        withMapFunc(f -> withItem(value -> {
            final MutableIntSet set = newIntBuilder().add(value).build();
            final Iterator<String> iterator = set.map(f).iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next());
            assertFalse(iterator.hasNext());
        }));
    }

    public void testMapForMultipleElements() {
        withMapFunc(f -> withItem(a -> withItem(b -> {
            final MutableIntSet set = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> iterator = set.iterator();
            final Iterator<String> mappedIterator = set.map(f).iterator();
            while (iterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(iterator.next()), mappedIterator.next());
            }

            assertFalse(mappedIterator.hasNext());
        })));
    }

    public void testMapToIntWhenEmpty() {
        final IntToIntFunction func = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertSame(ImmutableIntList.empty(), newIntBuilder().build().mapToInt(func));
    }

    public void testMapToIntForSingleElement() {
        withMapToIntFunc(f -> withItem(value -> {
            final Iterator<Integer> iterator = newIntBuilder().add(value).build().mapToInt(f).iterator();
            assertTrue(iterator.hasNext());
            assertEquals(f.apply(value), iterator.next().intValue());
            assertFalse(iterator.hasNext());
        }));
    }

    public void testMapToIntForMultipleElements() {
        withMapToIntFunc(f -> withItem(a -> withItem(b -> {
            final MutableIntSet set = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> iterator = set.iterator();
            final Iterator<Integer> mappedIterator = set.mapToInt(f).iterator();
            while (iterator.hasNext()) {
                assertTrue(mappedIterator.hasNext());
                assertEquals(f.apply(iterator.next()), mappedIterator.next().intValue());
            }

            assertFalse(mappedIterator.hasNext());
        })));
    }

    public void testToListWhenEmpty() {
        final IntSet set = newIntBuilder().build();
        assertTrue(set.isEmpty());
        assertTrue(set.toList().isEmpty());
    }

    public void testToList() {
        withItem(a -> withItem(b -> {
            final IntSet set = newIntBuilder().add(a).add(b).build();
            final IntList list = set.toList();

            if (a == b) {
                assertEquals(1, list.size());
                assertEquals(a, list.get(0));
            }
            else {
                assertEquals(2, list.size());

                if (a < b) {
                    assertEquals(a, list.get(0));
                    assertEquals(b, list.get(1));
                }
                else {
                    assertEquals(a, list.get(1));
                    assertEquals(b, list.get(0));
                }
            }
        }));
    }

    public void testClearWhenEmpty() {
        final MutableIntSet collection = newIntBuilder().build();
        assertFalse(collection.clear());
        assertTrue(collection.isEmpty());
    }

    public void testClearForSingleItem() {
        withInt(value -> {
            final MutableIntSet collection = newIntBuilder().add(value).build();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        });
    }

    public void testClearForMultipleItems() {
        withInt(a -> withInt(b -> {
            final MutableIntSet collection = newIntBuilder().add(a).add(b).build();
            assertTrue(collection.clear());
            assertTrue(collection.isEmpty());
        }));
    }
}

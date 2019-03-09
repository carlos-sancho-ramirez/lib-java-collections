package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static sword.collections.TestUtils.withInt;

public final class MutableIntListTest extends IntTransformableTest implements MutableIntTraversableTest<MutableIntList> {

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE, -1023, -2, -1, 0, 1, 2, 7, 108, Integer.MAX_VALUE
    };

    @Override
    void withItem(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    @Override
    void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v * v);
        procedure.apply(v -> v + 1);
    }

    private boolean isPositiveValue(int value) {
        return value >= 0;
    }

    @Override
    void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::isPositiveValue);
    }

    @Override
    MutableIntList.Builder newIntBuilder() {
        return new MutableIntList.Builder();
    }

    @Override
    public void withIntTraversableBuilderSupplier(Procedure<IntBuilderSupplier<MutableIntList, MutableIntTraversableBuilder<MutableIntList>>> procedure) {
        procedure.apply(MutableIntList.Builder::new);
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withItem(procedure);
    }

    @Test
    public void testIteratingForMultipleElements() {
        withItem(a -> withItem(b -> {
            final MutableIntList list = newIntBuilder().add(a).add(b).build();
            final Iterator<Integer> iterator = list.iterator();

            assertTrue(iterator.hasNext());
            assertEquals(a, iterator.next().intValue());

            assertTrue(iterator.hasNext());
            assertEquals(b, iterator.next().intValue());

            assertFalse(iterator.hasNext());
        }));
    }

    @Test
    public void testAppendWhenEmpty() {
        withItem(value -> {
            final ImmutableIntList empty = ImmutableIntList.empty();
            final ImmutableIntList list = empty.append(value);
            assertNotSame(empty, list);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    @Test
    public void testAppendForASingleElement() {
        withItem(a -> withItem(value -> {
            final ImmutableIntList initList = new ImmutableIntList.Builder().append(a).build();
            final ImmutableIntList list = initList.append(value);
            assertEquals(2, list.size());
            assertEquals(a, list.get(0));
            assertEquals(value, list.get(1));
        }));
    }

    @Test
    public void testAppendAllWhenBothEmpty() {
        final ImmutableList<String> empty = ImmutableList.empty();
        final ImmutableList<String> result = empty.appendAll(empty);
        assertSame(empty, result);
    }

    @Test
    public void testAppendANonEmptyListWhenEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        withItem(value -> {
            final ImmutableIntList list = new ImmutableIntList.Builder().append(value).build();
            final ImmutableIntList result = empty.appendAll(list);
            assertSame(list, result);
        });
    }

    @Test
    public void testAppendAnEmptyListWhenNoEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        withItem(value -> {
            final ImmutableIntList list = new ImmutableIntList.Builder().append(value).build();
            final ImmutableIntList result = list.appendAll(empty);
            assertSame(list, result);
        });
    }

    @Test
    public void testAppendAll() {
        withItem(a -> withItem(b -> withItem(c -> {
            final ImmutableIntList list1 = new ImmutableIntList.Builder().append(a).append(b).build();
            final ImmutableIntList list2 = new ImmutableIntList.Builder().append(c).build();

            final ImmutableIntList result12 = list1.appendAll(list2);
            assertEquals(3, result12.size());
            assertEquals(a, result12.get(0));
            assertEquals(b, result12.get(1));
            assertEquals(c, result12.get(2));

            final ImmutableIntList result21 = list2.appendAll(list1);
            assertEquals(3, result21.size());
            assertEquals(c, result21.get(0));
            assertEquals(a, result21.get(1));
            assertEquals(b, result21.get(2));
        })));
    }

    @Test
    public void testToImmutableForEmpty() {
        assertTrue(newIntBuilder().build().toImmutable().isEmpty());
    }

    @Test
    public void testToImmutable() {
        withInt(a -> withInt(b -> {
            final MutableIntList list1 = newIntBuilder().add(a).add(b).build();
            final ImmutableIntList list2 = list1.toImmutable();

            final Iterator<Integer> it1 = list1.iterator();
            final Iterator<Integer> it2 = list2.iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                final Integer item1 = it1.next();
                final Integer item2 = it2.next();
                assertEquals(item1, item2);
            }
            assertFalse(it2.hasNext());
        }));
    }

    @Test
    public void testHashCode() {
        withInt(a -> withInt(b -> withInt(c -> {
            final MutableIntList mutable = newIntBuilder()
                    .add(a)
                    .add(b)
                    .add(c)
                    .build();
            final ImmutableIntList immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable.hashCode(), immutable.hashCode());
        })));
    }

    @Test
    public void testEquals() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntList mutable = newIntBuilder()
                    .add(a)
                    .add(b)
                    .add(c)
                    .build();
            final IntList immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable, immutable);
            assertEquals(immutable, mutable);
        })));
    }

    @Test
    public void testRemoveAtForSingleElementList() {
        withInt(a -> {
            final MutableIntList list = newIntBuilder().add(a).build();
            list.removeAt(0);
            assertTrue(list.isEmpty());
        });
    }

    @Test
    public void testRemoveFirstFor2ElementsList() {
        withInt(a -> withInt(b -> {
            final MutableIntList list = newIntBuilder().add(a).add(b).build();
            list.removeAt(0);
            assertEquals(1, list.size());
            assertEquals(b, list.get(0));
        }));
    }

    @Test
    public void testRemoveLastFor2ElementsList() {
        withInt(a -> withInt(b -> {
            final MutableIntList list = newIntBuilder().add(a).add(b).build();
            list.removeAt(1);
            assertEquals(1, list.size());
            assertEquals(a, list.get(0));
        }));
    }

    @Test
    public void testRemoveAt() {
        withInt(a -> withInt(b -> {
            for (int i = 0; i <= 4; i++) {
                MutableIntList list = newIntBuilder().add(a).add(b).add(a).add(b).add(a).build();

                MutableIntList.Builder builder = newIntBuilder();
                for (int j = 0; j <= 4; j++) {
                    if (j != i) {
                        builder.add(list.get(j));
                    }
                }

                list.removeAt(i);
                assertEquals(builder.build(), list);
            }
        }));
    }

    @Test
    public void testToSetWhenEmpty() {
        final IntList list = newIntBuilder().build();
        assertTrue(list.toSet().isEmpty());
    }

    @Test
    public void testToSetWithSingleElement() {
        withItem(a -> {
            final IntList list = newIntBuilder().add(a).build();
            final IntSet set = list.toSet();
            assertEquals(1, set.size());
            assertEquals(a, set.valueAt(0));
        });
    }

    @Test
    public void testToSetWithTwoElements() {
        withItem(a -> withItem(b -> {
            final IntList list = newIntBuilder().add(a).add(b).build();
            final IntSet set = list.toSet();
            if (a == b) {
                assertEquals(1, set.size());
                assertEquals(a, set.valueAt(0));
            }
            else {
                assertEquals(2, set.size());
                if (a < b) {
                    assertEquals(a, set.valueAt(0));
                    assertEquals(b, set.valueAt(1));
                }
                else {
                    assertEquals(a, set.valueAt(1));
                    assertEquals(b, set.valueAt(0));
                }
            }
        }));
    }
}

package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withInt;

public final class MutableIntListTest extends IntListTest<MutableIntList.Builder> implements MutableIntTraversableTest<MutableIntList.Builder> {

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE, -1023, -2, -1, 0, 1, 2, 7, 108, Integer.MAX_VALUE
    };

    @Override
    public void withValue(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    public void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    @Override
    public void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v * v);
        procedure.apply(v -> v + 1);
    }

    private boolean isPositiveValue(int value) {
        return value >= 0;
    }

    @Override
    public void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::isPositiveValue);
    }

    @Override
    public MutableIntList.Builder newIntBuilder() {
        return new MutableIntList.Builder();
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<MutableIntList.Builder>> procedure) {
        procedure.apply(MutableIntList.Builder::new);
    }

    @Test
    void testIteratingForMultipleElements() {
        withValue(a -> withValue(b -> {
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
    void testInsertForEmptyList() {
        withValue(a -> {
            final MutableIntList list = MutableIntList.empty();
            list.insert(0, a);
            assertEquals(1, list.size());
            assertEquals(a, list.get(0));
        });
    }

    @Test
    void testInsertFirstForSingleElementList() {
        withValue(a -> withValue(b -> {
            final MutableIntList list = new MutableIntList.Builder().add(a).build();
            list.insert(0, b);
            assertEquals(2, list.size());
            assertEquals(b, list.get(0));
            assertEquals(a, list.get(1));
        }));
    }

    @Test
    void testInsertLastForSingleElementList() {
        withValue(a -> withValue(b -> {
            final MutableIntList list = new MutableIntList.Builder().add(a).build();
            list.insert(1, b);
            assertEquals(2, list.size());
            assertEquals(a, list.get(0));
            assertEquals(b, list.get(1));
        }));
    }

    @Test
    void testInsert() {
        withValue(a -> withValue(b -> withValue(c -> {
            for (int i = 0; i <= 4; i++) {
                final MutableIntList list = new MutableIntList.Builder().add(a).add(b).add(b).add(a).build();

                final MutableIntList.Builder builder = new MutableIntList.Builder();
                for (int j = 0; j < 4; j++) {
                    if (j == i) {
                        builder.add(c);
                    }
                    builder.add(list.get(j));
                }

                if (i == 4) {
                    builder.add(c);
                }

                list.insert(i, c);
                assertEquals(builder.build(), list);
            }
        })));
    }

    @Test
    void testPrependWhenEmpty() {
        withValue(value -> {
            final MutableIntList list = newIntBuilder().build();
            list.prepend(value);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    @Test
    void testPrependForASingleElement() {
        withValue(a -> withValue(value -> {
            final MutableIntList list = newIntBuilder().append(a).build();
            list.prepend(value);
            assertEquals(2, list.size());
            assertEquals(value, list.get(0));
            assertEquals(a, list.get(1));
        }));
    }

    @Test
    void testPrependForMultipleElements() {
        withValue(a -> withValue(b -> withValue(value -> {
            final MutableIntList list = newIntBuilder().append(a).append(b).build();
            list.prepend(value);
            assertEquals(3, list.size());
            assertEquals(value, list.get(0));
            assertEquals(a, list.get(1));
            assertEquals(b, list.get(2));
        })));
    }

    @Test
    void testAppendWhenEmpty() {
        withValue(value -> {
            final MutableIntList list = MutableIntList.empty();
            list.append(value);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    @Test
    void testAppendForASingleElement() {
        withValue(a -> withValue(value -> {
            final MutableIntList list = new MutableIntList.Builder().append(a).build();
            list.append(value);
            assertEquals(2, list.size());
            assertEquals(a, list.get(0));
            assertEquals(value, list.get(1));
        }));
    }

    @Test
    void testAppendANonEmptyListWhenEmpty() {
        withValue(value -> {
            final MutableIntList list = MutableIntList.empty();
            final ImmutableIntList arg = new ImmutableIntList.Builder().append(value).build();
            list.appendAll(arg);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    @Test
    void testAppendAnEmptyListWhenNoEmpty() {
        final ImmutableIntList empty = ImmutableIntList.empty();
        withValue(value -> {
            final MutableIntList list = new MutableIntList.Builder().append(value).build();
            list.appendAll(empty);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0));
        });
    }

    @Test
    void testAppendAllA() {
        withValue(a -> withValue(b -> withValue(c -> {
            final MutableIntList list1 = new MutableIntList.Builder().append(a).build();
            final MutableIntList list2 = new MutableIntList.Builder().append(b).append(c).build();

            list1.appendAll(list2);
            assertEquals(3, list1.size());
            assertEquals(a, list1.get(0));
            assertEquals(b, list1.get(1));
            assertEquals(c, list1.get(2));
        })));
    }

    @Test
    void testAppendAllB() {
        withValue(a -> withValue(b -> withValue(c -> {
            final MutableIntList list1 = new MutableIntList.Builder().append(a).append(b).build();
            final MutableIntList list2 = new MutableIntList.Builder().append(c).build();

            list1.appendAll(list2);
            assertEquals(3, list1.size());
            assertEquals(a, list1.get(0));
            assertEquals(b, list1.get(1));
            assertEquals(c, list1.get(2));
        })));
    }

    @Test
    void testPut() {
        withValue(a -> withValue(b -> withValue(c -> {
            for (int i = 0; i < 2; i++) {
                final MutableIntList list = new MutableIntList.Builder().add(a).add(b).build();
                final MutableIntList expectedList = new MutableIntList.Builder()
                        .add((i == 0) ? c : a)
                        .add((i == 1) ? c : b)
                        .build();
                if (i == 0 && a == c || i == 1 && b == c) {
                    assertFalse(list.put(i, c));
                }
                else {
                    assertTrue(list.put(i, c));
                }

                assertEquals(expectedList, list);
            }
        })));
    }

    @Test
    void testToImmutableForEmpty() {
        assertTrue(newIntBuilder().build().toImmutable().isEmpty());
    }

    @Test
    void testToImmutable() {
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
    void testHashCode() {
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
    void testEquals() {
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
    void testDonateWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final MutableIntList list = supplier.newBuilder().build();
            final MutableIntList list2 = list.donate();
            assertTrue(list.isEmpty());
            assertTrue(list2.isEmpty());
            assertNotSame(list, list2);
        });
    }

    @Test
    void testDonateForSingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final MutableIntList list = supplier.newBuilder().add(value).build();
            final MutableIntList list2 = list.donate();
            assertTrue(list.isEmpty());
            assertEquals(1, list2.size());
            assertEquals(value, list2.valueAt(0));
        }));
    }

    @Test
    void testDonateForSingleMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final MutableIntList list = supplier.newBuilder().add(a).add(b).build();
            final MutableIntList list2 = list.donate();
            assertTrue(list.isEmpty());

            assertEquals(2, list2.size());
            assertEquals(a, list2.valueAt(0));
            assertEquals(b, list2.valueAt(1));
        })));
    }
}

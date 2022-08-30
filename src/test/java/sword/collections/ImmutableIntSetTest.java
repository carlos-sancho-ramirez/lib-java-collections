package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class ImmutableIntSetTest extends IntSetTest<ImmutableIntSet.Builder> implements ImmutableIntTransformableTest<ImmutableIntSet.Builder> {

    @Override
    public abstract ImmutableIntSet.Builder newIntBuilder();

    private boolean isPositiveValue(int value) {
        return value >= 0;
    }

    @Override
    public void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::isPositiveValue);
    }

    @Override
    public void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    @Override
    public void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(v -> v * v);
        procedure.apply(v -> v + 1);
    }

    private int moduleFour(int value) {
        return value & 3;
    }

    private void withGroupingFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(value -> Integer.toString(moduleFour(value)));
    }

    private void withGroupingIntFunc(Procedure<IntToIntFunction> procedure) {
        procedure.apply(this::moduleFour);
    }

    @Test
    void testIteratingForMultipleElements() {
        withValue(a -> withValue(b -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).build();
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

    @Test
    void testAdd() {
        withValue(a -> withValue(b -> {
            ImmutableIntSet set = newIntBuilder().build();
            set = set.add(a);
            assertFalse(set.isEmpty());

            if (a == b) {
                assertSame(set, set.add(b));
                assertTrue(set.contains(b));
            }
            else {
                set = set.add(b);
                assertEquals(2, set.size());
                assertTrue(set.contains(a));
                assertTrue(set.contains(b));
            }
        }));
    }

    @Test
    void testAddAllForBothEmpty() {
        ImmutableIntSet set = newIntBuilder().build();
        assertSame(set, set.addAll(set));
    }

    @Test
    void testAddAllForEmptyGiven() {
        final ImmutableIntSet empty = newIntBuilder().build();
        withValue(a -> {
            final ImmutableIntSet set = newIntBuilder().add(a).build();
            assertSame(set, set.addAll(empty));
        });
    }

    @Test
    void testAddAll() {
        withValue(a -> withValue(b -> withValue(c -> withValue(d -> {
            final ImmutableIntSet set1 = newIntBuilder().add(a).add(b).build();
            final ImmutableIntSet set2 = newIntBuilder().add(c).add(d).build();

            final int expectedSize = (a == b && a == c && a == d)? 1 :
                    (a == b && a == c || a == b && a == d || a == c && a == d || b == c && b == d)? 2 :
                            (a == b && c == d || a == c && b == d || a == d && b == c)? 2 :
                            (a == b || a == c || a == d || b == c || b == d || c == d)? 3 : 4;
            final ImmutableIntSet result = set1.addAll(set2);
            assertEquals(expectedSize, result.size());
            assertTrue(result.contains(a));
            assertTrue(result.contains(b));
            assertTrue(result.contains(c));
            assertTrue(result.contains(d));
        }))));
    }

    @Test
    void testRemoveForEmptySet() {
        final ImmutableIntSet set = newIntBuilder().build();
        withValue(value -> assertSame(set, set.remove(value), "Removing on an empty set should always return the same set"));
    }

    @Test
    void testRemoveForASingleElement() {
        withValue(included -> {
            final ImmutableIntSet set = newIntBuilder().add(included).build();
            withValue(value -> {
                if (included == value) {
                    final ImmutableIntSet emptySet = set.remove(value);
                    final String msg = "Removing value " + value + " from set containing only that value should return an empty set";
                    assertNotSame(set, emptySet, msg);
                    assertTrue(emptySet.isEmpty(), msg);
                }
                else {
                    assertSame(set, set.remove(value), "Removing an element that is not included in the set should always return the same set");
                }
            });
        });
    }

    @Test
    void testValueAt() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).add(c).build();
            final Iterator<Integer> it = set.iterator();
            int index = 0;
            while (it.hasNext()) {
                assertEquals(set.valueAt(index++), it.next().intValue());
            }
        })));
    }

    @Test
    void testToImmutableMethodReturnSameInstance() {
        withValue(a -> withValue(b -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).build();
            assertSame(set, set.toImmutable());
        }));
    }

    @Test
    void testGroupByWhenEmpty() {
        final IntFunction<Integer> func = str -> {
            throw new AssertionError("This function should not be executed");
        };
        final ImmutableIntSet list = newIntBuilder().build();
        assertTrue(list.groupBy(func).isEmpty());
    }

    @Test
    void testGroupBy() {
        withGroupingFunc(func -> withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).add(c).build();
            final String aGroup = func.apply(a);
            final String bGroup = func.apply(b);
            final String cGroup = func.apply(c);

            final ImmutableMap<String, ImmutableIntSet> map = set.groupBy(func);
            if (aGroup.equals(bGroup)) {
                if (aGroup.equals(cGroup)) {
                    assertEquals(1, map.size());
                    assertEquals(aGroup, map.keyAt(0));
                    assertSame(set, map.valueAt(0));
                }
                else {
                    assertEquals(2, map.size());
                    if (aGroup.equals(map.keyAt(0))) {
                        assertEquals(cGroup, map.keyAt(1));
                        assertTrue(newIntBuilder().add(a).add(b).build().equalSet(map.valueAt(0)));
                        assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(1)));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(0));
                        assertEquals(aGroup, map.keyAt(1));
                        assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(0)));
                        assertTrue(newIntBuilder().add(a).add(b).build().equalSet(map.valueAt(1)));
                    }
                }
            }
            else if (aGroup.equals(cGroup)) {
                assertEquals(2, map.size());
                if (aGroup.equals(map.keyAt(0))) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertTrue(newIntBuilder().add(a).add(c).build().equalSet(map.valueAt(0)));
                    assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(1)));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(0)));
                    assertTrue(newIntBuilder().add(a).add(c).build().equalSet(map.valueAt(1)));
                }
            }
            else if (bGroup.equals(cGroup)) {
                assertEquals(2, map.size());
                if (aGroup.equals(map.keyAt(0))) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(0)));
                    assertTrue(newIntBuilder().add(b).add(c).build().equalSet(map.valueAt(1)));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertTrue(newIntBuilder().add(b).add(c).build().equalSet(map.valueAt(0)));
                    assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(1)));
                }
            }
            else {
                assertEquals(3, map.size());
                if (aGroup.equals(map.keyAt(0))) {
                    assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(0)));
                    if (bGroup.equals(map.keyAt(1))) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(1)));
                        assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(2)));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(bGroup, map.keyAt(2));
                        assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(1)));
                        assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(2)));
                    }
                }
                else if (bGroup.equals(map.keyAt(0))) {
                    assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(0)));
                    if (aGroup.equals(map.keyAt(1))) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(1)));
                        assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(2)));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(1)));
                        assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(2)));
                    }
                }
                else {
                    assertEquals(cGroup, map.keyAt(0));
                    assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(0)));
                    if (aGroup.equals(map.keyAt(1))) {
                        assertEquals(bGroup, map.keyAt(2));
                        assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(1)));
                        assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(2)));
                    }
                    else {
                        assertEquals(bGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(1)));
                        assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(2)));
                    }
                }
            }
        }))));
    }

    @Test
    void testGroupByIntWhenEmpty() {
        final IntToIntFunction func = str -> {
            throw new AssertionError("This function should not be executed");
        };
        final ImmutableIntSet list = newIntBuilder().build();
        assertTrue(list.groupByInt(func).isEmpty());
    }

    @Test
    void testGroupByInt() {
        withGroupingIntFunc(func -> withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).add(c).build();
            final int aGroup = func.apply(a);
            final int bGroup = func.apply(b);
            final int cGroup = func.apply(c);

            final ImmutableIntKeyMap<ImmutableIntSet> map = set.groupByInt(func);
            if (aGroup == bGroup) {
                if (aGroup == cGroup) {
                    assertEquals(1, map.size());
                    assertEquals(aGroup, map.keyAt(0));
                    assertSame(set, map.valueAt(0));
                }
                else {
                    assertEquals(2, map.size());
                    if (aGroup == map.keyAt(0)) {
                        assertEquals(cGroup, map.keyAt(1));
                        assertTrue(newIntBuilder().add(a).add(b).build().equalSet(map.valueAt(0)));
                        assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(1)));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(0));
                        assertEquals(aGroup, map.keyAt(1));
                        assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(0)));
                        assertTrue(newIntBuilder().add(a).add(b).build().equalSet(map.valueAt(1)));
                    }
                }
            }
            else if (aGroup == cGroup) {
                assertEquals(2, map.size());
                if (aGroup == map.keyAt(0)) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertTrue(newIntBuilder().add(a).add(c).build().equalSet(map.valueAt(0)));
                    assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(1)));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(0)));
                    assertTrue(newIntBuilder().add(a).add(c).build().equalSet(map.valueAt(1)));
                }
            }
            else if (bGroup == cGroup) {
                assertEquals(2, map.size());
                if (aGroup == map.keyAt(0)) {
                    assertEquals(bGroup, map.keyAt(1));
                    assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(0)));
                    assertTrue(newIntBuilder().add(b).add(c).build().equalSet(map.valueAt(1)));
                }
                else {
                    assertEquals(bGroup, map.keyAt(0));
                    assertEquals(aGroup, map.keyAt(1));
                    assertTrue(newIntBuilder().add(b).add(c).build().equalSet(map.valueAt(0)));
                    assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(1)));
                }
            }
            else {
                assertEquals(3, map.size());
                if (aGroup == map.keyAt(0)) {
                    assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(0)));
                    if (bGroup == map.keyAt(1)) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(1)));
                        assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(2)));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(bGroup, map.keyAt(2));
                        assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(1)));
                        assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(2)));
                    }
                }
                else if (bGroup == map.keyAt(0)) {
                    assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(0)));
                    if (aGroup == map.keyAt(1)) {
                        assertEquals(cGroup, map.keyAt(2));
                        assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(1)));
                        assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(2)));
                    }
                    else {
                        assertEquals(cGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(1)));
                        assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(2)));
                    }
                }
                else {
                    assertEquals(cGroup, map.keyAt(0));
                    assertTrue(newIntBuilder().add(c).build().equalSet(map.valueAt(0)));
                    if (aGroup == map.keyAt(1)) {
                        assertEquals(bGroup, map.keyAt(2));
                        assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(1)));
                        assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(2)));
                    }
                    else {
                        assertEquals(bGroup, map.keyAt(1));
                        assertEquals(aGroup, map.keyAt(2));
                        assertTrue(newIntBuilder().add(b).build().equalSet(map.valueAt(1)));
                        assertTrue(newIntBuilder().add(a).build().equalSet(map.valueAt(2)));
                    }
                }
            }
        }))));
    }

    @Test
    void testSliceWhenEmpty() {
        final ImmutableIntSet set = newIntBuilder().build();
        assertSame(set, set.slice(new ImmutableIntRange(0, 0)));
        assertSame(set, set.slice(new ImmutableIntRange(1, 1)));
        assertSame(set, set.slice(new ImmutableIntRange(2, 2)));
        assertSame(set, set.slice(new ImmutableIntRange(0, 1)));
        assertSame(set, set.slice(new ImmutableIntRange(1, 2)));
        assertSame(set, set.slice(new ImmutableIntRange(0, 2)));
    }

    @Test
    public void testSlice() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final int first = set.valueAt(0);
            final int second = (size >= 2)? set.valueAt(1) : 0;
            final int third = (size >= 3)? set.valueAt(2) : 0;

            final ImmutableIntSet sliceA = set.slice(new ImmutableIntRange(0, 0));
            if (size == 1) {
                assertSame(set, sliceA);
            }
            else {
                assertEquals(1, sliceA.size());
                assertEquals(first, sliceA.valueAt(0));
            }

            final ImmutableIntSet sliceB = set.slice(new ImmutableIntRange(1, 1));
            if (size >= 2) {
                assertEquals(1, sliceB.size());
                assertEquals(second, sliceB.valueAt(0));
            }
            else {
                assertTrue(sliceB.isEmpty());
            }

            final ImmutableIntSet sliceC = set.slice(new ImmutableIntRange(2, 2));
            if (size >= 3) {
                assertEquals(1, sliceC.size());
                assertEquals(third, sliceC.valueAt(0));
            }
            else {
                assertTrue(sliceC.isEmpty());
            }

            final ImmutableIntSet sliceAB = set.slice(new ImmutableIntRange(0, 1));
            if (size == 1) {
                assertEquals(1, sliceAB.size());
            }
            else if (size == 2) {
                assertSame(set, sliceAB);
            }
            else {
                assertEquals(2, sliceAB.size());
                assertEquals(second, sliceAB.valueAt(1));
            }
            assertEquals(first, sliceAB.valueAt(0));

            final ImmutableIntSet sliceBC = set.slice(new ImmutableIntRange(1, 2));
            if (size == 1) {
                assertTrue(sliceBC.isEmpty());
            }
            else if (size == 2) {
                assertEquals(1, sliceBC.size());
                assertEquals(second, sliceBC.valueAt(0));
            }
            else {
                assertEquals(2, sliceBC.size());
                assertEquals(second, sliceBC.valueAt(0));
                assertEquals(third, sliceBC.valueAt(1));
            }

            assertSame(set, set.slice(new ImmutableIntRange(0, 2)));
            assertSame(set, set.slice(new ImmutableIntRange(0, 3)));
        })));
    }

    @Test
    @Override
    public void testSkipWhenEmpty() {
        final ImmutableIntSet set = newIntBuilder().build();
        assertSame(set, set.skip(0));
        assertTrue(set.skip(1).isEmpty());
        assertTrue(set.skip(20).isEmpty());
    }

    @Test
    @Override
    public void testSkip() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final int second = (size >= 2)? set.valueAt(1) : 0;
            final int third = (size == 3)? set.valueAt(2) : 0;

            assertSame(set, set.skip(0));

            final ImmutableIntSet skip1 = set.skip(1);
            assertEquals(size - 1, skip1.size());
            if (size >= 2) {
                assertEquals(second, skip1.valueAt(0));
                if (size == 3) {
                    assertEquals(third, skip1.valueAt(1));
                }
            }

            final ImmutableIntSet skip2 = set.skip(2);
            if (size == 3) {
                assertEquals(third, skip2.valueAt(0));
                assertEquals(1, skip2.size());
            }
            else {
                assertTrue(skip2.isEmpty());
            }

            assertTrue(set.skip(3).isEmpty());
            assertTrue(set.skip(4).isEmpty());
            assertTrue(set.skip(24).isEmpty());
        })));
    }

    @Test
    @Override
    public void testTakeWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final ImmutableIntSet set = supplier.newBuilder().build();
            assertSame(set, set.take(0));
            assertSame(set, set.take(1));
            assertSame(set, set.take(2));
            assertSame(set, set.take(24));
        });
    }

    @Test
    @Override
    public void testTake() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final ImmutableIntSet set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();
            final int first = set.valueAt(0);

            assertTrue(set.take(0).isEmpty());

            final ImmutableIntSet take1 = set.take(1);
            if (size > 1) {
                assertEquals(1, take1.size());
                assertEquals(first, take1.valueAt(0));
            }
            else {
                assertSame(set, take1);
            }

            final ImmutableIntSet take2 = set.take(2);
            if (size > 2) {
                assertEquals(2, take2.size());
                assertEquals(first, take2.valueAt(0));
                assertEquals(set.valueAt(1), take2.valueAt(1));
            }
            else {
                assertSame(set, take2);
            }

            assertSame(set, set.take(3));
            assertSame(set, set.take(4));
            assertSame(set, set.take(24));
        }))));
    }

    @Test
    public void testSkipLastWhenEmpty() {
        final ImmutableIntSet set = newIntBuilder().build();
        assertSame(set, set.skipLast(0));
        assertSame(set, set.skipLast(1));
        assertSame(set, set.skipLast(2));
        assertSame(set, set.skipLast(24));
    }

    @Test
    public void testSkipLast() {
        withValue(a -> withValue(b -> withValue(c -> {
            final ImmutableIntSet set = newIntBuilder().add(a).add(b).add(c).build();
            assertSame(set, set.skipLast(0));

            final int size = set.size();
            final int first = set.valueAt(0);
            final int second = (size >= 2)? set.valueAt(1) : 0;

            final ImmutableIntSet set1 = set.skipLast(1);
            assertEquals(size - 1, set1.size());
            if (size >= 2) {
                assertEquals(first, set1.valueAt(0));
                if (size == 3) {
                    assertEquals(second, set1.valueAt(1));
                }
            }

            final ImmutableIntSet set2 = set.skipLast(2);
            if (size < 3) {
                assertTrue(set2.isEmpty());
            }
            else {
                assertEquals(1, set2.size());
                assertEquals(first, set2.valueAt(0));
            }

            assertTrue(set.skipLast(3).isEmpty());
            assertTrue(set.skipLast(4).isEmpty());
            assertTrue(set.skipLast(24).isEmpty());
        })));
    }
}

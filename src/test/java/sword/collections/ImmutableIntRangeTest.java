package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImmutableIntRangeTest {

    // This is used in some tests that iterates in a range instance as the maximum allowed size.
    // If the size is exceeded, then that range instance is not tested.
    // This is mainly to avoid spending too much time executing the tests
    private static final int RANGE_SIZE_THRESHOLD = 10;

    private static final int[] INT_VALUES = {
            Integer.MIN_VALUE,
            Integer.MIN_VALUE + RANGE_SIZE_THRESHOLD - 3,
            -RANGE_SIZE_THRESHOLD,
            -2, 0, 1, 3,
            RANGE_SIZE_THRESHOLD,
            Integer.MAX_VALUE - RANGE_SIZE_THRESHOLD + 3,
            Integer.MAX_VALUE
    };

    private void withValue(IntProcedure procedure) {
        for (int value : INT_VALUES) {
            procedure.apply(value);
        }
    }

    private void withRange(Procedure<ImmutableIntRange> procedure) {
        for (int min : INT_VALUES) {
            for (int max : INT_VALUES) {
                if (min <= max) {
                    procedure.apply(new ImmutableIntRange(min, max));
                }
            }
        }
    }

    private void withSmallRange(Procedure<ImmutableIntRange> procedure) {
        for (int min : INT_VALUES) {
            for (int max : INT_VALUES) {
                if (min <= max && max <= min + RANGE_SIZE_THRESHOLD - 1) {
                    procedure.apply(new ImmutableIntRange(min, max));
                }
            }
        }
    }

    private void withMapFunc(Procedure<IntFunction<String>> procedure) {
        procedure.apply(Integer::toString);
    }

    private void withMapToIntFunc(Procedure<IntToIntFunction> procedure) {
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
    void testMinMaxAndSizeConsistency() {
        withRange(range -> assertEquals(range.size(), range.max() - range.min() + 1));
    }

    @Test
    void testSum() {
        withSmallRange(range -> {
            int result = 0;
            for (int value : range) {
                result += value;
            }

            assertEquals(result, range.sum());
        });
    }

    @Test
    void testContains() {
        withRange(range -> withValue(value -> {
            if (value >= range.min() && value <= range.max()) {
                assertTrue(range.contains(value));
            }
            else {
                assertFalse(range.contains(value));
            }
        }));
    }

    @Test
    void testIteration() {
        withSmallRange(range -> {
            final int max = range.max();
            final Iterator<Integer> it = range.iterator();
            for (int value = range.min(); value <= max; value++) {
                assertTrue(it.hasNext());
                assertEquals(value, it.next().intValue());
            }

            assertFalse(it.hasNext());
        });
    }

    @Test
    void testIndexOfForSingleElement() {
        withValue(a -> {
            final IntTraversable list = new ImmutableIntRange(a, a);

            withValue(value -> assertEquals((a == value)? 0 : -1, list.indexOf(value)));
        });
    }

    @Test
    void testIndexOfForMultipleElements() {
        withRange(range -> {
            final int min = range.min();
            withValue(value -> {
                final boolean shouldBePresent = value >= min && value <= range.max();
                final int expectedIndex = shouldBePresent? value - min : -1;
                assertEquals(expectedIndex, range.indexOf(value));
            });
        });
    }

    @Test
    void testToImmutable() {
        withRange(range -> assertSame(range, range.toImmutable()));
    }

    @Test
    void testMutable() {
        withSmallRange(range -> {
            final MutableIntArraySet set = range.mutate();
            assertEquals(range.size(), set.size());
            assertEquals(range.min(), set.min());
            assertEquals(range.max(), set.max());
        });
    }

    @Test
    void testAdd() {
        withSmallRange(range -> withValue(value -> {
            final ImmutableIntSet set = range.add(value);

            if (range.contains(value)) {
                assertSame(range, set);
            }
            else {
                assertEquals(range.size() + 1, set.size());
                for (int v : range) {
                    assertTrue(set.contains(v));
                }
                assertTrue(set.contains(value));
            }
        }));
    }

    @Test
    void testRemove() {
        withSmallRange(range -> withValue(value -> {
            final ImmutableIntSet set = range.remove(value);

            if (!range.contains(value)) {
                assertSame(range, set);
            }
            else {
                assertEquals(range.size() - 1, set.size());
                for (int v : range) {
                    if (value != v) {
                        assertTrue(set.contains(v));
                    }
                    else {
                        assertFalse(set.contains(value));
                    }
                }
            }
        }));
    }

    @Test
    void testToList() {
        for (int min : INT_VALUES) {
            for (int i = 0; i < 3; i++) {
                final int max = min + i;
                if (max > min) {
                    final ImmutableIntRange set = new ImmutableIntRange(min, max);
                    final Iterator<Integer> listIt = set.toList().iterator();

                    for (int value : set) {
                        assertTrue(listIt.hasNext());
                        assertEquals(value, (int) listIt.next());
                    }
                    assertFalse(listIt.hasNext());
                }
            }
        }
    }

    @Test
    void testGroupBy() {
        withGroupingFunc(func -> withSmallRange(range -> {
            final ImmutableMap<String, ImmutableIntSet> map = range.groupBy(func);
            final int mapLength = map.size();

            int count = 0;
            for (int mapIndex = 0; mapIndex < mapLength; mapIndex++) {
                final int setLength = map.valueAt(mapIndex).size();
                assertFalse(setLength > range.size());
                assertNotEquals(0, setLength);
                count += setLength;
            }
            assertEquals(range.size(), count);

            for (int value : range) {
                final String group = func.apply(value);
                for (int mapIndex = 0; mapIndex < mapLength; mapIndex++) {
                    final ImmutableIntSet set = map.valueAt(mapIndex);
                    if (SortUtils.equal(group, map.keyAt(mapIndex))) {
                        assertTrue(set.contains(value));
                    }
                    else {
                        assertFalse(set.contains(value));
                    }
                }
            }
        }));
    }

    @Test
    void testGroupByInt() {
        withGroupingIntFunc(func -> withSmallRange(range -> {
            final ImmutableIntKeyMap<ImmutableIntSet> map = range.groupByInt(func);
            final int mapLength = map.size();

            int count = 0;
            for (int mapIndex = 0; mapIndex < mapLength; mapIndex++) {
                final int setLength = map.valueAt(mapIndex).size();
                assertFalse(setLength > range.size());
                assertNotEquals(0, setLength);
                count += setLength;
            }
            assertEquals(range.size(), count);

            for (int value : range) {
                final int group = func.apply(value);
                for (int mapIndex = 0; mapIndex < mapLength; mapIndex++) {
                    final ImmutableIntSet set = map.valueAt(mapIndex);
                    if (group == map.keyAt(mapIndex)) {
                        assertTrue(set.contains(value));
                    }
                    else {
                        assertFalse(set.contains(value));
                    }
                }
            }
        }));
    }

    @Test
    void testMap() {
        withSmallRange(range -> withMapFunc(func -> {
            final Iterator<Integer> it = range.iterator();
            final Iterator<String> mappedIt = range.map(func).iterator();

            while (it.hasNext()) {
                assertTrue(mappedIt.hasNext());
                assertEquals(func.apply(it.next()), mappedIt.next());
            }
            assertFalse(mappedIt.hasNext());
        }));
    }

    @Test
    void testMapToInt() {
        withSmallRange(range -> withMapToIntFunc(func -> {
            final Iterator<Integer> it = range.iterator();
            final Iterator<Integer> mappedIt = range.mapToInt(func).iterator();

            while (it.hasNext()) {
                assertTrue(mappedIt.hasNext());
                assertEquals(func.apply(it.next()), mappedIt.next().intValue());
            }
            assertFalse(mappedIt.hasNext());
        }));
    }

    @Test
    void testRemoveAtForSingleElement() {
        withValue(value -> {
            final ImmutableIntRange collection = new ImmutableIntRange(value, value);
            assertTrue(collection.removeAt(0).isEmpty());
        });
    }

    @Test
    void testRemoveAtForMultipleElements() {
        withSmallRange(range -> {
            final int size = range.size();
            for (int index = 0; index < size; index++) {
                final IntTransformer origTransformer = range.iterator();
                final IntTransformer removedTransformer = range.removeAt(index).iterator();
                for (int i = 0; i < size; i++) {
                    final int value = origTransformer.next();
                    if (i != index) {
                        assertEquals(value, removedTransformer.next().intValue());
                    }
                }
                assertFalse(removedTransformer.hasNext());
            }
        });
    }

    @Test
    void testSlice() {
        withValue(a -> withValue(b -> {
            if (a <= b) {
                final ImmutableIntRange set = new ImmutableIntRange(a, b);
                if (set.size() > 0) { // Size can be negative if it matches or exceeds 2^31
                    final ImmutableIntSet sliceA = set.slice(new ImmutableIntRange(0, 0));
                    if (a == b) {
                        assertSame(set, sliceA);
                    }
                    else {
                        assertEquals(1, sliceA.size());
                        assertEquals(a, sliceA.valueAt(0));
                    }

                    final ImmutableIntSet sliceB = set.slice(new ImmutableIntRange(1, 1));
                    if (a == b) {
                        assertTrue(sliceB.isEmpty());
                    }
                    else {
                        assertEquals(1, sliceB.size());
                        assertEquals(a + 1, sliceB.valueAt(0));
                    }

                    final ImmutableIntSet sliceC = set.slice(new ImmutableIntRange(2, 2));
                    if ((long) a + 2 <= (long) b) {
                        assertEquals(1, sliceC.size());
                        assertEquals(a + 2, sliceC.valueAt(0));
                    }
                    else {
                        assertTrue(sliceC.isEmpty());
                    }

                    final ImmutableIntSet sliceAB = set.slice(new ImmutableIntRange(0, 1));
                    if (a == b) {
                        assertEquals(1, sliceAB.size());
                    }
                    else if (a + 1 == b) {
                        assertSame(set, sliceAB);
                    }
                    else {
                        assertEquals(2, sliceAB.size());
                        assertEquals(a + 1, sliceAB.valueAt(1));
                    }
                    assertEquals(a, sliceAB.valueAt(0));

                    final ImmutableIntSet sliceBC = set.slice(new ImmutableIntRange(1, 2));
                    if (a == b) {
                        assertTrue(sliceBC.isEmpty());
                    }
                    else if (a + 1 == b) {
                        assertEquals(1, sliceBC.size());
                        assertEquals(a + 1, sliceBC.valueAt(0));
                    }
                    else {
                        assertEquals(2, sliceBC.size());
                        assertEquals(a + 1, sliceBC.valueAt(0));
                        assertEquals(a + 2, sliceBC.valueAt(1));
                    }
                }
            }
        }));
    }
}

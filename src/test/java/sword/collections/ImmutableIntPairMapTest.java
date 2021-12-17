package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withInt;

public final class ImmutableIntPairMapTest implements IntPairMapTest<ImmutableIntTransformableBuilder>, ImmutableIntTransformableTest<ImmutableIntTransformableBuilder> {

    @Override
    public ImmutableIntPairMap.Builder newBuilder() {
        return new ImmutableIntPairMap.Builder();
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

    private static final ImmutableIntPairMap[] IMMUTABLE_INT_PAIR_MAP_VALUEs = new ImmutableIntPairMap[] {
            null,
            new ImmutableIntPairMap.Builder().build(),
            new ImmutableIntPairMap.Builder().put(0, 0).build(),
            new ImmutableIntPairMap.Builder().put(0, -4).build(),
            new ImmutableIntPairMap.Builder().put(0, 6).build(),
            new ImmutableIntPairMap.Builder().put(Integer.MIN_VALUE, 0).build(),
            new ImmutableIntPairMap.Builder().put(Integer.MAX_VALUE, 0).build(),
            new ImmutableIntPairMap.Builder().put(124, 12).build(),
            new ImmutableIntPairMap.Builder().put(127, -17).build(),
            new ImmutableIntPairMap.Builder().put(125, 0).build(),
            new ImmutableIntPairMap.Builder().put(-3, 13).build(),
            new ImmutableIntPairMap.Builder().put(-45, 0).build(),
            new ImmutableIntPairMap.Builder().put(-42, -1).build(),
            new ImmutableIntPairMap.Builder().put(0, -4).put(12234, 12345).build(),
            new ImmutableIntPairMap.Builder().put(0, 4).put(1, 4).build(), // Intentionally no reversable
            new ImmutableIntPairMap.Builder().put(-34, -33).put(2, 3).put(Integer.MAX_VALUE, Integer.MIN_VALUE).build()
    };

    private void withImmutableSparseIntArray(Procedure<ImmutableIntPairMap> procedure) {
        final int length = IMMUTABLE_INT_PAIR_MAP_VALUEs.length;
        for (int i = 0; i < length; i++) {
            procedure.apply(IMMUTABLE_INT_PAIR_MAP_VALUEs[i]);
        }
    }

    private boolean valueIsEven(int value) {
        return (value & 1) == 0;
    }

    @Override
    public IntTransformableBuilder newIntBuilder() {
        return new SameKeyAndValueTraversableBuilder();
    }

    @Override
    public void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::valueIsEven);
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntTransformableBuilder>> procedure) {
        procedure.apply(SameKeyAndValueTraversableBuilder::new);
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntPairMapBuilderSupplier<IntPairMapBuilder>> procedure) {
        procedure.apply(ImmutableIntPairMap.Builder::new);
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withInt(procedure);
    }

    @Test
    public void testPutMethod() {
        withImmutableSparseIntArray(array -> withInt(key -> withInt(value -> {
            if (array != null) {
                boolean contained = false;
                for (int i = 0; i < array.size(); i++) {
                    if (array.keyAt(i) == key) {
                        contained = true;
                        break;
                    }
                }

                final ImmutableIntPairMap newArray = array.put(key, value);

                if (!contained) {
                    final ImmutableIntPairMap.Builder builder = new ImmutableIntPairMap.Builder();
                    for (IntPairMap.Entry entry : array.entries()) {
                        builder.put(entry.key(), entry.value());
                    }
                    assertEquals(builder.put(key, value).build(), newArray);
                }
            }
        })));
    }

    @Test
    void testPutAllMethodForMultipleElementsInThisMap() {
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap thisMap = newBuilder().build();
            final ImmutableIntPairMap thatMap = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .build();

            assertEquals(thatMap, thisMap.putAll(thatMap));
        }));
    }

    @Test
    void testPutAllMethodForEmptyGivenMap() {
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap thisMap = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .build();

            assertSame(thisMap, thisMap.putAll(newBuilder().build()));
        }));
    }

    @Test
    void testPutAllMethodForMultipleElementsInTheGivenMap() {
        withInt(a -> withInt(b -> withInt(c -> withInt(d -> {
            final ImmutableIntPairMap thisMap = newBuilder()
                    .put(a, a)
                    .put(b, b)
                    .build();

            final ImmutableIntPairMap thatMap = newBuilder()
                    .put(c, c)
                    .put(d, d)
                    .build();

            final ImmutableIntPairMap.Builder builder = newBuilder();
            for (IntPairMap.Entry entry : thisMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            for (IntPairMap.Entry entry : thatMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            assertEquals(builder.build(), thisMap.putAll(thatMap));
        }))));
    }

    @Test
    public void testReverseMethod() {
        withImmutableSparseIntArray(array -> {
            if (array != null) {
                // Check if the array is reversable, so no duplicated values should be found
                final int length = array.size();
                boolean duplicated = false;
                for (int i = 0; i < length - 1; i++) {
                    for (int j = i + 1; j < length; j++) {
                        if (array.valueAt(i) == array.valueAt(j)) {
                            duplicated = true;
                        }
                        break;
                    }

                    if (duplicated) {
                        break;
                    }
                }

                if (!duplicated) {
                    final ImmutableIntPairMap reversed = array.reverse();
                    assertEquals(length, reversed.size());

                    for (int i = 0; i < length; i++) {
                        assertEquals(reversed.keyAt(i), array.get(reversed.valueAt(i)));
                    }
                }
            }
        });
    }

    @Test
    public void testKeySetWhenEmpty() {
        final ImmutableIntPairMap empty = ImmutableIntPairMap.empty();
        assertSame(ImmutableIntArraySet.empty(), empty.keySet());
    }

    @Test
    public void testToImmutableForEmpty() {
        final ImmutableIntPairMap map = newBuilder().build();
        assertSame(map, map.toImmutable());
    }

    @Test
    public void testMutateForEmpty() {
        final ImmutableIntPairMap map1 = newBuilder().build();
        final MutableIntPairMap map2 = map1.mutate();

        assertTrue(map2.isEmpty());

        map2.put(1, 4);
        assertEquals(0, map1.get(1, 0));
    }

    @Test
    public void testToImmutable() {
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap map1 = newBuilder().put(a, 1).put(b, 2).build();
            final ImmutableIntPairMap map2 = map1.toImmutable();
            assertSame(map1, map2);
        }));
    }

    @Test
    public void testMutate() {
        final int defValue = -4;
        withInt(a -> withInt(b -> {
            final ImmutableIntPairMap map1 = newBuilder().put(a, 1).put(b, 2).build();
            final MutableIntPairMap map2 = map1.mutate();

            final Iterator<IntPairMap.Entry> it1 = map1.entries().iterator();
            final Iterator<IntPairMap.Entry> it2 = map2.entries().iterator();
            while (it1.hasNext()) {
                assertTrue(it2.hasNext());
                final IntPairMap.Entry item1 = it1.next();
                final IntPairMap.Entry item2 = it2.next();
                assertEquals(item1.key(), item2.key());
                assertEquals(item1.value(), item2.value());
            }
            assertFalse(it2.hasNext());

            map2.remove(b);
            assertEquals(2, map1.get(b, defValue));
            assertEquals(defValue, map2.get(b, defValue));
        }));
    }

    static final class SameKeyAndValueTraversableBuilder implements ImmutableIntTransformableBuilder {
        private final ImmutableIntPairMap.Builder builder = new ImmutableIntPairMap.Builder();

        @Override
        public SameKeyAndValueTraversableBuilder add(int value) {
            builder.put(value, value);
            return this;
        }

        @Override
        public ImmutableIntPairMap build() {
            return builder.build();
        }
    }
}

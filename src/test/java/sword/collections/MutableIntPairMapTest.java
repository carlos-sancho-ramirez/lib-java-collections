package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.SortUtils.equal;
import static sword.collections.TestUtils.withInt;

public final class MutableIntPairMapTest extends IntPairMapTest<MutableIntTransformableBuilder> implements MutableIntTraversableTest<MutableIntTransformableBuilder> {

    @Override
    MutableIntPairMap.Builder newBuilder() {
        return new MutableIntPairMap.Builder();
    }

    private boolean valueIsEven(int value) {
        return (value & 1) == 0;
    }

    @Override
    IntTransformableBuilder newIntBuilder() {
        return new SameKeyAndValueTraversableBuilder();
    }

    @Override
    void withFilterFunc(Procedure<IntPredicate> procedure) {
        procedure.apply(this::valueIsEven);
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<MutableIntTransformableBuilder>> procedure) {
        procedure.apply(SameKeyAndValueTraversableBuilder::new);
    }

    @Override
    void withMapBuilderSupplier(Procedure<IntPairMapBuilderSupplier<IntPairMapBuilder>> procedure) {
        procedure.apply(MutableIntPairMap.Builder::new);
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withInt(procedure);
    }

    @Test
    void testToImmutableForEmpty() {
        assertTrue(newBuilder().build().toImmutable().isEmpty());
    }

    @Test
    void testMutateForEmpty() {
        final MutableIntPairMap map1 = newBuilder().build();
        final MutableIntPairMap map2 = map1.mutate();

        assertEquals(map1, map2);
        assertNotSame(map1, map2);

        map1.put(1, 1);
        assertEquals(0, map2.get(1, 0));
    }

    @Test
    void testToImmutable() {
        withInt(a -> withInt(b -> {
            final MutableIntPairMap map1 = newBuilder().put(a, 1).put(b, 2).build();
            final ImmutableIntPairMap map2 = map1.toImmutable();

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
        }));
    }

    @Test
    void testMutate() {
        final int defValue = -2;
        withInt(a -> withInt(b -> {
            final MutableIntPairMap map1 = newBuilder().put(a, 1).put(b, 2).build();
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

    @Test
    void testHashCode() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntPairMap mutable = newBuilder()
                    .put(a, b)
                    .put(b, c)
                    .put(c, a)
                    .build();
            final IntPairMap immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable.hashCode(), immutable.hashCode());
        })));
    }

    @Test
    void testEquals() {
        withInt(a -> withInt(b -> withInt(c -> {
            final IntPairMap mutable = newBuilder()
                    .put(a, b)
                    .put(b, c)
                    .put(c, a)
                    .build();
            final IntPairMap immutable = mutable.toImmutable();
            assertNotSame(mutable, immutable);
            assertEquals(mutable, immutable);
            assertEquals(immutable, mutable);
        })));
    }

    @Test
    void testDonateWhenEmpty() {
        final MutableIntPairMap map = newBuilder().build();
        final MutableIntPairMap map2 = map.donate();
        assertTrue(map.isEmpty());
        assertTrue(map2.isEmpty());
        assertNotSame(map, map2);
    }

    @Test
    void testDonateForSingleElement() {
        withInt(a -> {
            final MutableIntPairMap map = newBuilder().put(a, a).build();
            final MutableIntPairMap map2 = map.donate();
            assertTrue(map.isEmpty());
            assertEquals(1, map2.size());
            assertEquals(a, map2.keyAt(0));
            assertEquals(a, map2.valueAt(0));
        });
    }

    @Test
    void testDonateForSingleMultipleElements() {
        withInt(a -> withInt(b -> {
            final MutableIntPairMap map = newBuilder().put(a, a).put(b, b).build();
            final MutableIntPairMap map2 = map.donate();
            assertTrue(map.isEmpty());

            if (equal(a, b)) {
                assertEquals(1, map2.size());
                assertEquals(a, map2.keyAt(0));
                assertEquals(a, map2.valueAt(0));
            }
            else {
                assertEquals(2, map2.size());
                if (a == map2.keyAt(0)) {
                    assertEquals(a, map2.valueAt(0));
                    assertEquals(b, map2.keyAt(1));
                    assertEquals(b, map2.valueAt(1));
                }
                else {
                    assertEquals(b, map2.keyAt(0));
                    assertEquals(b, map2.valueAt(0));
                    assertEquals(a, map2.keyAt(1));
                    assertEquals(a, map2.valueAt(1));
                }
            }
        }));
    }

    @Test
    void testPick() {
        withInt(k1 -> withInt(k2 -> withInt(v1 -> {
            if (k1 == k2) {
                final MutableIntPairMap map = newBuilder().put(k1, v1).build();
                assertEquals(v1, map.pick(k1));
                assertTrue(map.isEmpty());
            }
            else {
                withInt(v2 -> {
                    final MutableIntPairMap map = newBuilder().put(k1, v1).put(k2, v2).build();
                    assertEquals(v1, map.pick(k1));
                    assertEquals(1, map.size());
                    assertEquals(v2, map.get(k2));
                });
            }
        })));
    }

    static final class SameKeyAndValueTraversableBuilder implements MutableIntTransformableBuilder {
        private final MutableIntPairMap map = MutableIntPairMap.empty();

        @Override
        public SameKeyAndValueTraversableBuilder add(int value) {
            map.put(value, value);
            return this;
        }

        @Override
        public MutableIntPairMap build() {
            return map;
        }
    }
}

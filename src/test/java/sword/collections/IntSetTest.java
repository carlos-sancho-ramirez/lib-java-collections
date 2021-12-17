package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class IntSetTest<B extends IntSet.Builder> implements IntTransformableTest<B> {

    @Test
    void testMutate() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().add(a).add(b).build();
            final MutableIntSet mutated = set.mutate();
            assertNotSame(set, mutated);

            mutated.add(c);
            assertTrue(mutated.contains(a));
            assertTrue(mutated.contains(b));
            assertTrue(mutated.contains(c));

            if (a == c || b == c) {
                assertEquals(set.size(), mutated.size());
            }
            else {
                assertEquals(set.size() + 1, mutated.size());
                assertFalse(set.contains(c));
            }
        }))));
    }

    @Test
    void testAssignWhenEmpty() {
        final IntFunction<String> func = key -> {
            throw new AssertionError("This function should not be called");
        };

        withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().build();
            final IntKeyMap<String> map = set.assign(func);
            assertFalse(map.iterator().hasNext());
        });
    }

    @Test
    void testAssign() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> withMapFunc(func -> {
            final IntSet set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();

            final IntKeyMap<String> map = set.assign(func);
            assertEquals(size, map.size());

            for (int i = 0; i < size; i++) {
                final int value = set.valueAt(i);
                assertEquals(value, map.keyAt(i));
                assertEquals(func.apply(value), map.valueAt(i));
            }
        })))));
    }

    @Test
    void testAssignToIntWhenEmpty() {
        final IntToIntFunction func = key -> {
            throw new AssertionError("This function should not be called");
        };

        withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().build();
            final IntPairMap map = set.assignToInt(func);
            assertFalse(map.iterator().hasNext());
        });
    }

    @Test
    void testAssignToInt() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> withMapToIntFunc(func -> {
            final IntSet set = supplier.newBuilder().add(a).add(b).add(c).build();
            final int size = set.size();

            final IntPairMap map = set.assignToInt(func);
            assertEquals(size, map.size());

            for (int i = 0; i < size; i++) {
                final int value = set.valueAt(i);
                assertEquals(value, map.keyAt(i));
                assertEquals(func.apply(value), map.valueAt(i));
            }
        })))));
    }

    @Test
    void testEqualSet() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final IntSet set = supplier.newBuilder().add(a).add(b).add(c).build();
            assertTrue(set.equalSet(set));

            final IntSet.Builder setBuilder = supplier.newBuilder();
            final IntTransformer it = set.iterator();
            it.next();
            while (it.hasNext()) {
                setBuilder.add(it.next());
            }
            final IntSet reducedSet = setBuilder.build();

            assertFalse(set.equalSet(reducedSet));
            assertFalse(reducedSet.equalSet(set));

            assertTrue(set.equalSet(set.mutate()));
            assertTrue(set.equalSet(set.toImmutable()));
        }))));
    }
}

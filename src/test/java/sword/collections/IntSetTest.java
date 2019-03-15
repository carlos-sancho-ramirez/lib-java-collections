package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class IntSetTest<B extends IntSet.Builder> extends IntTransformableTest<B> {

    @Test
    public void testMutate() {
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
    public void testEqualSet() {
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

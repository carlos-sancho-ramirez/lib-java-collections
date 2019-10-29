package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;

abstract class IntListTest<B extends IntListBuilder> extends IntTransformableTest<B> {

    @Test
    void testToImmutableWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final ImmutableIntList immutableList = supplier.newBuilder().build().toImmutable();
            assertTrue(immutableList.isEmpty());
            assertSame(immutableList, immutableList.toImmutable());
        });
    }

    @Test
    void testToImmutableForASingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final ImmutableIntList immutableList = supplier.newBuilder().add(value).build().toImmutable();
            assertEquals(1, immutableList.size());
            assertEquals(value, immutableList.valueAt(0));
            assertSame(immutableList, immutableList.toImmutable());
        }));
    }

    @Test
    void testToImmutableForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final ImmutableIntList immutableList = supplier.newBuilder().add(a).add(b).build().toImmutable();
            assertEquals(2, immutableList.size());
            assertEquals(a, immutableList.valueAt(0));
            assertEquals(b, immutableList.valueAt(1));
            assertSame(immutableList, immutableList.toImmutable());
        })));
    }

    @Test
    void testMutateWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final IntList list = supplier.newBuilder().build();
            final MutableIntList mutableList = list.mutate();
            assertTrue(mutableList.isEmpty());
            assertTrue(list.isEmpty());

            withValue(value -> {
                mutableList.append(value);
                assertTrue(list.isEmpty());
            });
        });
    }

    @Test
    void testMutateForASingleElement() {
        withValue(a -> withBuilderSupplier(supplier -> {
            final IntList list = supplier.newBuilder().add(a).build();
            final MutableIntList mutableList = list.mutate();
            assertEquals(1, list.size());
            assertEquals(a, list.valueAt(0));
            assertEquals(1, list.size());
            assertEquals(a, mutableList.valueAt(0));

            withValue(value -> {
                mutableList.append(value);
                assertEquals(1, list.size());
                assertEquals(a, list.valueAt(0));
            });
        }));
    }

    @Test
    void testMutateForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final IntList list = supplier.newBuilder().add(a).add(b).build();
            final MutableIntList mutableList = list.mutate();
            assertEquals(2, list.size());
            assertEquals(a, list.valueAt(0));
            assertEquals(b, list.valueAt(1));
            assertEquals(2, list.size());
            assertEquals(a, mutableList.valueAt(0));
            assertEquals(b, mutableList.valueAt(1));

            withValue(value -> {
                mutableList.append(value);
                assertEquals(2, list.size());
                assertEquals(a, list.valueAt(0));
                assertEquals(b, list.valueAt(1));
            });
        })));
    }
}

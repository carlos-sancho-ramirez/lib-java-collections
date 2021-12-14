package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class ListTest<T, B extends ListBuilder<T>> implements TransformableTest<T, B> {

    @Test
    void testToImmutableWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final ImmutableList<T> immutableList = supplier.newBuilder().build().toImmutable();
            assertTrue(immutableList.isEmpty());
            assertSame(immutableList, immutableList.toImmutable());
        });
    }

    @Test
    void testToImmutableForASingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final ImmutableList<T> immutableList = supplier.newBuilder().add(value).build().toImmutable();
            assertEquals(1, immutableList.size());
            assertSame(value, immutableList.valueAt(0));
            assertSame(immutableList, immutableList.toImmutable());
        }));
    }

    @Test
    void testToImmutableForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final ImmutableList<T> immutableList = supplier.newBuilder().add(a).add(b).build().toImmutable();
            assertEquals(2, immutableList.size());
            assertSame(a, immutableList.valueAt(0));
            assertSame(b, immutableList.valueAt(1));
            assertSame(immutableList, immutableList.toImmutable());
        })));
    }

    @Test
    void testMutateWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final List<T> list = supplier.newBuilder().build();
            final MutableList<T> mutableList = list.mutate();
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
            final List<T> list = supplier.newBuilder().add(a).build();
            final MutableList<T> mutableList = list.mutate();
            assertEquals(1, list.size());
            assertSame(a, list.valueAt(0));
            assertEquals(1, list.size());
            assertSame(a, mutableList.valueAt(0));

            withValue(value -> {
                mutableList.append(value);
                assertEquals(1, list.size());
                assertSame(a, list.valueAt(0));
            });
        }));
    }

    @Test
    void testMutateForMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final List<T> list = supplier.newBuilder().add(a).add(b).build();
            final MutableList<T> mutableList = list.mutate();
            assertEquals(2, list.size());
            assertSame(a, list.valueAt(0));
            assertSame(b, list.valueAt(1));
            assertEquals(2, list.size());
            assertSame(a, mutableList.valueAt(0));
            assertSame(b, mutableList.valueAt(1));

            withValue(value -> {
                mutableList.append(value);
                assertEquals(2, list.size());
                assertSame(a, list.valueAt(0));
                assertSame(b, list.valueAt(1));
            });
        })));
    }
}

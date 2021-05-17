package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface MutableIntSetTest<B extends MutableIntSet.Builder> extends MutableIntTraversableTest<B> {
    @Test
    default void testAdd() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final MutableIntSet set = supplier.newBuilder().build();
            assertTrue(set.add(a));
            assertFalse(set.isEmpty());

            if (a == b) {
                assertFalse(set.add(b));
                assertEquals(1, set.size());
                assertTrue(set.contains(b));
            }
            else {
                assertTrue(set.add(b));
                assertEquals(2, set.size());
                assertTrue(set.contains(a));
                assertTrue(set.contains(b));
            }
        })));
    }

    @Test
    default void testAddAll() {
        withValue(a -> withValue(b -> {
            final ImmutableIntSet values = new ImmutableIntSetCreator().add(a).add(b).build();
            withValue(c -> withBuilderSupplier(supplier -> {
                final MutableIntSet set = supplier.newBuilder().build();
                set.add(c);

                if (c == a && c == b) {
                    assertFalse(set.addAll(values));
                    assertEquals(1, set.size());
                    assertTrue(set.contains(c));
                }
                else {
                    assertTrue(set.addAll(values));
                    assertTrue(set.contains(a));
                    assertTrue(set.contains(b));
                    assertTrue(set.contains(c));
                    if (a == b || a == c || b == c) {
                        assertEquals(2, set.size());
                    }
                    else {
                        assertEquals(3, set.size());
                    }
                }
            }));
        }));
    }

    @Test
    default void testRemoveForEmptySet() {
        withBuilderSupplier(supplier -> {
            final MutableIntSet set = supplier.newBuilder().build();
            withValue(value -> {
                assertFalse(set.remove(value));
                assertTrue(set.isEmpty());
            });
        });
    }

    @Test
    default void testRemove() {
        withValue(a -> withValue(b -> withValue(c -> withBuilderSupplier(supplier -> {
            final MutableIntSet set = supplier.newBuilder().add(a).add(b).build();
            final int originalSize = set.size();
            final boolean shouldBeRemoved = a == c || b == c;
            assertEquals(shouldBeRemoved, set.remove(c));

            if (shouldBeRemoved) {
                assertEquals(originalSize - 1, set.size());
                if (a == c && b != c) {
                    assertEquals(b, set.valueAt(0));
                }
                else if (a != c) {
                    assertEquals(a, set.valueAt(0));
                }
            }
            else {
                assertEquals(originalSize, set.size());
                assertTrue(set.contains(a));
                assertTrue(set.contains(b));
            }
        }))));
    }

    @Test
    default void testDonateWhenEmpty() {
        withBuilderSupplier(supplier -> {
            final MutableIntSet set = supplier.newBuilder().build();
            final MutableIntSet set2 = set.donate();
            assertTrue(set.isEmpty());
            assertTrue(set2.isEmpty());
            assertNotSame(set, set2);
        });
    }

    @Test
    default void testDonateForSingleElement() {
        withValue(value -> withBuilderSupplier(supplier -> {
            final MutableIntSet set = supplier.newBuilder().add(value).build();
            final MutableIntSet set2 = set.donate();
            assertTrue(set.isEmpty());
            assertEquals(1, set2.size());
            assertEquals(value, set2.valueAt(0));
        }));
    }

    @Test
    default void testDonateForSingleMultipleElements() {
        withValue(a -> withValue(b -> withBuilderSupplier(supplier -> {
            final MutableIntSet map = supplier.newBuilder().add(a).add(b).build();
            final MutableIntSet map2 = map.donate();
            assertTrue(map.isEmpty());

            if (a == b) {
                assertEquals(1, map2.size());
                assertEquals(a, map2.valueAt(0));
            }
            else {
                assertEquals(2, map2.size());
                if (a == map2.valueAt(0)) {
                    assertEquals(b, map2.valueAt(1));
                }
                else {
                    assertEquals(b, map2.valueAt(0));
                    assertEquals(a, map2.valueAt(1));
                }
            }
        })));
    }
}

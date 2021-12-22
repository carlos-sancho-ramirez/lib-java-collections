package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface ImmutableMapTest<K, V, B extends ImmutableTransformableBuilder<V>, MB extends ImmutableMap.Builder<K, V>> extends MapTest<K, V, B, MB>, ImmutableTransformableTest<V, B> {

    MB newBuilder();
    void withKey(Procedure<K> procedure);
    V valueFromKey(K key);

    @Test
    default void testPutAllMethodForMultipleElementsInThisMap() {
        withKey(a -> withKey(b -> {
            final ImmutableMap<K, V> thisMap = newBuilder().build();
            final ImmutableMap<K, V> thatMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            assertEquals(thatMap, thisMap.putAll(thatMap));
        }));
    }

    @Test
    default void testPutAllMethodForEmptyGivenMap() {
        withKey(a -> withKey(b -> {
            final ImmutableMap<K, V> thisMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            assertSame(thisMap, thisMap.putAll(newBuilder().build()));
        }));
    }

    @Test
    default void testPutAllMethodForMultipleElementsInTheGivenMap() {
        withKey(a -> withKey(b -> withKey(c -> withKey(d -> {
            final ImmutableMap<K, V> thisMap = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();

            final ImmutableMap<K, V> thatMap = newBuilder()
                    .put(c, valueFromKey(c))
                    .put(d, valueFromKey(d))
                    .build();

            final ImmutableMap.Builder<K, V> builder = newBuilder();
            for (Map.Entry<K, V> entry : thisMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            for (Map.Entry<K, V> entry : thatMap.entries()) {
                builder.put(entry.key(), entry.value());
            }

            assertEquals(builder.build(), thisMap.putAll(thatMap));
        }))));
    }

    @Test
    @Override
    default void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final Map.Entry<K, V> entry = new Map.Entry<>(0, key, valueFromKey(key));
            final ImmutableMap<K, V> map = supplier.newBuilder().put(key, entry.value()).build();
            final ImmutableMap<K, V> filtered = map.filterByEntry(f);

            if (f.apply(entry)) {
                assertTrue(map.equalMap(filtered));
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        })));
    }

    @Test
    @Override
    default void testFilterByEntryForMultipleElements() {
        withFilterByEntryFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableMap<K, V> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableMap<K, V> filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            int counter = 0;
            for (Map.Entry<K, V> entry : map.entries()) {
                if (f.apply(entry)) {
                    assertSame(entry.value(), filtered.get(entry.key()));
                    counter++;
                }
            }
            assertEquals(filteredSize, counter);
        }))));
    }
}

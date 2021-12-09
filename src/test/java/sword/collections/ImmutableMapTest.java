package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public interface ImmutableMapTest<K, V, B extends ImmutableTransformableBuilder<V>> extends ImmutableTransformableTest<V, B> {

    ImmutableMap.Builder<K, V> newBuilder();
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
}

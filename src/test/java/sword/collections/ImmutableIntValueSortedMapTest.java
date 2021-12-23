package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withInt;

public final class ImmutableIntValueSortedMapTest implements ImmutableIntValueMapTest<String, ImmutableIntTransformableBuilder, ImmutableIntValueSortedMap.Builder<String>> {

    @Override
    public ImmutableIntValueSortedMap.Builder<String> newBuilder() {
        return new ImmutableIntValueSortedMap.Builder<>(SortUtils::compareCharSequenceByUnicode);
    }

    @Override
    public void withKey(Procedure<String> procedure) {
        final String[] values = {null, "", " ", "abcd", "0"};
        for (String value : values) {
            procedure.apply(value);
        }
    }

    @Override
    public void withSortFunc(Procedure<SortFunction<String>> procedure) {
        procedure.apply(SortUtils::compareCharSequenceByUnicode);
        procedure.apply(SortUtils::compareByHashCode);
    }

    @Override
    public String keyFromInt(int value) {
        return Integer.toString(value);
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntTransformableBuilder>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new SameKeyAndValueTraversableBuilder(sortFunc)));
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<String, ImmutableIntValueSortedMap.Builder<String>>> procedure) {
        withSortFunc(sortFunc -> procedure.apply(() -> new ImmutableIntValueSortedMap.Builder<>(sortFunc)));
    }

    @Override
    public IntTransformableBuilder newIntBuilder() {
        return new SameKeyAndValueTraversableBuilder(SortUtils::compareByHashCode);
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withInt(procedure);
    }

    @Test
    @Override
    public void testFilterByEntryWhenEmpty() {
        final Predicate<IntValueMapEntry<String>> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            final ImmutableIntValueSortedMap<String> empty = supplier.newBuilder().build();
            final ImmutableIntValueSortedMap<String> filtered = empty.filterByEntry(f);
            assertSame(empty, filtered);
            assertTrue(filtered.isEmpty());
        });
    }

    @Test
    @Override
    public void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final IntValueMap.Entry<String> entry = new IntValueMap.Entry<>(0, key, valueFromKey(key));
            final ImmutableIntValueSortedMap<String> map = supplier.newBuilder().put(key, entry.value()).build();
            final ImmutableIntValueSortedMap<String> filtered = map.filterByEntry(f);

            if (f.apply(entry)) {
                assertSame(map, filtered);
            }
            else {
                assertTrue(filtered.isEmpty());
            }
        })));
    }

    @Test
    @Override
    public void testFilterByEntryForMultipleElements() {
        withFilterByEntryFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableIntValueSortedMap<String> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableIntValueSortedMap<String> filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            if (filteredSize == map.size()) {
                assertSame(map, filtered);
            }
            else {
                int counter = 0;
                for (IntValueMap.Entry<String> entry : map.entries()) {
                    if (f.apply(entry)) {
                        assertEquals(entry.value(), filtered.get(entry.key()));
                        counter++;
                    }
                }
                assertEquals(filteredSize, counter);
            }
        }))));
    }

    @Test
    void testPutAllMustReturnAnImmutableHashMap() {
        final ImmutableIntValueSortedMap<String> map = newBuilder().build();
        final ImmutableIntValueSortedMap<String> result = map.putAll(map);
        assertSame(result, map);
    }

    private static final class SameKeyAndValueTraversableBuilder implements ImmutableIntTransformableBuilder {
        private final ImmutableIntValueSortedMap.Builder<String> builder;

        SameKeyAndValueTraversableBuilder(SortFunction<String> sortFunction) {
            builder = new ImmutableIntValueSortedMap.Builder<>(sortFunction);
        }

        @Override
        public SameKeyAndValueTraversableBuilder add(int value) {
            builder.put(Integer.toString(value), value);
            return this;
        }

        @Override
        public ImmutableIntTransformable build() {
            return builder.build();
        }
    }
}

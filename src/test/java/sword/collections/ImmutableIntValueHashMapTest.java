package sword.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sword.collections.TestUtils.withInt;

public final class ImmutableIntValueHashMapTest implements ImmutableIntValueMapTest<String, ImmutableIntTransformableBuilder, ImmutableIntValueHashMap.Builder<String>> {

    @Override
    public ImmutableIntValueHashMap.Builder<String> newBuilder() {
        return new ImmutableIntValueHashMap.Builder<>();
    }

    @Override
    public void withMapBuilderSupplier(Procedure<IntValueMapBuilderSupplier<String, ImmutableIntValueHashMap.Builder<String>>> procedure) {
        procedure.apply(ImmutableIntValueHashMap.Builder::new);
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
    public void assertEmpty(ImmutableIntValueMap<String> map) {
        assertSame(newBuilder().build(), map);
    }

    @Override
    public void withBuilderSupplier(Procedure<IntBuilderSupplier<ImmutableIntTransformableBuilder>> procedure) {
        procedure.apply(SameKeyAndValueTraversableBuilder::new);
    }

    @Override
    public IntTransformableBuilder newIntBuilder() {
        return new SameKeyAndValueTraversableBuilder();
    }

    @Override
    public void withValue(IntProcedure procedure) {
        withInt(procedure);
    }

    @Test
    void testFilterByKeyReturnTheSameInstanceAndTypeWhenEmpty() {
        final Predicate<String> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        final ImmutableIntValueHashMap<String> map = newBuilder().build();
        final ImmutableIntValueHashMap<String> filtered = map.filterByKey(f);
        assertSame(map, filtered);
    }

    @Test
    void testFilterByKeyReturnTheSameInstanceAndType() {
        final Predicate<String> f = unused -> true;
        withKey(a -> withKey(b -> {
            final ImmutableIntValueHashMap<String> map = newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableIntValueMap<String> filtered = map.filterByKey(f);
            assertSame(map, filtered);
        }));
    }

    @Test
    @Override
    public void testFilterByEntryWhenEmpty() {
        final Predicate<IntValueMapEntry<String>> f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        withMapBuilderSupplier(supplier -> {
            final ImmutableIntValueHashMap<String> empty = supplier.newBuilder().build();
            final ImmutableIntValueHashMap<String> filtered = empty.filterByEntry(f);
            assertSame(empty, filtered);
            assertTrue(filtered.isEmpty());
        });
    }

    @Test
    @Override
    public void testFilterByEntryForSingleElement() {
        withFilterByEntryFunc(f -> withKey(key -> withMapBuilderSupplier(supplier -> {
            final IntValueMap.Entry<String> entry = new IntValueMap.Entry<>(0, key, valueFromKey(key));
            final ImmutableIntValueHashMap<String> map = supplier.newBuilder().put(key, entry.value()).build();
            final ImmutableIntValueHashMap<String> expected = f.apply(entry)? map : supplier.newBuilder().build();
            assertSame(expected, map.filterByEntry(f));
        })));
    }

    @Test
    @Override
    public void testFilterByEntryForMultipleElements() {
        withFilterByEntryFunc(f -> withKey(a -> withKey(b -> withMapBuilderSupplier(supplier -> {
            final ImmutableIntValueHashMap<String> map = supplier.newBuilder()
                    .put(a, valueFromKey(a))
                    .put(b, valueFromKey(b))
                    .build();
            final ImmutableIntValueHashMap<String> filtered = map.filterByEntry(f);
            final int filteredSize = filtered.size();

            if (filteredSize == 0) {
                assertSame(supplier.newBuilder().build(), filtered);
            }
            else if (filteredSize == map.size()) {
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
        final ImmutableIntValueHashMap<String> map = newBuilder().build();
        final ImmutableIntValueHashMap<String> result = map.putAll(map);
        assertSame(result, map);
    }

    static final class SameKeyAndValueTraversableBuilder implements ImmutableIntTransformableBuilder {
        private final ImmutableIntValueHashMap.Builder<String> builder = new ImmutableIntValueHashMap.Builder<>();

        @Override
        public SameKeyAndValueTraversableBuilder add(int value) {
            builder.put(Integer.toString(value), value);
            return this;
        }

        @Override
        public ImmutableIntValueHashMap<String> build() {
            return builder.build();
        }
    }
}

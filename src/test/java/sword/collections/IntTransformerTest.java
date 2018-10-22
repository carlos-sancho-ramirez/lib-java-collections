package sword.collections;

abstract class IntTransformerTest<C extends IntTransformable, B extends IntCollectionBuilder<C>> extends IntTraverserTest<C, B> {

    public void testToListWhenEmpty() {
        withBuilder(builder -> assertTrue(builder.build().iterator().toList().isEmpty()));
    }

    public void testToListForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final C transformable = builder.add(value).build();
            final IntList expected = new ImmutableIntList.Builder().add(value).build();
            assertEquals(expected, transformable.iterator().toList().toImmutable());
        }));
    }

    public void testToListForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final C transformable = builder.add(a).add(b).add(c).build();
            final ImmutableIntList.Builder listBuilder = new ImmutableIntList.Builder();
            for (int value : transformable) {
                listBuilder.add(value);
            }
            assertEquals(listBuilder.build(), transformable.iterator().toList().toImmutable());
        }))));
    }

    public void testToSetWhenEmpty() {
        withBuilder(builder -> assertTrue(builder.build().iterator().toSet().isEmpty()));
    }

    public void testToSetForSingleElement() {
        withValue(value -> withBuilder(builder -> {
            final IntSet set = builder.add(value).build().iterator().toSet();
            assertEquals(1, set.size());
            assertEquals(value, set.valueAt(0));
        }));
    }

    public void testToSetForMultipleElements() {
        withValue(a -> withValue(b -> withValue(c -> withBuilder(builder -> {
            final C transformable = builder.add(a).add(b).add(c).build();
            final ImmutableIntSetBuilder setBuilder = new ImmutableIntSetBuilder();
            for (int value : transformable) {
                setBuilder.add(value);
            }
            assertEquals(setBuilder.build(), transformable.iterator().toSet().toImmutable());
        }))));
    }
}

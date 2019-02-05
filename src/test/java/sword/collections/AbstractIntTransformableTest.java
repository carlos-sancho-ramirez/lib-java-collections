package sword.collections;

import java.util.Iterator;

abstract class AbstractIntTransformableTest extends AbstractIntIterableTest {

    abstract IntTransformableBuilder newIntBuilder();
    abstract void assertEmptyCollection(IntTransformable transformable);
    abstract void assertNotChanged(Object expected, Object given);

    public void testFilterWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertEmptyCollection(newIntBuilder().build().filter(f));
    }

    public void testFilterForSingleElement() {
        withFilterFunc(f -> withItem(value -> {
            final IntTransformable transformable = newIntBuilder().add(value).build();
            final IntTransformable filtered = transformable.filter(f);

            if (f.apply(value)) {
                assertNotChanged(transformable, filtered);
            }
            else {
                assertEmptyCollection(filtered);
            }
        }));
    }

    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withItem(a -> withItem(b -> {
            final IntTransformable transformable = newIntBuilder().add(a).add(b).build();
            final IntTransformable filtered = transformable.filter(f);

            final boolean aPassed = f.apply(a);
            final boolean bPassed = f.apply(b);

            if (aPassed && bPassed) {
                assertNotChanged(transformable, filtered);
            }
            else if (aPassed) {
                Iterator<Integer> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.next().intValue());
                assertFalse(iterator.hasNext());
            }
            else if (bPassed) {
                Iterator<Integer> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(b, iterator.next().intValue());
                assertFalse(iterator.hasNext());
            }
            else {
                assertEmptyCollection(filtered);
            }
        })));
    }

    public void testFilterNotWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertEmptyCollection(newIntBuilder().build().filterNot(f));
    }

    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withItem(value -> {
            final IntTransformable transformable = newIntBuilder().add(value).build();
            final IntTransformable filtered = transformable.filterNot(f);

            if (f.apply(value)) {
                assertEmptyCollection(filtered);
            }
            else {
                assertNotChanged(transformable, filtered);
            }
        }));
    }

    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withItem(a -> withItem(b -> {
            final IntTransformable transformable = newIntBuilder().add(a).add(b).build();
            final IntTransformable filtered = transformable.filterNot(f);

            final boolean aRemoved = f.apply(a);
            final boolean bRemoved = f.apply(b);

            if (aRemoved && bRemoved) {
                assertEmptyCollection(filtered);
            }
            else if (aRemoved) {
                Iterator<Integer> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(b, iterator.next().intValue());
                assertFalse(iterator.hasNext());
            }
            else if (bRemoved) {
                Iterator<Integer> iterator = filtered.iterator();
                assertTrue(iterator.hasNext());
                assertEquals(a, iterator.next().intValue());
                assertFalse(iterator.hasNext());
            }
            else {
                assertNotChanged(transformable, filtered);
            }
        })));
    }
}

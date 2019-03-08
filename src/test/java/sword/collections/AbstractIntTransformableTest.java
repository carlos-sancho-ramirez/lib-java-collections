package sword.collections;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractIntTransformableTest extends AbstractIntTraversableTest {

    abstract IntTransformableBuilder newIntBuilder();

    public void testFilterWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().filter(f).iterator().hasNext());
    }

    @Test
    public void testFilterForSingleElement() {
        withFilterFunc(f -> withItem(value -> {
            final IntTransformable transformable = newIntBuilder().add(value).build();
            final IntTransformable filtered = transformable.filter(f);

            if (f.apply(value)) {
                assertEquals(transformable, filtered);
            }
            else {
                assertFalse(filtered.iterator().hasNext());
            }
        }));
    }

    @Test
    public void testFilterForMultipleElements() {
        withFilterFunc(f -> withItem(a -> withItem(b -> {
            final IntTransformable transformable = newIntBuilder().add(a).add(b).build();
            final IntTransformable filtered = transformable.filter(f);

            final boolean aPassed = f.apply(a);
            final boolean bPassed = f.apply(b);

            if (aPassed && bPassed) {
                assertEquals(transformable, filtered);
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
                assertFalse(filtered.iterator().hasNext());
            }
        })));
    }

    @Test
    public void testFilterNotWhenEmpty() {
        final IntPredicate f = unused -> {
            throw new AssertionError("This function should not be called");
        };

        assertFalse(newIntBuilder().build().filterNot(f).iterator().hasNext());
    }

    @Test
    public void testFilterNotForSingleElement() {
        withFilterFunc(f -> withItem(value -> {
            final IntTransformable transformable = newIntBuilder().add(value).build();
            final IntTransformable filtered = transformable.filterNot(f);

            if (f.apply(value)) {
                assertFalse(filtered.iterator().hasNext());
            }
            else {
                assertEquals(transformable, filtered);
            }
        }));
    }

    @Test
    public void testFilterNotForMultipleElements() {
        withFilterFunc(f -> withItem(a -> withItem(b -> {
            final IntTransformable transformable = newIntBuilder().add(a).add(b).build();
            final IntTransformable filtered = transformable.filterNot(f);

            final boolean aRemoved = f.apply(a);
            final boolean bRemoved = f.apply(b);

            if (aRemoved && bRemoved) {
                assertFalse(filtered.iterator().hasNext());
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
                assertEquals(transformable, filtered);
            }
        })));
    }
}

package sword.collections;

import junit.framework.TestCase;

import static sword.collections.SortUtils.equal;

abstract class TraverserTest<T> extends TestCase {

    abstract CollectionBuilder<T> newIterableBuilder();
    abstract void withValue(Procedure<T> value);
    abstract void withFilterFunc(Procedure<Predicate<T>> procedure);

    public void testContainsWhenEmpty() {
        withValue(value -> {
            if (newIterableBuilder().build().iterator().contains(value)) {
                fail("contains method is expected to return false always for any empty set. " +
                        "But returned true for " + value);
            }
        });
    }

    public void testContainsWhenContainingASingleElement() {
        withValue(valueIncluded -> {
            final IterableCollection<T> iterable = newIterableBuilder().add(valueIncluded).build();
            withValue(otherValue -> {
                final Traverser<T> traverser = iterable.iterator();
                if (equal(valueIncluded, otherValue) && !traverser.contains(otherValue)) {
                    fail("contains method is expected to return true when containing the value. But failing for value " + otherValue);
                }
                else if (!equal(valueIncluded, otherValue) && traverser.contains(otherValue)) {
                    fail("contains method is expected to return false when no containing the value. But failing for value " + otherValue + " while only containing " + valueIncluded);
                }
            });
        });
    }

    public void testContainsWhenContainingMultipleElements() {
        withValue(a -> withValue(b -> {
            final IterableCollection<T> iterable = newIterableBuilder().add(a).add(b).build();
            withValue(value -> {
                final Traverser<T> traverser = iterable.iterator();
                if ((equal(a, value) || equal(b, value)) && !traverser.contains(value)) {
                    fail("contains method is expected to return true when containing the value. But failing for value " + value + " while containing " + a + " and " + b);
                }
                else if (!equal(a, value) && !equal(b, value) && traverser.contains(value)) {
                    fail("contains method is expected to return false when no containing the value. But failing for value " + value + " while containing " + a + " and " + b);
                }
            });
        }));
    }

    public void testAnyMatchWhenEmpty() {
        final IterableCollection<T> iterable = newIterableBuilder().build();
        withFilterFunc(f -> assertFalse(iterable.iterator().anyMatch(f)));
    }

    public void testAnyMatchForSingleElement() {
        withValue(value -> {
            final IterableCollection<T> iterable = newIterableBuilder().add(value).build();
            withFilterFunc(f -> {
                final Traverser<T> traverser = iterable.iterator();
                if (f.apply(value)) {
                    assertTrue(traverser.anyMatch(f));
                }
                else {
                    assertFalse(traverser.anyMatch(f));
                }
            });
        });
    }

    public void testAnyMatchForMultipleElements() {
        withValue(a -> withValue(b -> {
            final IterableCollection<T> iterable = newIterableBuilder().add(a).add(b).build();
            withFilterFunc(f -> {
                final Traverser<T> traverser = iterable.iterator();
                if (f.apply(a) || f.apply(b)) {
                    assertTrue(traverser.anyMatch(f));
                }
                else {
                    assertFalse(traverser.anyMatch(f));
                }
            });
        }));
    }
}

package sword.collections;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractImmutableTransformableTest<T> extends AbstractTransformableTest<T> implements ImmutableTransformableTest<T> {

    abstract <E> AbstractTraversable<E> emptyCollection();

    @Override
    void assertEmptyCollection(Transformable<T> collection) {
        assertSame(emptyCollection(), collection);
    }

    @Override
    void assertNotChanged(Object expected, Object given) {
        assertSame(expected, given);
    }
}

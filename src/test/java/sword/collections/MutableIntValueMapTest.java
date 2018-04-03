package sword.collections;

public final class MutableIntValueMapTest extends IntValueMapTest {

    @Override
    IntValueMapBuilder<String> newBuilder() {
        return new MutableIntValueMap.Builder<>();
    }
}

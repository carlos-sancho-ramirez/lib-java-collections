package sword.collections;

public final class MutableIntTreeSet implements IterableIntCollection, Sizable {
    private Node _root;

    public boolean contains(int value) {
        return _root != null && _root.contains(value);
    }

    @Override
    public boolean anyMatch(IntPredicate predicate) {
        return iterator().anyMatch(predicate);
    }

    @Override
    public int indexOf(int value) {
        return iterator().indexOf(value);
    }

    public int min() {
        if (_root == null) {
            throw new EmptyCollectionException();
        }

        Node node = _root;
        while (node.left != null) {
            node = node.left;
        }

        return node.key;
    }

    public int max() {
        if (_root == null) {
            throw new EmptyCollectionException();
        }

        Node node = _root;
        while (node.right != null) {
            node = node.right;
        }

        return node.key;
    }

    public boolean add(int value) {
        if (_root == null) {
            _root = new Node(value);
            return true;
        }

        return _root.add(value);
    }

    @Override
    public int size() {
        return (_root == null)? 0 : _root.size;
    }

    @Override
    public boolean isEmpty() {
        return _root == null;
    }

    public ImmutableIntSet toImmutable() {
        final int min = min();
        final int max = max();
        final int size = size();

        if (size >= 3 && max - min + 1 == size) {
            return new ImmutableIntRange(min, max);
        }
        // TODO: Implement bit set conversion
        else {
            final int[] values = new int[size];
            for (int i = 0; i < size; i++) {
                values[i] = valueAt(i);
            }
            return new ImmutableIntSetImpl(values);
        }
    }

    public int valueAt(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        return _root.valueAt(index);
    }

    @Override
    public int findFirst(IntPredicate predicate, int defaultValue) {
        return iterator().findFirst(predicate, defaultValue);
    }

    @Override
    public int reduce(IntReduceFunction func) throws EmptyCollectionException {
        return iterator().reduce(func);
    }

    @Override
    public int reduce(IntReduceFunction func, int defaultValue) {
        return iterator().reduce(func, defaultValue);
    }

    @Override
    public Iterator iterator() {
        return new Iterator(_root);
    }

    static class Iterator implements java.util.Iterator<Integer> {
        private Node _node;
        private Iterator _it;

        Iterator(Node node) {
            _node = node;
            if (node != null && node.left != null) {
                _it = new Iterator(node.left);
            }
        }

        @Override
        public boolean hasNext() {
            return _it != null || _node != null;
        }

        @Override
        public Integer next() {
            if (_it != null) {
                final int value = _it.next();
                if (!_it.hasNext()) {
                    _it = null;
                }
                return value;
            }

            if (_node.right != null) {
                _it = new Iterator(_node.right);
            }

            final int value = _node.key;
            _node = null;
            return value;
        }

        boolean anyMatch(IntPredicate predicate) {
            while (hasNext()) {
                if (predicate.apply(next())) {
                    return true;
                }
            }

            return false;
        }

        int findFirst(IntPredicate predicate, int defaultValue) {
            while (hasNext()) {
                final int value = next();
                if (predicate.apply(value)) {
                    return value;
                }
            }

            return defaultValue;
        }

        int indexOf(int value) {
            for (int index = 0; hasNext(); index++) {
                if (next() == value) {
                    return index;
                }
            }

            return -1;
        }

        private int reduceSecured(IntReduceFunction func) {
            int result = next();
            while (hasNext()) {
                result = func.apply(result, next());
            }

            return result;
        }

        public int reduce(IntReduceFunction func) throws EmptyCollectionException {
            if (!hasNext()) {
                throw new EmptyCollectionException();
            }

            return reduceSecured(func);
        }

        public int reduce(IntReduceFunction func, int defaultValue) {
            return hasNext()? reduceSecured(func) : defaultValue;
        }
    }

    public static final class Builder implements IntCollectionBuilder<MutableIntTreeSet> {
        private final MutableIntTreeSet _set = new MutableIntTreeSet();

        @Override
        public Builder add(int value) {
            _set.add(value);
            return this;
        }

        @Override
        public MutableIntTreeSet build() {
            return _set;
        }
    }

    static final class Node {
        int key;
        int size = 1;
        Node left;
        Node right;

        Node(int key) {
            this.key = key;
        }

        public boolean contains(int value) {
            return value == key || value < key && left != null && left.contains(value) ||
                    right != null && right.contains(value);
        }

        int valueAt(int index) {
            final int leftSize = (left != null)? left.size : 0;
            return (index < leftSize)? left.valueAt(index) :
                    (index == leftSize)? key : right.valueAt(index - leftSize - 1);
        }

        public boolean add(int value) {
            final boolean added;
            if (value == key) {
                added = false;
            }
            else if (value < key) {
                if (left == null) {
                    left = new Node(value);
                    added = true;
                }
                else {
                    added = left.add(value);
                }
            }
            else {
                if (right == null) {
                    right = new Node(value);
                    added = true;
                }
                else {
                    added = right.add(value);
                }
            }

            if (added) {
                size++;
            }
            return added;
        }
    }
}

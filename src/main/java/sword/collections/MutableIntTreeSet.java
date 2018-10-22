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

    static class Iterator implements IntTraverser {
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

        @Override
        public boolean anyMatch(IntPredicate predicate) {
            while (hasNext()) {
                if (predicate.apply(next())) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public int findFirst(IntPredicate predicate, int defaultValue) {
            while (hasNext()) {
                final int value = next();
                if (predicate.apply(value)) {
                    return value;
                }
            }

            return defaultValue;
        }

        @Override
        public int indexOf(int value) {
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

        @Override
        public int reduce(IntReduceFunction func) throws EmptyCollectionException {
            if (!hasNext()) {
                throw new EmptyCollectionException();
            }

            return reduceSecured(func);
        }

        @Override
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
                    value > key && right != null && right.contains(value);
        }

        int valueAt(int index) {
            final int leftSize = (left != null)? left.size : 0;
            return (index < leftSize)? left.valueAt(index) :
                    (index == leftSize)? key : right.valueAt(index - leftSize - 1);
        }

        private int replaceLeftNotContained(int value) {
            if (value < key) {
                return (left == null)? value : left.replaceLeftNotContained(key);
            }
            else {
                final int toReturn = (left == null)? key : left.replaceLeftNotContained(key);
                key = (right == null)? value : right.replaceLeftNotContained(value);
                return toReturn;
            }
        }

        // Include the given value within the tree without creating a new node.
        // This method should find the maximum value that the tree starting within this node is containing
        // and remove it to allow space for the given value. The maximum value removed has to be returned
        // in order to be stored outside this tree.
        private int replaceRightNotContained(int value) {
            if (value > key) {
                return (right == null)? value : right.replaceRightNotContained(value);
            }
            else {
                final int toReturn = (right == null)? key : right.replaceRightNotContained(key);
                key = (left == null)? value : left.replaceRightNotContained(value);
                return toReturn;
            }
        }

        private void addNotContained(int value) {
            if (value < key) {
                if (left == null) {
                    left = new Node(value);
                }
                else if (right == null) {
                    right = new Node(key);
                    key = left.replaceRightNotContained(value);
                }
                else {
                    if (right.size < left.size) {
                        right.addNotContained(key);
                        key = left.replaceRightNotContained(value);
                    }
                    else {
                        left.addNotContained(value);
                    }
                }
            }
            else {
                if (right == null) {
                    right = new Node(value);
                }
                else if (left == null) {
                    left = new Node(key);
                    key = right.replaceLeftNotContained(value);
                }
                else {
                    if (left.size < right.size) {
                        left.addNotContained(key);
                        key = right.replaceLeftNotContained(value);
                    }
                    else {
                        right.addNotContained(value);
                    }
                }
            }

            size++;
        }

        public boolean add(int value) {
            if (contains(value)) {
                return false;
            }
            else {
                addNotContained(value);
                return true;
            }
        }
    }
}
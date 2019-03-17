package sword.collections;

public final class MutableIntTreeSet extends AbstractIntTraversable implements MutableIntSet {
    private Node _root;

    public boolean contains(int value) {
        return _root != null && _root.contains(value);
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

    @Override
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

    @Override
    public MutableIntTreeSet mutate() {
        final MutableIntTreeSet newSet = new MutableIntTreeSet();
        if (_root != null) {
            newSet._root = _root.mutate();
        }

        return newSet;
    }

    public int valueAt(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        return _root.valueAt(index);
    }

    @Override
    public Iterator iterator() {
        return new Iterator(_root);
    }

    @Override
    public IntList toList() {
        return iterator().toList();
    }

    @Override
    public <E> IntKeyMap<E> assign(IntFunction<E> function) {
        final int size = size();
        if (size == 0) {
            return ImmutableIntKeyMap.empty();
        }

        final int[] keys = new int[size];
        final Object[] values = new Object[size];

        final IntTraverser it = iterator();
        for (int i = 0; it.hasNext(); i++) {
            final int key = it.next();
            keys[i] = key;
            values[i] = function.apply(key);
        }

        return new ImmutableIntKeyMap<>(keys, values);
    }

    @Override
    public IntPairMap assignToInt(IntToIntFunction function) {
        final int size = size();
        if (size == 0) {
            return ImmutableIntPairMap.empty();
        }

        final int[] keys = new int[size];
        final int[] values = new int[size];

        final IntTraverser it = iterator();
        for (int i = 0; it.hasNext(); i++) {
            final int key = it.next();
            keys[i] = key;
            values[i] = function.apply(key);
        }

        return new ImmutableIntPairMap(keys, values);
    }

    @Override
    public void removeAt(int index) throws IndexOutOfBoundsException {
        if (_root == null || index >= _root.size) {
            throw new IndexOutOfBoundsException();
        }

        if (_root.size == 1) {
            _root = null;
        }
        else {
            _root.removeAt(index);
        }
    }

    @Override
    public boolean clear() {
        final boolean changed = _root != null;
        _root = null;
        return changed;
    }

    static class Iterator extends AbstractIntTransformer {
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
    }

    public static final class Builder implements MutableIntSet.Builder {
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

        private int pullMin() {
            final int leftSize = (left != null)? left.size : 0;
            final int toReturn;
            if (leftSize == 0) {
                toReturn = key;
                if (right.size == 1) {
                    key = right.key;
                    right = null;
                }
                else {
                    key = right.pullMin();
                }
            }
            else if (leftSize == 1) {
                toReturn = left.key;
                left = null;
            }
            else {
                toReturn = left.pullMin();
            }

            size--;
            return toReturn;
        }

        private int pullMax() {
            final int rightSize = (right != null)? right.size : 0;
            final int toReturn;
            if (rightSize == 0) {
                toReturn = key;
                if (left.size == 1) {
                    key = left.key;
                    left = null;
                }
                else {
                    key = left.pullMax();
                }
            }
            else if (rightSize == 1) {
                toReturn = right.key;
                right = null;
            }
            else {
                toReturn = right.pullMax();
            }

            size--;
            return toReturn;
        }

        void removeAt(int index) {
            final int leftSize = (left != null)? left.size : 0;
            if (index == leftSize) {
                final int rightSize = (right != null)? right.size : 0;
                if (leftSize < rightSize) {
                    if (rightSize == 1) {
                        key = right.key;
                        right = null;
                    }
                    else {
                        key = right.pullMin();
                    }
                }
                else {
                    if (leftSize == 1) {
                        key = left.key;
                        left = null;
                    }
                    else {
                        key = left.pullMax();
                    }
                }
            }
            else if (index < leftSize) {
                if (left.size == 1) {
                    left = null;
                }
                else {
                    left.removeAt(index);
                }
            }
            else {
                if (right.size == 1) {
                    right = null;
                }
                else {
                    right.removeAt(index - leftSize - 1);
                }
            }

            size--;
        }

        Node mutate() {
            final Node newNode = new Node(key);
            newNode.size = size;

            if (left != null) {
                newNode.left = left.mutate();
            }

            if (right != null) {
                newNode.right = right.mutate();
            }

            return newNode;
        }
    }
}

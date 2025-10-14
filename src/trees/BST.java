package trees;

import java.util.*;

public class BST<T extends Comparable<T>> {
    protected int nodeCount;
    protected BSTNode<T> root;

    public BST() {
        this.nodeCount = 0;
        this.root = null;
    }

    public BSTNode<T> createNewNode(T value) {
        return new BSTNode<>(value);
    }

    public BSTNode<T> insert(T node) {
        BSTNode<T> newNode = createNewNode(node);
        if (this.root == null) {
            this.setRoot(newNode);
            this.nodeCount++;
            return this.root;
        }

        BSTNode<T> current = this.root;
        BSTNode<T> parent = null;
        while (current != null) {
            parent = current;
            int cmp = node.compareTo(current.getValue());
            if (cmp < 0) {
                current = current.getLeftSon();
            } else if (cmp > 0) {
                current = current.getRightSon();
            } else {
                return null;
            }
        }
        if (node.compareTo(parent.getValue()) < 0) {
            parent.setLeftSon(newNode);
        } else {
            parent.setRightSon(newNode);
        }
        newNode.setParent(parent);
        this.nodeCount++;
        return newNode;
    }

    public BSTNode<T> search(T node) {
        BSTNode<T> current = this.root;
        while (current != null) {
            int cmp = node.compareTo(current.getValue());
            if (cmp == 0) {
                return current;
            } else if (cmp < 0) {
                current = current.getLeftSon();
            } else {
                current = current.getRightSon();
            }
        }
        return null;
    }

    public boolean delete(T value) {
        BSTNode<T> current = this.root;
        while (current != null) {
            int cmp = value.compareTo(current.getValue());
            if (cmp < 0) {
                current = current.getLeftSon();
            } else if (cmp > 0) {
                current = current.getRightSon();
            } else {
                break;
            }
        }
        if (current == null) return false;

        if (current.getLeftSon() != null && current.getRightSon() != null) {
            BSTNode<T> successor = current.getRightSon();
            while (successor.getLeftSon() != null) {
                successor = successor.getLeftSon();
            }
            current.setValue(successor.getValue());
            current = successor;
        }

        BSTNode<T> replacement = (current.getLeftSon() != null)
                ? current.getLeftSon()
                : current.getRightSon();

        replaceNode(current, replacement);

        nodeCount--;
        return true;
    }

    public void replaceNode(BSTNode<T> target, BSTNode<T> replacement) {
        if (target.getParent() == null) {
            root = replacement;
        }
        else if (target == target.getParent().getLeftSon()) {
            target.getParent().setLeftSon(replacement);
        }
        else {
            target.getParent().setRightSon(replacement);
        }
        if (replacement != null) {
            replacement.setParent(target.getParent());
        }
    }

    public void setRoot(BSTNode<T> root) {
        this.root = root;
    }

    public List<T> intervalSearch(T low, T high) {
        List<T> res = new ArrayList<>();
        Deque<BSTNode<T>> queue = new ArrayDeque<>();
        BSTNode<T> current = this.root;
        while(current != null || !queue.isEmpty()) {
            while(current != null) {
                queue.push(current);
                current = current.getLeftSon();
            }
            current = queue.pop();
            T val = current.getValue();

            if (val.compareTo(low) >= 0 && val.compareTo(high) <= 0) {
                res.add(val);
            }

            if (val.compareTo(high) > 0) {
                break;
            }

            current = current.getRightSon();
        }
        return res;
    }

}

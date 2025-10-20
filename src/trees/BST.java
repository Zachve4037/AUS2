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

    public boolean insert(T node) {
        BSTNode<T> newNode = createNewNode(node);
        if (this.root == null) {
            this.setRoot(newNode);
            this.nodeCount++;
            return true;
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
                return false;
            }
        }
        if (node.compareTo(parent.getValue()) < 0) {
            parent.setLeftSon(newNode);
        } else {
            parent.setRightSon(newNode);
        }
        newNode.setParent(parent);
        this.nodeCount++;
        return true;
    }

    public T search(T node) {
        BSTNode<T> current = this.root;
        while (current != null) {
            int cmp = node.compareTo(current.getValue());
            if (cmp == 0) {
                return current.getValue();
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
        if (replacement != null) {
            replacement.setParent(target.getParent());
        }
        if (target.getParent() == null) {
            root = replacement;
        }
        else if (target == target.getParent().getLeftSon()) {
            target.getParent().setLeftSon(replacement);
        }
        else {
            target.getParent().setRightSon(replacement);
        }
    }

    public void setRoot(BSTNode<T> root) {
        this.root = root;
    }

    public List<T> intervalSearch(T low, T high) {
        List<T> res = new LinkedList<>();
        List<BSTNode<T>> stack = new LinkedList<>();
        BSTNode<T> current = this.root;
        while(current != null || !stack.isEmpty()) {
            while(current != null) {
                stack.add(current);
                current = current.getLeftSon();
            }
            current = stack.removeLast();
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

    public List<T> inOrder(boolean print) {
        List<T> res = new LinkedList<>();
        for (BSTNode<T> val : innerInOrder(this.root)) {
            if (print) {
                System.out.println(val.getValue());
            }
            res.add(val.getValue());
        }
        return res;
    }

    protected List<BSTNode<T>> innerInOrder(BSTNode<T> startNode) {
        List<BSTNode<T>> res = new LinkedList<>();
        List<BSTNode<T>> stack = new LinkedList<>();
        BSTNode<T> current = startNode;
        while (current != null || !stack.isEmpty()) {
            while(current != null) {
                stack.add(current);
                current = current.getLeftSon();
            }
            current = stack.removeLast();
            res.add(current);
            current = current.getRightSon();
        }
        return res;
    }

    public T findMin(BSTNode<T> startNode) {
        if (startNode == null) {
            return null;
        } else if (startNode.getLeftSon() == null) {
            return startNode.getValue();
        } else {
            BSTNode<T> current = startNode.getLeftSon();
            while (current.getLeftSon() != null) {
                current = current.getLeftSon();
            }
            return current.getValue();
        }
    }

    public T findMax(BSTNode<T> startNode) {
        if (startNode == null) {
            return null;
        } else if (startNode.getRightSon() == null) {
            return startNode.getValue();
        } else {
            BSTNode<T> current = startNode.getRightSon();
            while (current.getRightSon() != null) {
                current = current.getRightSon();
            }
            return current.getValue();
        }
    }

    public BSTNode<T> getRoot() {
        return this.root;
    }
}

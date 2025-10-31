package trees.avl;

import trees.BSTree;

public class AVLTree<T extends Comparable<T>> extends BSTree<T> {

    public AVLTree() {
        super();
    }

    @Override
    public AVLNode<T> createNewNode(T value) {
        return new AVLNode<>(value);
    }

    @Override
    public T insert(T value) {
        AVLNode<T> newNode = createNewNode(value);
        if (this.root == null) {
            this.setRoot(newNode);
            this.nodeCount++;
            return this.root.getValue();
        }

        AVLNode<T> current = (AVLNode<T>) this.root;
        AVLNode<T> parent = null;

        while (current != null) {
            parent = current;
            int cmp = value.compareTo(current.getValue());
            if (cmp < 0) {
                current = (AVLNode<T>)current.getLeftSon();
            } else if (cmp > 0) {
                current = (AVLNode<T>)current.getRightSon();
            } else {
                return null;
            }
        }

        if (value.compareTo(parent.getValue()) < 0) {
            parent.setLeftSon(newNode);
        } else {
            parent.setRightSon(newNode);
        }
        newNode.setParent(parent);
        this.nodeCount++;
        rebalance(parent, true);
        return newNode.getValue();
    }

    @Override
    public T delete(T value) {
        AVLNode<T> current = (AVLNode<T>) this.root;
        while (current != null) {
            int cmp = value.compareTo(current.getValue());
            if (cmp < 0) {
                current = (AVLNode<T>) current.getLeftSon();
            } else if (cmp > 0){
                current = (AVLNode<T>) current.getRightSon();
            } else {
                break;
            }
        }
        if (current == null) return null;

        if (current.getLeftSon() != null && current.getRightSon() != null) {
            AVLNode<T> successor = (AVLNode<T>) current.getRightSon();
            while (successor.getLeftSon() != null) {
                successor = (AVLNode<T>) successor.getLeftSon();
            }
            current.setValue(successor.getValue());
            current = successor;
        }

        AVLNode<T> replacement = (current.getLeftSon() != null)
                ? (AVLNode<T>) current.getLeftSon()
                : (AVLNode<T>) current.getRightSon();

        AVLNode<T> parent = (AVLNode<T>) current.getParent();
        T deleted = current.getValue();
        super.replaceNode(current, replacement);
        nodeCount--;
        rebalance(parent, false);
        return deleted;
    }

    private void rebalance(AVLNode<T> node, boolean isInsert) {
        while (node != null) {
            int oldFac = balanceFactor(node);
            updateHeight(node);
            int balFac = balanceFactor(node);
            if (oldFac == 0 && balFac != 0 && !isInsert) {
                return;
            }
            if (balFac > 1) {
                if (height((AVLNode<T>) node.getLeftSon().getLeftSon()) >= height((AVLNode<T>) node.getLeftSon().getRightSon())) {
                    rotateRight((AVLNode<T>) node.getLeftSon());
                } else {
                    rotateLeftRight((AVLNode<T>) node.getLeftSon().getRightSon());
                }
            }
            if (balFac < -1) {
                if (height((AVLNode<T>) node.getRightSon().getRightSon()) >= height((AVLNode<T>) node.getRightSon().getLeftSon())) {
                    rotateLeft((AVLNode<T>) node.getRightSon());
                } else {
                    rotateRightLeft((AVLNode<T>) node.getRightSon().getLeftSon());
                }
            }
            node = (AVLNode<T>) node.getParent();
        }
    }

    private int height(AVLNode<T> node) {
        return node == null ? 0 : node.getHeight();
    }

    private int balanceFactor(AVLNode<T> node) {
        if (node == null) return 0;
        return (height((AVLNode<T>) node.getLeftSon()) - height((AVLNode<T>) node.getRightSon()));
    }

    private void updateHeight(AVLNode<T> node) {
        int leftHeight = (node.getLeftSon() == null) ? 0 : ((AVLNode<T>) node.getLeftSon()).getHeight();
        int rightHeight = (node.getRightSon() == null) ? 0 : ((AVLNode<T>) node.getRightSon()).getHeight();

        node.setHeight(1 + Math.max(leftHeight, rightHeight));
    }

    private void replaceChild(AVLNode<T> parent, AVLNode<T> old, AVLNode<T> neew) {
        if (parent == null) {
            this.root = neew;
            if (neew != null) {
                neew.setParent(null);
            }
        } else {
            if (neew != null) {
                neew.setParent(parent);
            }

            if (parent.getRightSon() == old) {
                parent.setRightSon(neew);
            } else {
                parent.setLeftSon(neew);
            }
        }
    }

    private void rotateRight(AVLNode<T> current) {
        if (current == null) return;
        AVLNode<T> parent = (AVLNode<T>) current.getParent();
        if (parent == null) return;

        AVLNode<T> grandParent = (AVLNode<T>)parent.getParent();

        AVLNode<T> c_r = (AVLNode<T>)current.getRightSon();
        if (c_r != null) {
            c_r.setParent(parent);
        }

        parent.setLeftSon(c_r);
        current.setRightSon(parent);
        parent.setParent(current);

        this.replaceChild(grandParent, parent, current);
        updateHeight(parent);
        updateHeight(current);
    }

    private void rotateLeft(AVLNode<T> current) {
        if (current == null) return;
        AVLNode<T> parent = (AVLNode<T>) current.getParent();
        if (parent == null) return;

        AVLNode<T> grandParent = (AVLNode<T>) parent.getParent();

        AVLNode<T> c_l = (AVLNode<T>)current.getLeftSon();
        if (c_l != null) {
            c_l.setParent(parent);
        }

        parent.setRightSon(c_l);
        current.setLeftSon(parent);
        parent.setParent(current);

        this.replaceChild(grandParent, parent, current);
        updateHeight(parent);
        updateHeight(current);
    }

    private void rotateLeftRight(AVLNode<T> current) {
        if (current == null) return;

        rotateLeft(current);
        rotateRight(current);
    }

    private void rotateRightLeft(AVLNode<T> current) {
        if (current == null) return;

        rotateRight(current);
        rotateLeft(current);
    }
}

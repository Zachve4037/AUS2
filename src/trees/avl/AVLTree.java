package trees.avl;

import trees.BST;

public class AVLTree<T extends Comparable<T>> extends BST<T> {

    public AVLTree() {
        super();
    }

    private void replaceChild(AVLNode<T> parent, AVLNode<T> old, AVLNode<T> neew) {
        if (parent == null) {
            this.root = neew;
            if (neew != null) neew.setParent(null);
        } else {
            if (neew != null) neew.setParent(parent);

            if (parent.getRightSon() == old) {
                parent.setRightSon(neew);
            } else {
                parent.setLeftSon(neew);
            }
        }
    }

    public void rotateRight(AVLNode<T> current) {
        if (current == null) return;
        AVLNode<T> parent = (AVLNode<T>) current.getParent();
        if (parent == null) return;

        AVLNode<T> grandParent = (AVLNode<T>)parent.getParent();

        AVLNode<T> c_r = (AVLNode<T>)current.getRightSon();
        if (c_r != null) c_r.setParent(parent);

        parent.setLeftSon(c_r);
        current.setRightSon(parent);
        parent.setParent(current);

        this.replaceChild(grandParent, parent, current);
    }

    public void rotateLeft(AVLNode<T> current) {
        if (current == null) return;
        AVLNode<T> parent = (AVLNode<T>) current.getParent();
        if (parent == null) return;

        AVLNode<T> grandParent = (AVLNode<T>) parent.getParent();

        AVLNode<T> c_l = (AVLNode<T>)current.getLeftSon();
        if (c_l != null) c_l.setParent(parent);

        parent.setRightSon(c_l);
        current.setLeftSon(parent);
        parent.setParent(current);

        this.replaceChild(grandParent, parent, current);
    }

    public void rotateLeftRight(AVLNode<T> current) {
        if (current == null) return;

        AVLNode<T> parent = (AVLNode<T>) current.getParent();
        if (parent == null) return;

        AVLNode<T> grandParent = (AVLNode<T>) parent.getParent();
        if (grandParent == null) return;

        rotateLeft(current);
        rotateRight(current);
    }

    public void rotateRightLeft(AVLNode<T> current) {
        if (current == null) return;

        AVLNode<T> parent = (AVLNode<T>) current.getParent();
        if (parent == null) return;

        AVLNode<T> grandParent = (AVLNode<T>) parent.getParent();
        if (grandParent == null) return;

        rotateRight(current);
        rotateLeft(current);
    }

    @Override
    public AVLNode<T> createNewNode(T value) {
        return new AVLNode<>(value);
    }
}

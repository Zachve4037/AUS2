package trees.avl;

import trees.BST;

public class AVLTree<T extends Comparable<T>> extends BST<T> {

    public AVLTree() {
        super();
    }

    public void rotateRight(AVLNode<T> node) {
        AVLNode<T> current = node;
        AVLNode<T> parent = (AVLNode<T>)current.getParent();
        AVLNode<T> grandParent = (AVLNode<T>)parent.getParent();
        if (grandParent == null) {
            this.root = current;
            current.setRightSon(parent);
            parent.setParent(current);
        } else {
            grandParent.setLeftSon(current);
            current.setRightSon(parent);
            parent.setParent(current);
        }
    }

    public void rotateLeft(AVLNode<T> node) {
        AVLNode<T> current = node;
        AVLNode<T> parent = (AVLNode<T>) current.getParent();
        AVLNode<T> grandParent = (AVLNode<T>) parent.getParent();
        if (grandParent == null) {
            this.root = current;
            current.setLeftSon(parent);
            parent.setParent(current);
        } else {
            grandParent.setRightSon(current);
            current.setRightSon(parent);
            parent.setParent(current);
        }
    }
}

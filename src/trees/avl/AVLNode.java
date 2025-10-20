package trees.avl;

import trees.BSTNode;

public class AVLNode<T extends Comparable<T>> extends BSTNode<T> {
    private int height;

    public AVLNode(T value) {
        super(value);
        this.height = 1;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}

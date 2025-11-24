package S1.trees;

public class BSTNode<T extends Comparable<T>> {
    private T value;
    private BSTNode<T> leftSon;
    private BSTNode<T> rightSon;
    private BSTNode<T> parent;

    public BSTNode(T value) {
        this.value = value;
        this.leftSon = null;
        this.rightSon = null;
        this.parent = null;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public void setLeftSon(BSTNode<T> leftSon) {
        this.leftSon = leftSon;
    }

    public void setRightSon(BSTNode<T> rightSon) {
        this.rightSon = rightSon;
    }

    public void setParent(BSTNode<T> parent) {
        this.parent = parent;
    }

    public T getValue() {
        return this.value;
    }

    public BSTNode<T> getLeftSon() {
        return this.leftSon;
    }

    public BSTNode<T> getRightSon() {
        return this.rightSon;
    }

    public BSTNode<T> getParent() {
        return this.parent;
    }
}

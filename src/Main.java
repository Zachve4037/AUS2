import trees.BST;
import trees.avl.AVLNode;
import trees.avl.AVLTree;

void main() {
    AVLTree<Integer> avl = new AVLTree<>();
    avl.insert(30);
    avl.insert(29);
    avl.insert(20);
    AVLNode<Integer> test = (AVLNode<Integer>) avl.innerInsert(25);
    avl.insert(22);
    avl.insert(26);
    avl.insert(19);

    avl.inOrder();

    avl.rotateLeftRight(test);

    avl.inOrder();
}
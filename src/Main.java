import tester.Tester;
import trees.BST;
import trees.avl.AVLNode;
import trees.avl.AVLTree;

public class Main {
    public static void main(String[] args) {
//    AVLTree<Integer> avl = new AVLTree<>();
//    avl.insert(30);
//    avl.insert(29);
//    avl.insert(20);
//    AVLNode<Integer> test = (AVLNode<Integer>) avl.innerInsert(25);
//    avl.insert(22);
//    avl.insert(26);
//    avl.insert(19);
//
//    avl.inOrder();
//
//    avl.rotateLeftRight(test);
//
//    avl.inOrder();
        Tester tester = new Tester();
        tester.testBst();
    }
}
import tester.Tester;
import trees.BSTNode;
import trees.BSTree;
import trees.avl.AVLTree;

public class Main {
    public static void main() {
        Tester teste = new Tester();
        teste.testAvl();
//        teste.testAvlBstInsert();
//        teste.testAvlBstDelete();
//        teste.testAvlBstSearch();
//        teste.testAvlBstInterval();
//        teste.testAvlBstMinMax();
//        AVLTree<Integer> avlTree = new AVLTree<>();
//        avlTree.insert(2099829013);
//        avlTree.insert(1983575708);
//        avlTree.insert(1193001495);
//        avlTree.insert(1366102330);
//        avlTree.insert(1394982712);
//        avlTree.delete(1983575708);
//        avlTree.delete(1366102330);
//        printTreeWithBalance(avlTree);
    }

    private static <T extends Comparable<T>> void printTreeWithBalance(BSTree<T> tree) {
        printSubtreeWithBalance(tree.getRoot(), "", true);
        System.out.println("-----------------------");
    }

    private static <T extends Comparable<T>> int nodeHeight(BSTNode<T> node) {
        if (node == null) return 0;
        if (node instanceof trees.avl.AVLNode) {
            return ((trees.avl.AVLNode<T>) node).getHeight();
        }
        return 1 + Math.max(nodeHeight(node.getLeftSon()), nodeHeight(node.getRightSon()));
    }

    private static <T extends Comparable<T>> void printSubtreeWithBalance(BSTNode<T> node, String prefix, boolean isTail) {
        if (node == null) return;
        BSTNode<T> right = node.getRightSon();
        if (right != null) {
            printSubtreeWithBalance(right, prefix + (isTail ? "    " : "│   "), false);
        }
        int leftH = nodeHeight(node.getLeftSon());
        int rightH = nodeHeight(node.getRightSon());
        int h = 1 + Math.max(leftH, rightH);
        int balance = leftH - rightH;
        System.out.println(prefix + (isTail ? "└── " : "├── ") + node.getValue() + " (h=" + h + ", b=" + balance + ")");
        BSTNode<T> left = node.getLeftSon();
        if (left != null) {
            printSubtreeWithBalance(left, prefix + (isTail ? "    " : "│   "), true);
        }
    }
}
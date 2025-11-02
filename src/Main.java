import tester.Tester;
import trees.BSTNode;
import trees.BSTree;
import who.gui.DatabaseGUI;
import who.wrappers.PCRT_DateTime;

import javax.swing.*;

public class Main {
    public static void main() {
        SwingUtilities.invokeLater(() -> {
            DatabaseGUI gui = new DatabaseGUI();
            gui.setVisible(true);
        });
//        Tester teste = new Tester();
//        teste.testAvl();
//        teste.testAvlBstInsert();
//        teste.testAvlBstDelete();
//        teste.testAvlBstSearch();
//        teste.testAvlBstInterval();
//        teste.testAvlBstMinMax();
//        teste.testBstAvlIncrement();
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
}
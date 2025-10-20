package tester;

import trees.BST;
import trees.BSTNode;
import trees.avl.AVLNode;
import trees.avl.AVLTree;

import java.util.*;

public class Tester {
    private final int OPERATIONS = 10_000_000;
    private final int INSERT = OPERATIONS;
    private final int DELETE = 2_000_000;
    private final int SEARCH = 5_000_000;
    private final int INTERVAL = 1_000_000;
    private final int MINMAX = 2_000_000;

    public Tester() {
    }

    public void testAvlBst() {
        AVLTree<Integer> avl = new AVLTree<>();
        BST<Integer> bst = new BST<>();


    }

    public void testBst() {
        for (int j = 0; j < 50; j++) {
            BST<Integer> bst = new BST<>();
            ArrayList<Integer> pom = new ArrayList<>();
            Random rng = new Random(j);
            for (int i = 0; i < OPERATIONS; i++) {
                int operation = rng.nextInt(100);
                if (operation < 40) {
                    int val = rng.nextInt();
                    if (bst.insert(val)) {
                        pom.add(val);
                    }
                } else{
                    if (!pom.isEmpty()) {
                        int index = rng.nextInt(pom.size());
                        int val = pom.get(index);
                        if (operation < 80) {
                            boolean res = bst.delete(val);
                            if (!res) {
                                throw new AssertionError("error at " + j);
                            }
                            pom.remove(index);
                        } else {
                            Integer res = bst.search(val);
                            if (res == null || !res.equals(val)) {
                                throw new AssertionError("error at " + j);
                            }
                        }
                    }
                }
            }
            if (!haveSameElements(pom, bst.inOrder(false))) {
                throw new AssertionError("error at " + j);
            }
            System.out.println("Finished operations for test " + j);
        }
    }

    public void testAvl() {
        for (int j = 50; j < 150; j++) {
            AVLTree<Integer> avl = new AVLTree<>();
            ArrayList<Integer> pom = new ArrayList<>();
            Random rng = new Random(j);
            for (int i = 0; i < OPERATIONS; i++) {
                int operation = rng.nextInt(100);
                if (operation < 40) {
                    int val = rng.nextInt();
                    if (avl.insert(val)) {
                        pom.add(val);
                    }
                } else{
                    if (!pom.isEmpty()) {
                        int index = rng.nextInt(pom.size());
                        int val = pom.get(index);
                        if (operation < 80) {
                            boolean res = avl.delete(val);
                            if (!res) {
                                throw new AssertionError("error at " + j);
                            }
                            pom.remove(index);
                        } else {
                            Integer res = avl.search(val);
                            if (res == null || !res.equals(val)) {
                                throw new AssertionError("error at " + j);
                            }
                        }
                    }
                }
            }
            if (!haveSameElements(pom, avl.inOrder(false))) {
                throw new AssertionError("error at " + j);
            }
            validateNodeHeights(avl.getRoot());
            System.out.println("Finished operations for test " + j);
        }
    }

    public static boolean haveSameElements(ArrayList<Integer> list, List<Integer> bstIO) {

        if (list.size() != bstIO.size()) return false;

        List<Integer> sortedList = new ArrayList<>(list);
        sortedList.sort(Comparator.naturalOrder());

        Iterator<Integer> it1 = sortedList.iterator();
        Iterator<Integer> it2 = bstIO.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            Integer a = it1.next();
            Integer b = it2.next();
            if (!Objects.equals(a, b)) return false;
        }
        return !it1.hasNext() && !it2.hasNext();
    }

    private int validateNodeHeights(BSTNode<Integer> node) {
        if (node == null) return 0;
        int lh = validateNodeHeights(node.getLeftSon());
        int rh = validateNodeHeights(node.getRightSon());
        int computed = Math.max(lh, rh) + 1;

        if (node instanceof AVLNode<Integer>) {
            AVLNode<Integer> avlNode = (AVLNode<Integer>)node;
            if (avlNode.getHeight() != computed) {
                throw new AssertionError("Height mismatch at node " + node.getValue());
            }
        }

        int balance = lh - rh;
        if (balance < -1 || balance > 1) {
            throw new AssertionError("Balance factor violation at node " + node.getValue());
        }
        return computed;
    }
}

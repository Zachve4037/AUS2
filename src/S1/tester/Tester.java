package S1.tester;

import S2.HeapFile.HeapFile;
import S2.HeapFile.IData;
import S2.HeapFile.IRecord;
import S1.trees.BSTree;
import S1.trees.BSTNode;
import S1.trees.avl.AVLNode;
import S1.trees.avl.AVLTree;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;

public class Tester {
    private final int OPERATIONS = 10_000_000;
    private final int INSERT = 10_000_000;
    private final int DELETE = 2_000_000;
    private final int SEARCH = 5_000_000;
    private final int INTERVAL = 1_000_000;
    private final int MINMAX = 2_000_000;

    public Tester() {
    }

    public void testAvlBstInsert() {
        AVLTree<Integer> avl = new AVLTree<>();
        BSTree<Integer> bst = new BSTree<>();
        TreeSet<Integer> set = new TreeSet<>();

        long avlTime = 0L;
        long bstTime = 0L;
        long setTime = 0L;

        Random rng = new Random();
        for (int i = 0; i < INSERT; i++) {
            int val = rng.nextInt();

            long t0 = System.nanoTime();
            bst.insert(val);
            long t1 = System.nanoTime();
            bstTime += t1 - t0;

            long t2 = System.nanoTime();
            avl.insert(val);
            long t3 = System.nanoTime();
            avlTime += (t3 - t2);

            long t4 = System.nanoTime();
            set.add(val);
            long t5 = System.nanoTime();
            setTime += t5 - t4;
        }
        System.out.println("for bst insert " + bstTime / 1_000_000.0);
        System.out.println("for avl insert " + avlTime / 1_000_000.0);
        System.out.println("for treeSet insert " + setTime / 1_000_000.0);
    }

    public void testAvlBstDelete() {
        AVLTree<Integer> avl = new AVLTree<>();
        BSTree<Integer> bst = new BSTree<>();
        TreeSet<Integer> set = new TreeSet<>();
        ArrayList<Integer> pom = new ArrayList<>();
        Random rng = new Random();

        for (int i = 0; i < INSERT; i++) {
            int val = rng.nextInt();
            if (avl.insert(val) != null) {
                bst.insert(val);
                pom.add(val);
                set.add(val);
            }
        }

        Collections.shuffle(pom);
        long avlTime = 0L;
        long bstTime = 0L;
        long setTime = 0L;

        for (int i = 0; i < DELETE; i++) {
            int index = rng.nextInt(pom.size());
            int val = pom.get(index);

            long t0 = System.nanoTime();
            avl.delete(val);
            long t1 = System.nanoTime();
            avlTime += t1 - t0;

            long t2 = System.nanoTime();
            bst.delete(val);
            long t3 = System.nanoTime();
            bstTime += t3 - t2;
            pom.remove(index);

            long t4 = System.nanoTime();
            set.remove(val);
            long t5 = System.nanoTime();
            setTime += t5 - t4;
        }
        System.out.println("for bst delete " + bstTime / 1_000_000.0);
        System.out.println("for avl delete " + avlTime / 1_000_000.0);
        System.out.println("for treeSet delete " + setTime / 1_000_000.0);
    }

    public void testAvlBstSearch() {
        AVLTree<Integer> avl = new AVLTree<>();
        BSTree<Integer> bst = new BSTree<>();
        TreeSet<Integer> set = new TreeSet<>();
        ArrayList<Integer> pom = new ArrayList<>();
        Random rng = new Random();

        for (int i = 0; i < INSERT; i++) {
            int val = rng.nextInt();
            if (avl.insert(val) != null) {
                bst.insert(val);
                pom.add(val);
                set.add(val);
            }
        }

        long avlTime = 0L;
        long bstTime = 0L;
        long setTime = 0L;

        for (int i = 0; i < SEARCH; i++) {
            int index = rng.nextInt(pom.size());
            int val = pom.get(index);

            long t0 = System.nanoTime();
            bst.search(val);
            long t1 = System.nanoTime();
            bstTime += t1 - t0;

            long t2 = System.nanoTime();
            avl.search(val);
            long t3 = System.nanoTime();
            avlTime += t3 - t2;

            long t4 = System.nanoTime();
            set.contains(val);
            long t5 = System.nanoTime();
            setTime += t5 - t4;
        }

        System.out.println("for bst search " + bstTime / 1_000_000.0);
        System.out.println("for avl search " + avlTime / 1_000_000.0);
        System.out.println("for treeSet search " + setTime / 1_000_000.0);
    }

    public void testAvlBstInterval() {
        AVLTree<Integer> avl = new AVLTree<>();
        BSTree<Integer> bst = new BSTree<>();
        TreeSet<Integer> set = new TreeSet<>();
        ArrayList<Integer> pom = new ArrayList<>();
        Random rng = new Random();

        for (int i = 0; i < 100000; i++) {
            pom.add(i);
        }
        Collections.shuffle(pom);
        for (int i : pom) {
            avl.insert(i);
            bst.insert(i);
        }

        long avlTime = 0L;
        long bstTime = 0L;
        long setTime = 0L;

        for (int i = 0; i < INTERVAL; i++) {
            int a = rng.nextInt() % (pom.size()-500);
            int b = a + 500;

            long t0 = System.nanoTime();
            bst.intervalSearch(a, b);
            long t1 = System.nanoTime();
            bstTime += t1 - t0;

            long t2 = System.nanoTime();
            avl.intervalSearch(a, b);
            long t3 = System.nanoTime();
            avlTime += t3 - t2;

            long t4 = System.nanoTime();
            set.subSet(a, b);
            long t5 = System.nanoTime();
            setTime += t5 - t4;
        }

        System.out.println("for bst interval search " + bstTime / 1_000_000.0);
        System.out.println("for avl interval search " + avlTime / 1_000_000.0);
        System.out.println("for treeSet interval search " + setTime / 1_000_000.0);
    }

    public void testAvlBstMinMax() {
        long avlTime = 0L;
        long bstTime = 0L;
        long setTime = 0L;


        System.out.println("Initial insertions done.");
        for (int i = 0; i < MINMAX; i++) {
            AVLTree<Integer> avl = new AVLTree<>();
            BSTree<Integer> bst = new BSTree<>();
            TreeSet<Integer> set = new TreeSet<>();
            ArrayList<Integer> pom = new ArrayList<>();
            Random rng = new Random();
            for (int k = 0; k < 10000; k++) {
                int val = rng.nextInt();
                if (avl.insert(val) != null) {
                    bst.insert(val);
                    set.add(val);
                    pom.add(val);
                }
            }

            long t0 = System.nanoTime();
            int bst_min = bst.findMin(bst.getRoot());
            int bst_max = bst.findMax(bst.getRoot());
            long t1 = System.nanoTime();
            bstTime += t1 - t0;

            long t2 = System.nanoTime();
            int avl_min = avl.findMin(avl.getRoot());
            int avl_max = avl.findMax(avl.getRoot());
            long t3 = System.nanoTime();
            avlTime += t3 - t2;

            long t4 = System.nanoTime();
            int set_first = set.first();
            int set_last = set.last();
            long t5 = System.nanoTime();
            setTime += t5 - t4;

            if (bst_min != avl_min || bst_min != set_first) {
                throw new AssertionError("Min values do not match!");
            }
            if (bst_max != avl_max || bst_max != set_last) {
                throw new AssertionError("Max values do not match!");
            }
        }
        System.out.println("for bst min/max " + bstTime / 1_000_000.0);
        System.out.println("for avl min/max " + avlTime / 1_000_000.0);
        System.out.println("for treeSet min/max " + setTime / 1_000_000.0);

    }

    public void testBst() {
        System.out.println("testBST");
        for (int j = 1; j < 2; j++) {
            BSTree<Integer> bst = new BSTree<>();
            ArrayList<Integer> pom = new ArrayList<>();
            Random rng = new Random(j);
            for (int i = 0; i < OPERATIONS; i++) {
                int operation = rng.nextInt(100);
                if (operation < 30) {
                    int val = Math.abs(rng.nextInt());
                    if (bst.insert(val) != null) {
                        pom.add(val);
                    }
                } else{
                    if (!pom.isEmpty()) {
                        int index = rng.nextInt(pom.size());
                        int val = pom.get(index);
                        if (operation < 50) {
                            Integer res = bst.delete(val);
                            if (res == null) {
                                throw new AssertionError("error at " + j);
                            }
                            pom.remove(index);
                        } if (operation >= 50 && operation < 70) {
                            Integer res = bst.search(val);
                            if (res == null || !res.equals(val)) {
                                throw new AssertionError("error at " + j);
                            }
                        }
                        if (operation >= 70) {
                            int min_s = bst.findMin(bst.getRoot());
                            int max_s = bst.findMax(bst.getRoot());
                            int min = rng.nextInt((max_s - min_s) + 1) + min_s;
                            int max = rng.nextInt((max_s - min) + 1) + min;

                            List<Integer> res = bst.intervalSearch(min, max);
                            if (res == null) {
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
        System.out.println("Test finished");
    }

    public void testBstAvlIncrement() {
        long avlTime = 0L;
        long bstTime = 0L;
        long setTime = 0L;

        System.out.println("Test bst incremental");
        BSTree<Integer> bst = new BSTree<>();
        AVLTree<Integer> avl = new AVLTree<>();
        TreeSet<Integer> set = new TreeSet<>();
        for (int i = 0; i < INSERT; i++) {
            long t0 = System.nanoTime();
            bst.insert(i);
            long t1 = System.nanoTime();
            bstTime += t1 - t0;

            long t2 = System.nanoTime();
            avl.insert(i);
            long t3 = System.nanoTime();
            avlTime += t3 - t2;

            long t4 = System.nanoTime();
            set.add(i);
            long t5 = System.nanoTime();
            setTime += t5 - t4;
            System.out.println(i);
        System.out.println("for bst insert " + bstTime / 1_000_000.0);
        System.out.println("for avl insert " + avlTime / 1_000_000.0);
        System.out.println("for treeSet insert " + setTime / 1_000_000.0);
        }
    }



    public void testAvl() {
        System.out.println("testAVL");
        for (int j = 1; j < 2; j++) {
            AVLTree<Integer> avl = new AVLTree<>();
            ArrayList<Integer> pom = new ArrayList<>();
            Random rng = new Random(j);
            for (int i = 1; i < OPERATIONS; i++) {
                System.out.println(i);
                int operation = rng.nextInt(100);
                if (operation < 30) {
                    int val = Math.abs(rng.nextInt());
                    if (avl.insert(val) != null) {
                        pom.add(val);
                    }
                } else{
                    if (!pom.isEmpty()) {
                        int index = rng.nextInt(pom.size());
                        int val = pom.get(index);
                        if (operation < 50) {
                            Integer res = avl.delete(val);
                            if (res == null) {
                                throw new AssertionError("error at " + j);
                            }
                            pom.remove(index);
                        }
                        if (operation >= 50 && operation < 70) {
                            Integer res = avl.search(val);
                            if (res == null || !res.equals(val)) {
                                throw new AssertionError("error at " + j);
                            }
                        }
                        if (operation >= 70) {
                            int min_s = avl.findMin(avl.getRoot());
                            int max_s = avl.findMax(avl.getRoot());
                            int min = rng.nextInt((max_s - min_s) + 1) + min_s;
                            int max = rng.nextInt((max_s - min) + 1) + min;

                            List<Integer> res = avl.intervalSearch(min, max);
                            if (res == null) {
                                throw new AssertionError("error at " + j);
                            }
                        }
                    }
                }
                if (i % 10_000 == 0) {
                    if (!haveSameElements(pom, avl.inOrder(false))) {
                        throw new AssertionError("error at " + j);
                    }
                    validateNodeHeights(avl.getRoot());
                }
            }
        }
        System.out.println("Test finished");
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

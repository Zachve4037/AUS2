package tester;

import com.sun.source.tree.Tree;
import trees.BST;
import trees.avl.AVLTree;

import java.util.*;

public class Tester {
    private BST<Integer> bst;
    private AVLTree<Integer> avl;
    //private something tree;
    private final int OPERATIONS = 100000;

    public Tester() {
        this.avl = new AVLTree<>();
    }

    private static <T extends Comparable<T>> boolean sameNodesBST(BST<T> bst, List<T> list) {
        List<T> inOrder = bst.inOrder(false);
        if (inOrder.size() != list.size()) return false;

        List<T> sorted = new ArrayList<>(list);
        Comparator<T> cmp = Comparator.nullsFirst(Comparator.naturalOrder());
        sorted.sort(cmp);

        Iterator<T> it1 = inOrder.iterator();
        Iterator<T> it2 = sorted.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            T v = it1.next();
            T s = it2.next();
            if (v == null) {
                if (s != null) return false;
            } else {
                if (!v.equals(s)) return false;
            }
        }
        return !it1.hasNext() && !it2.hasNext();
    }


    public void testBst() {
        for (int j = 1; j < 5; j++) {
            this.bst = new BST<>();
            ArrayList<Integer> pom = new ArrayList<>();
            Random rng = new Random(j);
            int ig = 0;
            for (int i = 0; i < OPERATIONS; i++) {
                ig = i;
                if (rng.nextInt(100) < 70) {
                    int val = rng.nextInt();
                    this.bst.insert(val);
                    pom.add(val);
                } else {
                    if (!pom.isEmpty()) {
                        int index = rng.nextInt(pom.size());
                        int val = pom.get(index);
                        this.bst.delete(val);
                        pom.remove(index);
                    }
                }
            }
            if (!sameNodesBST(this.bst, pom)) {
                throw new AssertionError("error at " + j + " operation " + ig);
            }
            System.out.println("Finished operations for test " + j);
            System.out.println(j);
        }
    }
}

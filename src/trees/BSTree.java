package trees;

import java.io.*;
import java.util.*;
import java.util.function.Supplier;
import who.Exportable;

public class BSTree<T extends Comparable<T> & Exportable> {
    protected int nodeCount;
    protected BSTNode<T> root;

    public BSTree() {
        this.nodeCount = 0;
        this.root = null;
    }

    public BSTNode<T> createNewNode(T value) {
        return new BSTNode<>(value);
    }

    public T insert(T value) {
        BSTNode<T> newNode = createNewNode(value);
        if (this.root == null) {
            this.setRoot(newNode);
            this.nodeCount++;
            return this.root.getValue();
        }

        BSTNode<T> current = this.root;
        BSTNode<T> parent = null;
        while (current != null) {
            parent = current;
            int cmp = value.compareTo(current.getValue());
            if (cmp < 0) {
                current = current.getLeftSon();
            } else if (cmp > 0) {
                current = current.getRightSon();
            } else {
                return null;
            }
        }
        if (value.compareTo(parent.getValue()) < 0) {
            parent.setLeftSon(newNode);
        } else {
            parent.setRightSon(newNode);
        }
        newNode.setParent(parent);
        this.nodeCount++;
        return newNode.getValue();
    }

    public T search(T value) {
        BSTNode<T> current = this.root;
        while (current != null) {
            int cmp = value.compareTo(current.getValue());
            if (cmp == 0) {
                return current.getValue();
            } else if (cmp < 0) {
                current = current.getLeftSon();
            } else {
                current = current.getRightSon();
            }
        }
        return null;
    }

    public T delete(T value) {
        BSTNode<T> current = this.root;
        while (current != null) {
            int cmp = value.compareTo(current.getValue());
            if (cmp < 0) {
                current = current.getLeftSon();
            } else if (cmp > 0) {
                current = current.getRightSon();
            } else {
                break;
            }
        }
        if (current == null) return null;

        if (current.getLeftSon() != null && current.getRightSon() != null) {
            BSTNode<T> successor = current.getRightSon();
            while (successor.getLeftSon() != null) {
                successor = successor.getLeftSon();
            }
            current.setValue(successor.getValue());
            current = successor;
        }

        BSTNode<T> replacement = (current.getLeftSon() != null)
                ? current.getLeftSon()
                : current.getRightSon();

        T deleted= current.getValue();

        replaceNode(current, replacement);

        nodeCount--;
        return deleted;
    }

    protected void replaceNode(BSTNode<T> target, BSTNode<T> replacement) {
        if (replacement != null) {
            replacement.setParent(target.getParent());
        }
        if (target.getParent() == null) {
            root = replacement;
        }
        else if (target == target.getParent().getLeftSon()) {
            target.getParent().setLeftSon(replacement);
        }
        else {
            target.getParent().setRightSon(replacement);
        }
    }

    public List<T> intervalSearch(T low, T high) {
        List<T> res = new LinkedList<>();

        BSTNode<T> current = this.root;
        BSTNode<T> pom = null;

        while (current != null) {
            int cmp = low.compareTo(current.getValue());
            if (cmp <= 0) {
                pom = current;
                current = current.getLeftSon();
            } else {
                current = current.getRightSon();
            }
        }

        current = pom;
        while (current != null) {
            if (current.getValue().compareTo(high) > 0) break;
            res.add(current.getValue());
            current = traverseInOrder(current);
        }

        return res;
    }

    public List<T> inOrder(boolean print) {
        List<T> res = new LinkedList<>();
        for (BSTNode<T> val : innerInOrder(this.root)) {
            if (print) {
                System.out.println(val.getValue());
            }
            res.add(val.getValue());
        }
        return res;
    }

    protected List<BSTNode<T>> innerInOrder(BSTNode<T> startNode) {
        List<BSTNode<T>> res = new LinkedList<>();
        if (startNode == null) {
            return res;
        }

        BSTNode<T> current = startNode;
        while (current.getLeftSon() != null) {
            current = current.getLeftSon();
        }

        while (current != null) {
            res.add(current);
            current = traverseInOrder(current);
        }
        return res;
    }

    private BSTNode<T> traverseInOrder(BSTNode<T> current) {
        if (current.getRightSon() != null) {
            current = current.getRightSon();
            while (current.getLeftSon() != null) {
                current = current.getLeftSon();
            }
        } else {
            while (current.getParent() != null && current == current.getParent().getRightSon()) {
                current = current.getParent();
            }
            current = current.getParent();
        }
        return current;
    }

    public List<T> levelOrder(boolean print) {
        List<T> res = new LinkedList<>();
        for (BSTNode<T> val : innerLevelOrder(this.root)) {
            if (print) {
                System.out.println(val.getValue());
            }
            res.add(val.getValue());
        }
        return res;
    }

    private List<BSTNode<T>> innerLevelOrder(BSTNode<T> startNode) {
        List<BSTNode<T>> res = new LinkedList<>();
        if (startNode == null) return res;
        List<BSTNode<T>> queue = new LinkedList<>();
        queue.addLast(startNode);
        while (!queue.isEmpty()) {
            BSTNode<T> current = queue.removeFirst();
            res.addLast(current);
            if (current.getLeftSon() != null) {
                queue.addLast(current.getLeftSon());
            }
            if (current.getRightSon() != null) {
                queue.addLast(current.getRightSon());
            }
        }
        return res;
    }

    public T findMin(BSTNode<T> startNode) {
        if (startNode == null) {
            return null;
        } else if (startNode.getLeftSon() == null) {
            return startNode.getValue();
        } else {
            BSTNode<T> current = startNode.getLeftSon();
            while (current.getLeftSon() != null) {
                current = current.getLeftSon();
            }
            return current.getValue();
        }
    }

    public T findMax(BSTNode<T> startNode) {
        if (startNode == null) {
            return null;
        } else if (startNode.getRightSon() == null) {
            return startNode.getValue();
        } else {
            BSTNode<T> current = startNode.getRightSon();
            while (current.getRightSon() != null) {
                current = current.getRightSon();
            }
            return current.getValue();
        }
    }

    public void exportToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (T value : this.inOrder(false)) {
                writer.write(value.serialize());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importFromFile(String filePath, Supplier<T> factory) throws IOException {
        this.root = null;
        this.nodeCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                T obj = factory.get();
                obj.deserialize(line);
                this.insert(obj);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BSTNode<T> getRoot() {
        return this.root;
    }

    protected void setRoot(BSTNode<T> root) {
        this.root = root;
    }

    public int getNodeCount() {
        return this.nodeCount;
    }
}

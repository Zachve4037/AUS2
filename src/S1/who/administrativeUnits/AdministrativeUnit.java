package S1.who.administrativeUnits;

import S1.trees.avl.AVLTree;
import S1.who.baseClasses.PCRTest;
import S1.who.wrappers.PCRT_DateTime;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public abstract class AdministrativeUnit<T extends AdministrativeUnit<T>> implements Comparable<T>, Serializable {
    private static final long serialVersionUID = 1L;
    private final int adminUnitID_;
    private AVLTree<PCRT_DateTime> tests;
    private AVLTree<PCRT_DateTime> positiveTests;

    public AdministrativeUnit(int ID) {
        this.adminUnitID_ = ID;
        this.tests = new AVLTree<>();
        this.positiveTests = new AVLTree<>();
    }

    public int getID() {
        return this.adminUnitID_;
    }

    public void insertTest(PCRTest test) {
        if (test == null) return;
        PCRT_DateTime wrapper = new PCRT_DateTime(test);
        this.tests.insert(wrapper);
        if (test.getTestResult()) {
            this.positiveTests.insert(wrapper);
        }
    }

    public void removeTest(PCRTest test) {
        if (test == null) return;
        PCRT_DateTime wrapper = new PCRT_DateTime(test);
        this.tests.delete(wrapper);
        if (test.getTestResult()) {
            this.positiveTests.delete(wrapper);
        }
    }

    public List<PCRT_DateTime> getTestsInInterval(LocalDateTime from, LocalDateTime to, boolean positive) {
        if (from == null || to == null) return null;
        PCRT_DateTime testFrom = new PCRT_DateTime(
                new PCRTest(from, null, Integer.MIN_VALUE, 0, 0, 0,false,0.0, "")
        );
        PCRT_DateTime testTo = new PCRT_DateTime(
                new PCRTest(to, null, Integer.MAX_VALUE, 0, 0, 0, false, 0.0, "")
        );
        if (positive) {
            return this.positiveTests.intervalSearch(testFrom, testTo);
        } else {
            return this.tests.intervalSearch(testFrom, testTo);
        }
    }

    @Override
    public int compareTo(T o) {
        if (o == null) return 1;
        return Integer.compare(this.adminUnitID_, o.getID());
    }

    @Override
    public String toString() {
        return Integer.toString(this.adminUnitID_);
    }
}

package who.administrativeUnits;

import trees.avl.AVLTree;
import who.baseClasses.PCRTest;
import who.wrappers.PCRT_DateTime;

public abstract class AdministrativeUnit<T extends AdministrativeUnit<T>> implements Comparable<T> {
    private final int adminUnitID_;
    private AVLTree<PCRT_DateTime> tests;
    private AVLTree<PCRT_DateTime> positiveTests;

    public AdministrativeUnit(int districtID) {
        this.adminUnitID_ = districtID;
        this.tests = new AVLTree<>();
        this.positiveTests = new AVLTree<>();
    }

    public int getDistrictID() {
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

    @Override
    public int compareTo(T o) {
        if (o == null) return 1;
        return Integer.compare(this.adminUnitID_, o.getDistrictID());
    }
}

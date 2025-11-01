package who.DBSystem;

import trees.avl.AVLTree;
import who.administrativeUnits.AdministrativeUnit;
import who.administrativeUnits.District;
import who.administrativeUnits.Region;
import who.administrativeUnits.Workplace;
import who.baseClasses.PCRTest;
import who.baseClasses.Person;
import who.wrappers.PCRT_DateTime;

import java.time.LocalDateTime;
import java.util.List;

public class Database {
    private AVLTree<PCRT_DateTime> tests_;
    private AVLTree<PCRT_DateTime> positiveTests_;
    private AVLTree<Person> people_;
    private AVLTree<Region> regions_;
    private AVLTree<District> districts_;
    private AVLTree<Workplace> workplaces_;

    public Database() {
        this.tests_ = new AVLTree<>();
        this.positiveTests_ = new AVLTree<>();
        this.people_ = new AVLTree<>();
        this.regions_ = new AVLTree<>();
        this.districts_ = new AVLTree<>();
        this.workplaces_ = new AVLTree<>();
    }

    //1
    public PCRTest insertTest(PCRTest test) {
        if (test == null) return null;

        //osoby
        String personID = test.getPersonID();
        if (personID != null && !personID.isEmpty()) {
            Person keyPerson = new Person(personID, null, null, null);
            Person existing = this.people_.search(keyPerson);
            if (existing == null) {
                //zabezpecim aby sa test vkladal iba osobe ktora uz existuje
                return null;
            } else {
                existing.addTest(test);
                test.setPerson(existing);
            }
        }

        //testy
        PCRT_DateTime wrapper = new PCRT_DateTime(test);
        this.tests_.insert(wrapper);
        if (test.getTestResult()) {
            this.positiveTests_.insert(wrapper);
        }

        //kraje
        int regionID = test.getRegionID();
        if (regionID > 0) {
            Region keyRegion = new Region(regionID);
            Region existing = this.regions_.search(keyRegion);
            if (existing == null) {
                this.regions_.insert(keyRegion);
                existing = keyRegion;
            }
            existing.insertTest(test);
        }

        //okresy
        int districtID = test.getDistrictID();
        if (districtID > 0) {
            District keyDistrict = new District(districtID);
            District existing = this.districts_.search(keyDistrict);
            if (existing == null) {
                this.districts_.insert(keyDistrict);
                existing = keyDistrict;
            }
            existing.insertTest(test);
        }

        //pracoviska
        int workplaceID = test.getWorkplaceID();
        if (workplaceID > 0) {
            Workplace keyWorkplace = new Workplace(workplaceID);
            Workplace existing = this.workplaces_.search(keyWorkplace);
            if (existing == null) {
                this.workplaces_.insert(keyWorkplace);
                existing = keyWorkplace;
            }
            existing.insertTest(test);
        }

        return test;
    }

    //2
    public PCRTest getTestByPersonByID(String personID, int testID) {
        if (personID == null || personID.isEmpty() || testID <= 0) return null;
        Person keyPerson = new Person(personID, null, null, null);
        Person existing = this.people_.search(keyPerson);
        if (existing == null) return null;
        return existing.getTestByID(testID);
    }

    //3
    public List<PCRT_DateTime> getAllTestsForPerson(String personID) {
        if (personID == null || personID.isEmpty()) return null;
        Person keyPerson = new Person(personID, null, null, null);
        Person existing = this.people_.search(keyPerson);
        if (existing == null) {
            return null;
        }
        return existing.getAllTestsByDate();
    }

    //4, 5, 6, 7, 17
    private <U extends AdministrativeUnit<U>> List<PCRT_DateTime> getTestsInIntervalFromTree(
            AVLTree<U> tree, U key, LocalDateTime from, LocalDateTime to, boolean positive) {
        if (key == null || from == null || to == null) return null;
        U existing = tree.search(key);
        if (existing == null) return null;
        return existing.getTestsInInterval(from, to, positive);
    }

    //4, 5, 6, 7, 17
    public List<PCRT_DateTime> getTestsInIntervalForDistrict(int districtID, LocalDateTime from, LocalDateTime to, boolean positive) {
        if (districtID == 0 || from == null || to == null) return null;
        return getTestsInIntervalFromTree(this.districts_, new District(districtID), from, to, positive);
    }

    //4, 5, 6, 7, 17
    public List<PCRT_DateTime> getTestsInIntervalForRegion(int regionId, LocalDateTime from, LocalDateTime to, boolean positive) {
        if (regionId == 0 || from == null || to == null) return null;
        return getTestsInIntervalFromTree(this.regions_, new Region(regionId), from, to, positive);
    }

    //4, 5, 6, 7, 17
    public List<PCRT_DateTime> getTestsInIntervalForWorkplace(int workplaceID, LocalDateTime from, LocalDateTime to) {
        if (workplaceID == 0 || from == null || to == null) return null;
        return getTestsInIntervalFromTree(this.workplaces_, new Workplace(workplaceID), from, to, false);
    }

    //8, 9
    public List<PCRT_DateTime> getTestsinInterval(LocalDateTime from, LocalDateTime to, boolean positive) {
        if (from == null || to == null) return null;
        PCRT_DateTime testFrom = new PCRT_DateTime(
                new PCRTest(from, null, Integer.MIN_VALUE,0,0,0,false,0.0,"")
        );
        PCRT_DateTime testTo = new PCRT_DateTime(
                new PCRTest(to, null, Integer.MAX_VALUE, 0, 0, 0, false, 0.0, "")
        );
        if (positive) {
            return this.positiveTests_.intervalSearch(testFrom, testTo);
        } else {
            return this.tests_.intervalSearch(testFrom, testTo);
        }
    }
}

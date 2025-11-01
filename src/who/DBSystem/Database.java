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
import java.util.*;

public class Database {
    private AVLTree<PCRT_DateTime> testsByDate_;
    private AVLTree<PCRTest> testsByID_;
    private AVLTree<PCRT_DateTime> positiveTests_;
    private AVLTree<Person> people_;
    private AVLTree<Region> regions_;
    private AVLTree<District> districts_;
    private AVLTree<Workplace> workplaces_;

    public Database() {
        this.testsByDate_ = new AVLTree<>();
        this.positiveTests_ = new AVLTree<>();
        this.testsByID_ = new AVLTree<>();
        this.people_ = new AVLTree<>();
        this.regions_ = new AVLTree<>();
        this.districts_ = new AVLTree<>();
        this.workplaces_ = new AVLTree<>();
    }

    //1
    public PCRTest insertTest(PCRTest test) {
        if (test == null) return null;

        //osoba
        String personID = test.getPersonID();
        if (personID != null && !personID.isEmpty()) {
            Person keyPerson = new Person(personID, null, null, null);
            Person existingPerson = this.people_.search(keyPerson);
            if (existingPerson == null) {
                // osoba neexistuje -> neumožníme vložiť test
                return null;
            } else {
                existingPerson.addTest(test);
                test.setPerson(existingPerson);
            }
        }

        //testy
        PCRT_DateTime wrapper = new PCRT_DateTime(test);
        this.testsByDate_.insert(wrapper);
        this.testsByID_.insert(test); // predpoklad: PCRTest.compareTo porovnáva podľa ID
        if (test.getTestResult()) {
            this.positiveTests_.insert(wrapper);
        }

        //jednotky
        insertTestIntoUnit(this.regions_, new Region(test.getRegionID()), test);
        insertTestIntoUnit(this.districts_, new District(test.getDistrictID()), test);
        insertTestIntoUnit(this.workplaces_, new Workplace(test.getWorkplaceID()), test);

        return test;
    }

    //pomocka na vlozenie testu do jednotky
    private <U extends AdministrativeUnit<U>> void insertTestIntoUnit(AVLTree<U> tree, U keyUnit, PCRTest test) {
        if (tree == null || keyUnit == null || test == null) return;
        int id = keyUnit.getID();
        if (id <= 0) return;

        U existing = tree.search(keyUnit);
        if (existing == null) {
            tree.insert(keyUnit);
            existing = tree.search(keyUnit);
            if (existing == null) existing = keyUnit;
        }
        existing.insertTest(test);
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
        AVLTree<U> tree, U key, LocalDateTime from, LocalDateTime to, boolean positive
    ) {
        if (key == null || from == null || to == null) return null;
        U existing = tree.search(key);
        if (existing == null) return null;
        return existing.getTestsInInterval(from, to, positive);
    }

    //4, 5, 6, 7, 17
    public List<PCRT_DateTime> getTestsInIntervalForDistrict(int districtID, LocalDateTime from, LocalDateTime to, boolean positive) {
        if (districtID <= 0 || from == null || to == null) return null;
        return getTestsInIntervalFromTree(this.districts_, new District(districtID), from, to, positive);
    }

    //4, 5, 6, 7, 17
    public List<PCRT_DateTime> getTestsInIntervalForRegion(int regionId, LocalDateTime from, LocalDateTime to, boolean positive) {
        if (regionId <= 0 || from == null || to == null) return null;
        return getTestsInIntervalFromTree(this.regions_, new Region(regionId), from, to, positive);
    }

    //4, 5, 6, 7, 17
    public List<PCRT_DateTime> getTestsInIntervalForWorkplace(int workplaceID, LocalDateTime from, LocalDateTime to, boolean positive) {
        if (workplaceID <= 0 || from == null || to == null) return null;
        return getTestsInIntervalFromTree(this.workplaces_, new Workplace(workplaceID), from, to, positive);
    }

    //8, 9
    public List<PCRT_DateTime> getTestsInInterval(LocalDateTime from, LocalDateTime to, boolean positive) {
        if (from == null || to == null) return null;
        PCRT_DateTime testFrom = createPCRTKey(from, Integer.MIN_VALUE);
        PCRT_DateTime testTo = createPCRTKey(to, Integer.MAX_VALUE);
        if (positive) {
            return this.positiveTests_.intervalSearch(testFrom, testTo);
        } else {
            return this.testsByDate_.intervalSearch(testFrom, testTo);
        }
    }

    //pomocka na vytvorenie PCRT_DateTime testu
    private PCRT_DateTime createPCRTKey(LocalDateTime dt, int idKey) {
        return new PCRT_DateTime(new PCRTest(dt, null, idKey, 0, 0, 0, false, 0.0, ""));
    }

    //10, 11
    public AVLTree<Person> getSickPeopleFromDisctrict(int districtID, LocalDateTime date, int days, boolean sortByTestValue) {
        if (districtID <= 0 || days < 0) return null;
        District keyDistrict = new District(districtID);
        District existing = this.districts_.search(keyDistrict);
        if (existing != null) {
            List<PCRT_DateTime> positiveTests = existing.getTestsInInterval(date.minusDays(days), date, true);
            return getPersonAVLTree(sortByTestValue, positiveTests);
        }
        return null;
    }

    //12
    public AVLTree<Person> getSickPeopleFromRegion(int regionID, LocalDateTime date, int days, boolean sortByTestValue) {
        if (regionID <= 0 || days < 0) return null;
        Region keyRegion = new Region(regionID);
        Region existing = this.regions_.search(keyRegion);
        if (existing != null) {
            List<PCRT_DateTime> positiveTests = existing.getTestsInInterval(date.minusDays(days), date, true);
            return getPersonAVLTree(sortByTestValue, positiveTests);
        }
        return null;
    }

    //13
    public AVLTree<Person> getSickPeopleFromAll(LocalDateTime date, int days, boolean sortByTestValue) {
        if (days < 0) return null;
        PCRT_DateTime testFrom = createPCRTKey(date.minusDays(days), Integer.MIN_VALUE);
        PCRT_DateTime testTo = createPCRTKey(date, Integer.MAX_VALUE);
        List<PCRT_DateTime> positiveTests = this.positiveTests_.intervalSearch(testFrom, testTo);
        return getPersonAVLTree(sortByTestValue, positiveTests);
    }

    //pomocka na usporiadanie podla testValue a vrati zoznam osob bez duplicit
    private AVLTree<Person> getPersonAVLTree(boolean sortByTestValue, List<PCRT_DateTime> positiveTests) {
        if (sortByTestValue) {
            positiveTests.sort(Comparator.comparingDouble(o -> o.getTest().getTestValue()));
        }
        AVLTree<Person> sickPeople = new AVLTree<>();
        for (PCRT_DateTime wrapper : positiveTests) {
            Person person = wrapper.getTest().getPerson();
            if (person != null) {
                sickPeople.insert(person);
            }
        }
        return sickPeople;
    }

    //14
    public List<Person> getPersonByHighestTestValue(LocalDateTime date, int days) {
        if (date == null || days < 0 || this.districts_ == null) return null;
        List<Person> highestPople = new LinkedList<>();
        for (District dis : this.districts_.inOrder(false)) {
            List<PCRT_DateTime> filtered = dis.getTestsInInterval(date.minusDays(days), date, true);
            Person res = null;
            double highest = Double.NEGATIVE_INFINITY;
            if (filtered != null) {
                for (PCRT_DateTime wrapper : filtered) {
                    if (wrapper == null) continue;
                    PCRTest test = wrapper.getTest();
                    if (test.getTestValue() > highest) {
                        highest = test.getTestValue();
                        res = test.getPerson();
                    }
                }
            }
            highestPople.add(res);
        }
        return highestPople;
    }

    //pomocka na usporiadanie jednotiek podla poctu nakazenych
    private <U extends AdministrativeUnit<U>> List<U> getUnitsSortedBySickPeople(
            AVLTree<U> tree, LocalDateTime date, int days) {
        if (tree == null || date == null || days < 0) return null;
        List<U> units = tree.inOrder(false);
        List<UnitCount<U>> counts = new LinkedList<>();
        for (U unit : units) {
            List<PCRT_DateTime> positiveTests = unit.getTestsInInterval(date.minusDays(days), date, true);
            AVLTree<Person> sickPeople = getPersonAVLTree(false, positiveTests);
            int cnt = sickPeople.getNodeCount();
            counts.add(new UnitCount<>(unit, cnt));
        }
        counts.sort((a, b) -> Integer.compare(b.count, a.count));
        List<U> result = new LinkedList<>();
        for (UnitCount<U> uc : counts) {
            result.add(uc.unit);
        }
        return result;
    }

    // 15
    public List<District> getDistrictsSortedBySickPeople(LocalDateTime date, int days) {
        return getUnitsSortedBySickPeople(this.districts_, date, days);
    }

    // 16
    public List<Region> getRegionsSortedBySickPeople(LocalDateTime date, int days) {
        return getUnitsSortedBySickPeople(this.regions_, date, days);
    }

    //18
    public PCRTest getPCRTestByID(int testID) {
        if (testID < 0) return null;
        return this.testsByID_.search(new PCRTest(null, null, testID, 0, 0, 0, false, 0.0, ""));
    }

    //19
    public Person insertPerson(Person person) {
        if (person == null) return null;
        return this.people_.insert(person);
    }

    //20
    public void deleteTest(int testID) {
        if (testID <= 0) return;
        PCRTest test = getPCRTestByID(testID);
        if (test == null) return;

        //testy
        this.testsByID_.delete(test);
        PCRT_DateTime wrapper = new PCRT_DateTime(test);
        this.testsByDate_.delete(wrapper);
        if (test.getTestResult()) this.positiveTests_.delete(wrapper);

        //osoby
        Person person = test.getPerson();
        if (person != null) {
            person.deleteTest(test);
            test.setPerson(null);
        }

        //jednotky
        removeTestFromUnit(this.regions_, new Region(test.getRegionID()), test);
        removeTestFromUnit(this.districts_, new District(test.getDistrictID()), test);
        removeTestFromUnit(this.workplaces_, new Workplace(test.getWorkplaceID()), test);
    }

    private <U extends AdministrativeUnit<U>> void removeTestFromUnit(AVLTree<U> tree, U keyUnit, PCRTest test) {
        if (tree == null || keyUnit == null || test == null) return;
        U existing = tree.search(keyUnit);
        if (existing != null) existing.removeTest(test);
    }

    //21
    public void deletePerson(String personID) {
        if (this.people_ == null || personID == null || personID.isEmpty()) return;
        Person keyPerson = new Person(personID, null, null, null);
        Person existing = this.people_.search(keyPerson);
        if (existing == null) return;

        List<PCRTest> testsSnapshot = new LinkedList<>();
        if (existing.getPcrTestsById_() != null) {
            testsSnapshot.addAll(existing.getPcrTestsById_().inOrder(false));
        }
        for (PCRTest test : testsSnapshot) {
            if (test != null) {
                deleteTest(test.getPcrID());
            }
        }
        if (existing.getPcrTestsById_() != null) {
            for (PCRTest remaining : existing.getPcrTestsById_().inOrder(false)) {
                existing.getPcrTestsById_().delete(remaining);
            }
        }
        this.people_.delete(existing);
    }
}
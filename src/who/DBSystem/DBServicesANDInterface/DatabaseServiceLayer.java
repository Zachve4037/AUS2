package who.DBSystem.DBServicesANDInterface;

import who.DBSystem.Database;
import who.administrativeUnits.District;
import who.administrativeUnits.Region;
import who.baseClasses.PCRTest;
import who.baseClasses.Person;
import who.wrappers.PCRT_DateTime;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class DatabaseServiceLayer implements IDatabaseService {
    private static volatile DatabaseServiceLayer instance;
    private Database db_;

    private DatabaseServiceLayer() {
        this.db_ = Database.getInstance();
    }

    public static DatabaseServiceLayer getInstance() {
        if (instance == null) {
            synchronized (DatabaseServiceLayer.class) {
                if (instance == null) instance = new DatabaseServiceLayer();
            }
        }
        return instance;
    }

    public void generateTestData() {
        this.db_.generateTestData();
    }

    @Override
    public void importFromFile(String peoplePath, String testsPath) throws IOException {
        this.db_.importFromFiles(peoplePath, testsPath);
    }

    @Override
    public void exportToFile(String peoplePath, String testsPath) throws IOException {
        this.db_.exportToFiles(peoplePath, testsPath);
    }

    @Override
    public Person addPerson(Person person) {
        Person added =  this.db_.insertPerson(person);
        return CopyClass.deepCopy(added);
    }

    @Override
    public void deletePerson(String personID) {
        this.db_.deletePerson(personID);
    }

    @Override
    public List<PCRT_DateTime> getPersonTestsForPerson(String personID) {
        List<PCRT_DateTime> res = this.db_.getAllTestsForPerson(personID);
        return res != null ? CopyClass.deepCopyList(res) : Collections.emptyList();
    }

    @Override
    public PCRTest addTest(PCRTest test) {
        PCRTest added = this.db_.insertTest(test);
        return CopyClass.deepCopy(added);
    }

    @Override
    public PCRTest getPCRTestByID(int testID) {
        PCRTest res = this.db_.getPCRTestByID(testID);
        return CopyClass.deepCopy(res);
    }

    @Override
    public PCRTest getTestByPersonByID(String personID, int testID) {
        PCRTest rest = this.db_.getTestByPersonByID(personID, testID);
        return CopyClass.deepCopy(rest);
    }

    @Override
    public void deleteTest(int testID) {
        this.db_.deleteTest(testID);
    }

    @Override
    public List<PCRT_DateTime> getTestsInIntervalForDistrict(int districtID, LocalDateTime from, LocalDateTime to, boolean positive) {
        List<PCRT_DateTime> res = this.db_.getTestsInIntervalForDistrict(districtID, from, to, positive);
        return res != null ? CopyClass.deepCopyList(res) : Collections.emptyList();
    }

    @Override
    public List<PCRT_DateTime> getTestsInIntervalForRegion(int regionID, LocalDateTime from, LocalDateTime to, boolean positive) {
        List<PCRT_DateTime> res = this.db_.getTestsInIntervalForRegion(regionID, from, to, positive);
        return res != null ? CopyClass.deepCopyList(res) : Collections.emptyList();
    }

    @Override
    public List<PCRT_DateTime> getTestsInIntervalForWorkplace(int workplaceID, LocalDateTime from, LocalDateTime to, boolean positive) {
        List<PCRT_DateTime> res = this.db_.getTestsInIntervalForWorkplace(workplaceID, from, to, positive);
        return res != null ? CopyClass.deepCopyList(res) : Collections.emptyList();
    }

    @Override
    public List<PCRT_DateTime> getTestsInInterval(LocalDateTime from, LocalDateTime to, boolean positive) {
        List<PCRT_DateTime> res = this.db_.getTestsInInterval(from, to, positive);
        return res != null ? CopyClass.deepCopyList(res) : Collections.emptyList();
    }

    @Override
    public List<Person> getSickPeopleFromDisctrict(int districtID, LocalDateTime date, int days, boolean sortByTestValue) {
        List<Person> res = this.db_.getSickPeopleFromDisctrict(districtID, date, days, sortByTestValue);
        return res != null ? CopyClass.deepCopyList(res) : Collections.emptyList();
    }

    @Override
    public List<Person> getSickPeopleFromRegion(int regionID, LocalDateTime date, int days, boolean sortByTestValue) {
        List<Person> res = this.db_.getSickPeopleFromRegion(regionID, date, days, sortByTestValue);
        return res != null ? CopyClass.deepCopyList(res) : Collections.emptyList();
    }

    @Override
    public List<Person> getSickPeopleFromAll(LocalDateTime date, int days, boolean sortByTestValue) {
        List<Person> res = this.db_.getSickPeopleFromAll(date, days, sortByTestValue);
        return res != null ? CopyClass.deepCopyList(res) : Collections.emptyList();
    }

    @Override
    public List<District> getDistrictsSortedBySickPeople(LocalDateTime date, int days) {
        List<District> res = this.db_.getDistrictsSortedBySickPeople(date, days);
        return res != null ? CopyClass.deepCopyList(res) : Collections.emptyList();
    }

    @Override
    public List<Region> getRegionsSortedBySickPeople(LocalDateTime date, int days) {
        List<Region> res = this.db_.getRegionsSortedBySickPeople(date, days);
        return res != null ? CopyClass.deepCopyList(res) : Collections.emptyList();
    }

    @Override
    public List<String> getPersonByHighestTestValue(LocalDateTime date, int days) {
        List<String> res = this.db_.getPersonByHighestTestValue(date, days);
        return res != null ? new java.util.ArrayList<>(res) : Collections.emptyList();
    }
}

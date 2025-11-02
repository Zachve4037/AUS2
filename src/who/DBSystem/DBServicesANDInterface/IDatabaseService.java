package who.DBSystem.DBServicesANDInterface;

import who.administrativeUnits.District;
import who.administrativeUnits.Region;
import who.baseClasses.*;
import who.wrappers.PCRT_DateTime;
import java.time.LocalDateTime;
import java.util.List;

public interface IDatabaseService {

    Person addPerson(Person person); //19
    void deletePerson(String personID); //21
    List<PCRT_DateTime> getPersonTestsForPerson(String personID); //3


    PCRTest addTest(PCRTest test); //1
    PCRTest getPCRTestByID(int testID); //18
    PCRTest getTestByPersonByID(String personID, int testID); //2
    void deleteTest(int testID); //20

    List<PCRT_DateTime> getTestsInIntervalForDistrict(int districtID, LocalDateTime from, LocalDateTime to, boolean positive); //4, 5
    List<PCRT_DateTime> getTestsInIntervalForRegion(int regionID, LocalDateTime from, LocalDateTime to, boolean positive); //6, 7
    List<PCRT_DateTime> getTestsInIntervalForWorkplace(int workplaceID, LocalDateTime from, LocalDateTime to, boolean positive); //17
    List<PCRT_DateTime> getTestsInInterval(LocalDateTime from, LocalDateTime to, boolean positive); //8, 9

    List<Person> getSickPeopleFromDisctrict(int districtID, LocalDateTime date, int days, boolean sortByTestValue); //10, 11
    List<Person> getSickPeopleFromRegion(int regionID, LocalDateTime date, int days, boolean sortByTestValue); //12
    List<Person> getSickPeopleFromAll(LocalDateTime date, int days, boolean sortByTestValue); //13

    List<District> getDistrictsSortedBySickPeople(LocalDateTime date, int days); //15
    List<Region> getRegionsSortedBySickPeople(LocalDateTime date, int days); //16
    List<Person> getPersonByHighestTestValue(LocalDateTime date, int days); //14
}

package S2.db;

import S2.baseClasses.PCRTest;
import S2.baseClasses.PersonBinary;
import S2.heapFile.HeapFile;
import S2.linearHashing.LinearHashing;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DB {
    private static final int INITIAL_BUCKET_COUNT = 4;
    private LinearHashing<PersonBinary> people;
    private LinearHashing<PCRTest> tests;
    private int testId = 0;
    private int personId = 0;

    public DB(String dataDirectoryPath, int peopleBucketSizeMain, int peopleBucketSizeOverflow,
              int testBucketSizeMain, int testBucketSizeOverflow) throws IOException {
        File dir = new File(dataDirectoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        RandomAccessFile peopleMainFile = new RandomAccessFile(
                new File(dataDirectoryPath, "people_main.dat"), "rw");
        RandomAccessFile peopleOverflowFile = new RandomAccessFile(
                new File(dataDirectoryPath, "people_overflow.dat"), "rw");
        RandomAccessFile testsMainFile = new RandomAccessFile(
                new File(dataDirectoryPath, "tests_main.dat"), "rw");
        RandomAccessFile testsOverflowFile = new RandomAccessFile(
                new File(dataDirectoryPath, "tests_overflow.dat"), "rw");

        PersonBinary personType = new PersonBinary();
        PCRTest testType = new PCRTest();

        // Calculate actual block sizes in bytes

        HeapFile<PersonBinary> peopleMain = new HeapFile<>(
                peopleMainFile,
                PersonBinary::new,
                personType.getSize(),
                peopleBucketSizeMain,
                dataDirectoryPath + "/people_main.meta",
                true
        );

        HeapFile<PersonBinary> peopleOverflow = new HeapFile<>(
                peopleOverflowFile,
                PersonBinary::new,
                personType.getSize(),
                peopleBucketSizeOverflow,
                dataDirectoryPath + "/people_overflow.meta",
                true
        );

        HeapFile<PCRTest> testsMain = new HeapFile<>(
                testsMainFile,
                PCRTest::new,
                testType.getSize(),
                testBucketSizeMain,
                dataDirectoryPath + "/tests_main.meta",
                true
        );

        HeapFile<PCRTest> testsOverflow = new HeapFile<>(
                testsOverflowFile,
                PCRTest::new,
                testType.getSize(),
                testBucketSizeOverflow,
                dataDirectoryPath + "/tests_overflow.meta",
                true
        );

        this.people = new LinearHashing<>(peopleMain, peopleOverflow, INITIAL_BUCKET_COUNT);
        this.tests = new LinearHashing<>(testsMain, testsOverflow, INITIAL_BUCKET_COUNT);
    }


    //1
    public void insertTest(PCRTest test) throws Exception {
        test.setId(this.testId++);
        String patientId = test.getPatientId();
        PersonBinary person = findPersonById(patientId);
        test.setPatientName(person.getName());
        test.setPatientSurname(person.getSurname());
        this.tests.insert(test);

        if (person != null) {
            if (person.getTestCount() < 6) {
                person.addTest(test);
                this.people.update(person);
            } else {
                throw new Exception("Person has reached the maximum number of tests.");
            }
        } else {
            throw new IllegalArgumentException("Person with id: " + patientId + " not found.");
        }
    }

    //2
    public PersonBinary findPersonById(String personId) throws Exception {
        PersonBinary wanted = new PersonBinary();
        wanted.setId(personId);
        return this.people.get(wanted);
    }

    //3
    public PCRTest findTestById(int testId) throws Exception {
        PCRTest wanted = new PCRTest();
        wanted.setId(testId);
        return this.tests.get(wanted);
    }

    //4
    public void insertPerson(PersonBinary person) throws Exception {
        person.setId("P" + this.personId++);
        this.people.insert(person);
    }

    //7
    public void updatePerson(String personId, PersonBinary updatedPerson) throws Exception {
        updatedPerson.setId(personId);
        this.people.update(updatedPerson);
    }

    //8
    public void updateTest(int testId, PCRTest updatedTest) throws Exception {
        PCRTest old = this.findTestById(testId);
        if (old == null) {
            throw new IllegalArgumentException("Test with id: " + testId + " not found.");
        }
        String personId = old.getPatientId();
        updatedTest.setPatientId(personId);
        updatedTest.setPatientSurname(old.getPatientSurname());
        updatedTest.setPatientName(old.getPatientName());
        this.tests.update(updatedTest);

        PersonBinary person = findPersonById(personId);
        if (person != null) {
            person.updateTest(updatedTest);
            this.people.update(person);
        } else {
            throw new IllegalArgumentException("Person with id: " + personId + " not found.");
        }
    }

}

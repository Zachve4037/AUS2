package S2.db;

import S2.baseClasses.PCRTest;
import S2.baseClasses.PersonBinary;
import S2.heapFile.HeapFile;
import S2.linearHashing.LinearHashing;

import java.io.*;
import java.util.Properties;

public class DB {
    private final int INITIAL_BUCKETS = 4;
    private LinearHashing<PersonBinary> people;
    private LinearHashing<PCRTest> tests;
    private int testId = 1;
    private int personId = 0;
    private String dataDirectoryPath;

    public DB(String dataDirectoryPath, int peopleBlockSizeMain, int peopleBlockSizeOverflow,
              int testBlockSizeMain, int testBlockSizeOverflow) throws IOException {
        File dir = new File(dataDirectoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        this.dataDirectoryPath = dataDirectoryPath;
        PersonBinary personType = new PersonBinary();
        PCRTest testType = new PCRTest();

        RandomAccessFile peopleMainFile = new RandomAccessFile(dataDirectoryPath + "/people_main.dat", "rw");
        RandomAccessFile peopleOverflowFile = new RandomAccessFile(dataDirectoryPath + "/people_overflow.dat", "rw");
        RandomAccessFile testsMainFile = new RandomAccessFile(dataDirectoryPath + "/tests_main.dat", "rw");
        RandomAccessFile testsOverflowFile = new RandomAccessFile(dataDirectoryPath + "/tests_overflow.dat", "rw");

        HeapFile<PersonBinary> peopleMain = new HeapFile<>(
                peopleMainFile,
                PersonBinary::new,
                personType.getSize(),
                peopleBlockSizeMain,
                dataDirectoryPath + "/people_main.meta",
                true
        );

        HeapFile<PersonBinary> peopleOverflow = new HeapFile<>(
                peopleOverflowFile,
                PersonBinary::new,
                personType.getSize(),
                peopleBlockSizeOverflow,
                dataDirectoryPath + "/people_overflow.meta",
                true
        );

        HeapFile<PCRTest> testsMain = new HeapFile<>(
                testsMainFile,
                PCRTest::new,
                testType.getSize(),
                testBlockSizeMain,
                dataDirectoryPath + "/tests_main.meta",
                true
        );

        HeapFile<PCRTest> testsOverflow = new HeapFile<>(
                testsOverflowFile,
                PCRTest::new,
                testType.getSize(),
                testBlockSizeOverflow,
                dataDirectoryPath + "/tests_overflow.meta",
                true
        );

        this.people = new LinearHashing<>(peopleMain, peopleOverflow, INITIAL_BUCKETS);
        this.tests = new LinearHashing<>(testsMain, testsOverflow, INITIAL_BUCKETS);
    }

    public DB(String pathToConfig) throws IOException {
        File configFile = new File(pathToConfig, "db_config.txt");

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
        }

        int blockSizePeopleMain = Integer.parseInt(props.getProperty("blockSizePeopleMain"));
        int blockSizePeopleOverflow = Integer.parseInt(props.getProperty("blockSizePeopleOverflow"));
        int blockSizeTestsMain = Integer.parseInt(props.getProperty("blockSizeTestsMain"));
        int blockSizeTestsOverflow = Integer.parseInt(props.getProperty("blockSizeTestsOverflow"));
        int peopleS = Integer.parseInt(props.getProperty("peopleS"));
        int testsS = Integer.parseInt(props.getProperty("testsS"));
        int peopleM = Integer.parseInt(props.getProperty("peopleM"));
        int testsM = Integer.parseInt(props.getProperty("testsM"));
        int peopleU = Integer.parseInt(props.getProperty("peopleU"));
        int testsU = Integer.parseInt(props.getProperty("testsU"));
        int peopleRecordCount = Integer.parseInt(props.getProperty("peopleRecordCount"));
        int testsRecordCount = Integer.parseInt(props.getProperty("testsRecordCount"));

        this.dataDirectoryPath = pathToConfig;
        this.personId = peopleRecordCount;
        this.testId = testsRecordCount;

        RandomAccessFile peopleMainFile = new RandomAccessFile(pathToConfig + "/people_main.dat", "rw");
        RandomAccessFile peopleOverflowFile = new RandomAccessFile(pathToConfig + "/people_overflow.dat", "rw");
        RandomAccessFile testsMainFile = new RandomAccessFile(pathToConfig + "/tests_main.dat", "rw");
        RandomAccessFile testsOverflowFile = new RandomAccessFile(pathToConfig + "/tests_overflow.dat", "rw");

        PersonBinary personType = new PersonBinary();
        PCRTest testType = new PCRTest();

        HeapFile<PersonBinary> peopleMain = new HeapFile<>(
                peopleMainFile,
                PersonBinary::new,
                personType.getSize(),
                blockSizePeopleMain,
                pathToConfig + "/people_main.meta",
                true
        );

        HeapFile<PersonBinary> peopleOverflow = new HeapFile<>(
                peopleOverflowFile,
                PersonBinary::new,
                personType.getSize(),
                blockSizePeopleOverflow,
                pathToConfig + "/people_overflow.meta",
                true
        );

        HeapFile<PCRTest> testsMain = new HeapFile<>(
                testsMainFile,
                PCRTest::new,
                testType.getSize(),
                blockSizeTestsMain,
                pathToConfig + "/tests_main.meta",
                true
        );

        HeapFile<PCRTest> testsOverflow = new HeapFile<>(
                testsOverflowFile,
                PCRTest::new,
                testType.getSize(),
                blockSizeTestsOverflow,
                pathToConfig + "/tests_overflow.meta",
                true
        );

        this.people = new LinearHashing<>(peopleMain, peopleOverflow, peopleM, peopleU, peopleS, peopleRecordCount);
        this.tests = new LinearHashing<>(testsMain, testsOverflow, testsM, testsU, testsS, testsRecordCount);
    }

    public void close() throws IOException {
        this.people.close();
        this.tests.close();
        File file = new File(this.dataDirectoryPath, "db_config.txt");
        if (file.exists()) {
            file.delete();
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("blockSizePeopleMain=" + this.people.getMainFile().getBlockSize());
            writer.println("blockSizePeopleOverflow=" + this.people.getOverflowFile().getBlockSize());
            writer.println("blockSizeTestsMain=" + this.tests.getMainFile().getBlockSize());
            writer.println("blockSizeTestsOverflow=" + this.tests.getOverflowFile().getBlockSize());
            writer.println("peopleS=" + this.people.getS());
            writer.println("testsS=" + this.tests.getS());
            writer.println("peopleM=" + this.people.getM());
            writer.println("testsM=" + this.tests.getM());
            writer.println("peopleU=" + this.people.getU());
            writer.println("testsU=" + this.tests.getU());
            writer.println("peopleRecordCount=" + this.personId);
            writer.println("testsRecordCount=" + this.testId);
        }
    }

    //1
    public void insertTest(PCRTest test) throws Exception {
        test.setId(this.testId++);
        String patientId = test.getPatientId();
        PersonBinary person = findPersonById(patientId);
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
        PersonBinary old = this.findPersonById(personId);
        if (old == null) {
            throw new IllegalArgumentException("Person with id: " + personId + " not found.");
        }
        updatedPerson.setId(personId);
        updatedPerson.setTests(old.getTests());
        updatedPerson.setTestCount(old.getTestCount());
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

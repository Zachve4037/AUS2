package S2.Tester;

import S2.BaseClasses.PersonBinary;
import S2.HeapFile.HeapFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class BTester {
    private final int OPERATIONS = 10_000;
    private final int INSERT = 40;
    private final int DELETE = 70;
    public BTester() {
    }

    public void testHeapFile() {
        System.out.println("testHeapFile");
        try {
            for (int j = 1; j < 200000; j++) {
                File dataFile = new File("test_heap_" + j + ".dat");
                File metaFile = new File("test_heap_" + j + ".meta");
                if (dataFile.exists()) dataFile.delete();
                if (metaFile.exists()) metaFile.delete();

                RandomAccessFile raf = new RandomAccessFile(dataFile, "rw");

                HeapFile<PersonBinary> hf = new HeapFile<>(
                        raf,
                        () -> new PersonBinary(),
                        176,
                        512,
                        metaFile.getAbsolutePath()
                );

                HashMap<String, Integer> addressMap = new HashMap<>();
                ArrayList<PersonBinary> allPersons = new ArrayList<>();

                Random rng = new Random(j);

                for (int i = 0; i < OPERATIONS; i++) {
                    int operation = rng.nextInt(100);
                    if (operation < INSERT) {
                        PersonBinary person = generateRandomPerson(rng);
                        if (!addressMap.containsKey(person.getId())) {
                            int address = hf.insert(person);
                            if (address != -1) {
                                addressMap.put(person.getId(), address);
                                allPersons.add(person);
                            }
                        }
                    } else if (operation < DELETE) {
                        if (!allPersons.isEmpty()) {
                            int index = rng.nextInt(allPersons.size());
                            PersonBinary expected = allPersons.get(index);
                            int address = addressMap.get(expected.getId());

                            PersonBinary result = hf.get(address, expected);
                            if (result == null || !result.equals(expected)) {
                                throw new AssertionError("Get failed at iteration " + i);
                            }
                        }
                    } else {
                        if (!allPersons.isEmpty()) {
                            int index = rng.nextInt(allPersons.size());
                            PersonBinary toDelete = allPersons.get(index);
                            int address = addressMap.get(toDelete.getId());

                            hf.delete(address, toDelete);

                            PersonBinary shouldBeNull = hf.get(address, toDelete);
                            if (shouldBeNull != null) {
                                throw new AssertionError("Delete failed at iteration " + i);
                            }

                            addressMap.remove(toDelete.getId());
                            allPersons.remove(index);
                        }
                    }

                    if (i % 10_000 == 0) {

                        for (PersonBinary person : allPersons) {
                            int address = addressMap.get(person.getId());
                            PersonBinary retrieved = hf.get(address, person);
                            if (retrieved == null || !retrieved.equals(person)) {
                                throw new AssertionError("Validation failed at iteration " + i);
                            }
                        }
                    }
                }
                hf.insert(new PersonBinary("ýýýýýýýýýýýýýýý", "žžžžžžžžžžžžžž", new Date(), "ľľľľľľľľľľ"));

                hf.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }

    public void testLeftmostInsertion() {
        try {
            File dataFile = new File("test_leftmost.dat");
            File metaFile = new File("test_leftmost.meta");
            dataFile.deleteOnExit();
            metaFile.deleteOnExit();

            RandomAccessFile raf = new RandomAccessFile(dataFile, "rw");

            HeapFile<PersonBinary> hf = new HeapFile<>(
                    raf,
                    () -> new PersonBinary(),
                    176,
                    512,
                    metaFile.getAbsolutePath()
            );

            Random rng = new Random(42);
            ArrayList<PersonBinary> persons = new ArrayList<>();
            ArrayList<Integer> addresses = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                PersonBinary person = generateRandomPerson(rng);
                int address = hf.insert(person);

                persons.add(person);
                addresses.add(address);

            }

            int blockingFactor = 2;
            int expectedBlock = 0;
            int recordsInCurrentBlock = 0;
            for (int i = 0; i < addresses.size(); i++) {
                int actualBlock = addresses.get(i);
                if (actualBlock != expectedBlock) {
                    throw new AssertionError(
                            "Expected record " + i + " in block " + expectedBlock + " but was in block " + actualBlock
                    );
                }
                recordsInCurrentBlock++;
                if (recordsInCurrentBlock >= blockingFactor) {
                    expectedBlock++;
                    recordsInCurrentBlock = 0;
                }
            }

            PersonBinary del1 = persons.get(2);
            int addr1 = addresses.get(2);
            hf.delete(addr1, del1);
            if (hf.get(addr1, del1) != null) throw new AssertionError("Delete verification failed for index 2");

            PersonBinary del2 = persons.get(5);
            int addr2 = addresses.get(5);
            hf.delete(addr2, del2);
            if (hf.get(addr2, del2) != null) throw new AssertionError("Delete verification failed for index 5");

            PersonBinary del3 = persons.get(8);
            int addr3 = addresses.get(8);
            hf.delete(addr3, del3);
            if (hf.get(addr3, del3) != null) throw new AssertionError("Delete verification failed for index 8");

            PersonBinary newPerson = generateRandomPerson(rng);
            int newAddress = hf.insert(newPerson);

            int expectedLeftmost = Math.min(Math.min(addr1, addr2), addr3);
            if (newAddress != expectedLeftmost) {
                throw new AssertionError(
                        "Expected new record in block " + expectedLeftmost + " (leftmost partially free), but was in block " + newAddress
                );
            }

            PersonBinary another = generateRandomPerson(rng);
            int anotherAddr = hf.insert(another);
            if (anotherAddr < 0) {
                throw new AssertionError("Unexpected negative block address for subsequent insert");
            }

            hf.close();

        } catch (Exception e) {
            throw new RuntimeException("Leftmost insertion test failed", e);
        }
    }

    public void testLastBlockEmpty() {
        System.out.println("testLastBlockEmpty");
        try {
            File dataFile = new File("test_lastblock.dat");
            File metaFile = new File("test_lastblock.meta");
            dataFile.deleteOnExit();
            metaFile.deleteOnExit();

            RandomAccessFile raf = new RandomAccessFile(dataFile, "rw");

            HeapFile<PersonBinary> hf = new HeapFile<>(
                    raf,
                    () -> new PersonBinary(),
                    176,
                    512,
                    metaFile.getAbsolutePath()
            );

            Random rng = new Random(123);
            ArrayList<PersonBinary> persons = new ArrayList<>();
            ArrayList<Integer> addresses = new ArrayList<>();

            int blockingFactor = 2;

            for (int i = 0; i < blockingFactor + 1; i++) {
                PersonBinary p = generateRandomPerson(rng);
                int addr = hf.insert(p);
                if (addr < 0) throw new AssertionError("Insert returned negative address");
                persons.add(p);
                addresses.add(addr);
            }

            int lastIndex = persons.size() - 1;
            PersonBinary toDelete = persons.get(lastIndex);
            int deleteAddr = addresses.get(lastIndex);
            hf.delete(deleteAddr, toDelete);

            if (hf.get(deleteAddr, toDelete) != null) {
                throw new AssertionError("Delete verification failed for last block record");
            }

            int blockSize = 512;

            long fileBlocks = raf.length() / blockSize;
            if (deleteAddr >= fileBlocks) {
                hf.close();
                raf.close();
                return;
            }

            raf.seek((long) deleteAddr * blockSize);
            int validCount = raf.readInt();

            if (validCount != 0) {
                throw new AssertionError("Expected target block to be empty (validCount=0) but was: " + validCount);
            }

            hf.close();
            raf.close();
        } catch (Exception e) {
            throw new RuntimeException("Last block empty test failed", e);
        }
    }

    private PersonBinary generateRandomPerson(Random rng) {
        String name = generateRandomString(rng, 1, 15);
        String surname = generateRandomString(rng, 1, 14);
        String id = generateRandomString(rng, 10, 10);
        Date birthDate = new Date(Math.abs(rng.nextLong()) % System.currentTimeMillis());

        return new PersonBinary(name, surname, birthDate, id);
    }

    private String generateRandomString(Random rng, int minLength, int maxLength) {
        int length = minLength + rng.nextInt(maxLength - minLength + 1);
        StringBuilder sb = new StringBuilder();

        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789žščťýáíéúô";

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        }

        return sb.toString();
    }
}

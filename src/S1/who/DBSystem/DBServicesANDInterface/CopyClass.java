package S1.who.DBSystem.DBServicesANDInterface;

import S1.who.baseClasses.Person;
import S1.who.baseClasses.PCRTest;
import S1.who.wrappers.PCRT_DateTime;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CopyClass {
    private CopyClass() {}

    public static <T> T deepCopy(T obj) {
        if (obj == null) return null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            oos.flush();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (T) ois.readObject();
            }
        } catch (Exception ignored) {
        }
        return switch (obj) {
            case Person person -> (T) copyPerson(person);
            case PCRTest pcrTest -> (T) copyPCRTest(pcrTest);
            case List list -> (T) deepCopyList(list);
            default -> obj;
        };

    }

    public static <T> List<T> deepCopyList(List<T> list) {
        if (list == null) return Collections.emptyList();
        List<T> out = new ArrayList<>(list.size());
        for (T item : list) {
            out.add(deepCopy(item));
        }
        return out;
    }

    private static Person copyPerson(Person p) {
        if (p == null) return null;
        Date bd = p.getBirthDate() == null ? null : new Date(p.getBirthDate().getTime());
        Person cp = new Person(p.getID(), p.getName(), p.getSurname(), bd);

        try {
            List<PCRT_DateTime> tests = p.getAllTestsByDate();
            if (tests != null) {
                for (PCRT_DateTime wrapper : tests) {
                    if (wrapper == null) continue;
                    PCRTest originalTest = wrapper.getTest();
                    PCRTest testCopy = copyPCRTest(originalTest);
                    cp.addTest(testCopy);
                }
            }
        } catch (Exception ignored) {
        }
        return cp;
    }

    private static Person shallowCopyPerson(Person p) {
        if (p == null) return null;
        Date bd = p.getBirthDate() == null ? null : new Date(p.getBirthDate().getTime());
        return new Person(p.getID(), p.getName(), p.getSurname(), bd);
    }

    private static PCRTest copyPCRTest(PCRTest t) {
        if (t == null) return null;
        PCRTest cp = new PCRTest(
                t.getTimeOfTest(),
                t.getPersonID(),
                t.getPcrID(),
                t.getWorkplaceID(),
                t.getRegionID(),
                t.getDistrictID(),
                t.getTestResult(),
                t.getTestValue(),
                t.getNote()
        );
        try {
            Person originalPerson = t.getPerson();
            if (originalPerson != null) {
                cp.setPerson(shallowCopyPerson(originalPerson));
                cp.setPersonID(originalPerson.getID());
            }
        } catch (Exception ignored) {
        }
        return cp;
    }
}

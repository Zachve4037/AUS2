package who.DBSystem.DBServicesANDInterface;

import who.baseClasses.Person;
import who.baseClasses.PCRTest;
import who.wrappers.PCRT_DateTime;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CopyClass {
    private CopyClass() {}

    @SuppressWarnings("unchecked")
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
        } catch (NotSerializableException nse) {
        } catch (Exception ignored) {
        }
        if (obj instanceof Person) {
            return (T) copyPerson((Person) obj);
        } else if (obj instanceof PCRTest) {
            return (T) copyPCRTest((PCRTest) obj);
        } else if (obj instanceof List) {
            return (T) deepCopyList((List<?>) obj);
        }

        return obj;
    }

    public static <T> List<T> deepCopyList(List<T> list) {
        if (list == null) return null;
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

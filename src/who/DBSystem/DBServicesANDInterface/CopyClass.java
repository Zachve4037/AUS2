package who.DBSystem.DBServicesANDInterface;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CopyClass {
    private CopyClass() {}

    public static <T extends Serializable> T deepCopy(T obj) {
        if (obj == null) return null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            oos.flush();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (T) ois.readObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("Deep copy failed", e);
        }
    }

    public static <T extends Serializable> List<T> deepCopyList(List<T> list) {
        if (list == null) return Collections.emptyList();
        List<T> res = new ArrayList<>(list.size());
        for (T item : list) {
            res.add(deepCopy(item));
        }
        return res;
    }
}

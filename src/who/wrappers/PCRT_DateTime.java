package who.wrappers;

import who.Exportable;
import who.baseClasses.PCRTest;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PCRT_DateTime implements Comparable<PCRT_DateTime>, Serializable, Exportable {
    private static final long serialVersionUID = 1L;
    private final PCRTest test;

    public PCRT_DateTime(PCRTest test) {
        this.test = test;
    }

    public PCRTest getTest() {
        return test;
    }

    @Override
    public int compareTo(PCRT_DateTime o) {
        if (o == null) return 1;
        LocalDateTime self = this.test.getTimeOfTest();
        LocalDateTime other = o.getTest().getTimeOfTest();
        if (self == null && other == null) {
            return this.test.compareTo(o.getTest());
        }
        if (self == null) return -1;
        if (other == null) return 1;
        int cmp = self.compareTo(other);
        return (cmp != 0) ? cmp : this.test.compareTo(o.getTest());
    }

    @Override
    public String serialize() {
        return this.test.serialize();
    }

    @Override
    public void deserialize(String data) {
        this.test.deserialize(data);
    }
}

package S2.heapFile;

public interface IRecord {
    boolean equals(IRecord other);
    int getSize();
    byte[] getBytes();
    void fromBytes(byte[] bytes);
}

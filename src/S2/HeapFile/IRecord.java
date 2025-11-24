package S2.HeapFile;

public interface IRecord {
    boolean equals(IRecord other);
    int getSize();
    byte[] getBytes();
    void fromBytes(byte[] bytes);
}

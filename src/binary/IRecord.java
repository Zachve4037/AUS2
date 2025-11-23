package binary;

public interface IRecord {
    boolean myEquals(IRecord other);
    int getSize();
    byte[] getBytes();
    void fromBytes(byte[] bytes);
}

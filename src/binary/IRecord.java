package binary;

public interface IRecord<T> {
    boolean myEquals(T other);
    int getSize();
    byte[] getBytes();
    void fromBytes(byte[] bytes);
}

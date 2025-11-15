package binary;

public interface IData<T extends IRecord> {
    T createClass();
    boolean equals(T other);
}

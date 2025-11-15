package binary;

public class HeapFile<T extends IRecord> {
    private int recordSize;
    private String filePath;

    HeapFile(int recordSize,String filePath) {
        this.recordSize = recordSize;
        this.filePath = filePath;
    }

    public long insert(T record) {

    }

    public T get(int address, T data) {

    }

    public void delete(int address, T data) {

    }

    public void cutTail() {

    }
}

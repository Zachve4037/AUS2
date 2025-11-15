package binary;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class BinaryFile<T extends IRecord> {
    private final RandomAccessFile file;
    private final IData<T> type;
    private final int blockSize;

    BinaryFile(String filePath, IData<T> type, int blockSize) throws FileNotFoundException {
        this.file = new RandomAccessFile(filePath, "rw");
        this.type = type;
        this.blockSize = blockSize;
    }

    public void seak() {

    }

    public int size() {
        return 0;
    }
}

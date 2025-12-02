package S2.linearHashing;


import S2.heapFile.HeapFile;
import S2.heapFile.IRecord;

import java.io.IOException;
import java.util.List;

public class LinearHashing<T extends IRecord & IHash> {
    private HeapFile<T> mainFile;
    private HeapFile<T> overflowFile;
    private final ISplitPolicy splitPolicy;
    private final int M; // initial count(buckets)
    private int u; // level
    private int S; // split pointer
    private final int blockCapacity;
    private long recordCount;

    private static final double MAX_DENSITY = 0.75;

    public LinearHashing(HeapFile<T> mainFile, HeapFile<T> overflowFile, ISplitPolicy splitPolicy, int initialBucketCount, int blockCapacity) {
        this.mainFile = mainFile;
        this.overflowFile = overflowFile;
        this.splitPolicy = splitPolicy;
        this.blockCapacity = blockCapacity;
        this.M = initialBucketCount;
        this.u = 0;
        this.S = 0;
        this.recordCount = 0;
    }

    public void insert(T record) throws IOException {

    }

    public T get(T record) throws IOException {
        return null;
    }

    private void split() throws IOException {

    }

    // zozbiera vsetky recordy z daneho bucketu (main + overflow)
    private List<T> collectBucket(int bucket) throws IOException {
        return null;
    }

    private int resolveBucket(long key) {
        int base = M << u;
        int i = (int) (key % base);

        if (i < S) {
            i = (int) (key % (base << 1));
        }
        return i;
    }
}

package S2.linearHashing;

import S2.heapFile.HeapFile;
import S2.heapFile.IRecord;
import java.io.IOException;
import java.util.LinkedList;
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

    public LinearHashing(HeapFile<T> mainFile, HeapFile<T> overflowFile, ISplitPolicy splitPolicy, int initialBucketCount, int blockCapacity) throws IOException {
        this.mainFile = mainFile;
        this.overflowFile = overflowFile;
        this.splitPolicy = splitPolicy;
        this.blockCapacity = blockCapacity;
        this.M = initialBucketCount;
        this.u = 0;
        this.S = 0;
        this.recordCount = 0;

        // Initialize main file with M empty buckets
        for (int i =0; i < M; i++) {
            LHBlock<T> block = new LHBlock<>(this.blockCapacity, mainFile.getType());
            this.mainFile.writeBlock(i, block);
        }
    }

    public void insert(T record) throws IOException {
        if (record == null) return;
        long key = record.getHashCode();
        int bucketAddr = findBucket(key);
        LHBlock<T> bucket = (LHBlock<T>) this.mainFile.readBlock(bucketAddr);
        if (!bucket.isFull()) {
            bucket.getData()[bucket.getValidCount()] = record;
            bucket.setValidCount(bucket.getValidCount() + 1);
            this.mainFile.writeBlock(bucketAddr, bucket);
            this.recordCount++;
        } else {
            /// TODO: doriesit ak je overflow block plny a nema nasledovnika
            /// TODO: doriesit ak je main plny a overflow je prazdny
            int overflowAddr = bucket.getNextBlockAddr();
            while (overflowAddr != -1) {
                LHBlock<T> overflowBucket = (LHBlock<T>) this.overflowFile.readBlock(overflowAddr);
                if (!overflowBucket.isFull()) {
                    overflowBucket.getData()[overflowBucket.getValidCount()] = record;
                    overflowBucket.setValidCount(overflowBucket.getValidCount() + 1);
                    this.overflowFile.writeBlock(overflowAddr, overflowBucket);
                    this.recordCount++;
                } else {
                    overflowAddr = overflowBucket.getNextBlockAddr();
                }
            }
        }
    }

    public T get(T wanted) throws IOException {
        if (wanted == null) return null;
        long key = wanted.getHashCode();
        int bucket = findBucket(key);
        List<T> records = collectRecords(bucket);
        if (records != null) {
            for (T rec : records) {
                if (rec.equals(wanted)) {
                    return rec;
                }
            }
        }
        return null;
    }

    private void split() throws IOException {

    }

    private List<T> collectRecords(int bucketAddr) throws IOException {
        if (bucketAddr < 0) return null;
        List<T> records = new LinkedList<>();
        LHBlock<T> bucket = (LHBlock<T>) this.mainFile.readBlock(bucketAddr);
        for (int i =0; i < bucket.getValidCount(); i++) {
            records.add(bucket.getData()[i]);
        }
        int overFlowAddr = bucket.getNextBlockAddr();
        while (overFlowAddr != -1) {
            LHBlock<T> bucketOverflow = (LHBlock<T>) this.overflowFile.readBlock(overFlowAddr);
            for (int i = 0; i < bucketOverflow.getValidCount(); i++) {
                records.add(bucketOverflow.getData()[i]);
            }
            overFlowAddr = bucketOverflow.getNextBlockAddr();
        }
        return records;
    }

    private int findBucket(long key) {
        int base = M << u;
        int i = (int) (key % base);

        if (i < S) {
            i = (int) (key % (base << 1));
        }
        return i;
    }
}

package S2.linearHashing;

import S2.heapFile.Block;
import S2.heapFile.HeapFile;
import S2.heapFile.IRecord;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class LinearHashing<T extends IRecord & IHash> {
    private final LinkedList<Integer> bucketInsertCounts;
    private HeapFile<T> mainFile;
    private HeapFile<T> overflowFile;
    private final ISplitPolicy splitPolicy;
    private final int M; // initial count(buckets)
    private int u; // level
    private int S; // split pointer
    private final int blockCapacity;
    private long recordCount;

    private static final double MAX_DENSITY = 0.75;

    public LinearHashing(HeapFile<T> mainFile, HeapFile<T> overflowFile, ISplitPolicy splitPolicy, int initialBuckets, int blockCapacity) {
        this.mainFile = mainFile;
        this.overflowFile = overflowFile;
        this.splitPolicy = splitPolicy;
        this.blockCapacity = blockCapacity;
        this.M = initialBuckets;
        this.u = 0;
        this.S = 0;
        this.recordCount = 0;
        this.bucketInsertCounts = new LinkedList<>();
        for (int i = 0; i < M; i++) this.bucketInsertCounts.add(Integer.valueOf(0));
    }

    public void insert(T record) throws IOException {
        if (record == null) return;
        long key = record.getHashCode();
        int bucket = resolveBucket(key);

        boolean placedInMain = insertIntoMain(bucket, record);

        if (!placedInMain) {
            insertIntoOverflow(bucket, record);
        }

        this.recordCount++;
        this.ensureBucketCounterSize(bucket);
        this.bucketInsertCounts.set(bucket, Integer.valueOf(bucketInsertCounts.get(bucket) + 1));

        int allocatedBuckets = (this.M << u) + S;
        if (splitPolicy.split(this.recordCount, allocatedBuckets, this.blockCapacity)) {
            this.split();
        }
    }

    public T get(T record) throws IOException {
        if (record == null) return null;
        long key = record.getHashCode();
        int bucket = resolveBucket(key);

        return this.search(bucket, record);
    }

    private T search(int bucket, T record) throws IOException {
        //main file search
        Block<T> block = this.mainFile.readBlock(bucket);

        for (int i = 0; i < block.getValidCount(); i++) {
            T rec = block.getData()[i];
            if (rec.equals(record)) {
                return rec;
            }
        }
        //overflow file search
        /// TODO: implement overflow search

        return null;
    }


    private boolean insertIntoMain(int bucket, T record) throws IOException {
        Block<T> block  = mainFile.readBlock(bucket);
        if (!block.isFull()) {
            block.getData()[block.getValidCount()] = record;
            block.setValidCount(block.getValidCount() + 1);

            /// TODO: // update header: totalRecords++ (main+overflow)
    ///             // and chainLength remains the same (unless we added overflow)
    ///             // TODO: MainBlock-specific methods, e.g., block.incrementTotalRecords();
    ///             // Use HeapFile / MainBlock API to update header fields

            this.mainFile.writeBlock(bucket, block);
            return true;
        }
        return false;
    }

    private void insertIntoOverflow(int bucket, T record) {
        /// TODO: na zaklade heapFile insert
    }

    private void split() throws IOException {
        int old = S;
        int neew = S + (M << u);

        this.mainFile.insertEmptyBlockAt(neew);
        List<T> records = collectBucket(old);

        for (T rec : records) {
            int target = (int) rec.getHashCode() % (M << (u + 1));
            if (target == old) {
                insertIntoMain(old, rec);
            } else {
                insertIntoMain(neew, rec);
            }
        }

        ensureBucketCounterSize(neew);

        S++;
        if (S == (M << u)) {
            S = 0;
            u++;
        }
    }

    private void ensureBucketCounterSize(int index) {
        while (index >= bucketInsertCounts.size()) {
            bucketInsertCounts.add(0);
        }
    }

    // zozbiera vsetky recordy z daneho bucketu (main + overflow)
    private List<T> collectBucket(int bucket) throws IOException {
        List<T> records = new LinkedList<>();
        Block<T> block = this.mainFile.readBlock(bucket);
        for (int i =0; i < block.getValidCount(); i++) {
            records.add(block.getData()[i]);
        }
        /// TODO: collect from overflow file
        return records;
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

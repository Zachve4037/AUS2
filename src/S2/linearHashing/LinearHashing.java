package S2.linearHashing;

import S2.Main;
import S2.heapFile.Block;
import S2.heapFile.HeapFile;
import S2.heapFile.IRecord;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;

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

    public LinearHashing(HeapFile<T> mainFile, HeapFile<T> overflowFile, ISplitPolicy splitPolicy, int initialBucketCount, int blockCapacity) {
        this.mainFile = mainFile;
        this.overflowFile = overflowFile;
        this.splitPolicy = splitPolicy;
        this.blockCapacity = blockCapacity;
        this.M = initialBucketCount;
        this.u = 0;
        this.S = 0;
        this.recordCount = 0;
        this.bucketInsertCounts = new LinkedList<>();
        for (int i = 0; i < M; i++) this.bucketInsertCounts.add(0);
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
        this.bucketInsertCounts.set(bucket, bucketInsertCounts.get(bucket) + 1);

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
        return searchOverflow(bucket, record);
    }

    private T searchOverflow(int bucket, T record) throws IOException {
        Block<T> mainBlock  = this.mainFile.readBlock(bucket);

        if (!(mainBlock instanceof MainBlock<T>)) {
            return null;
        }

        MainBlock<T> mb = (MainBlock<T>) mainBlock;
        if (mb.getChainLength() == 0) {
            return null;
        }

        int addr = findFirstOverflowBlock(bucket);
        while (addr !=  -1) {
            LHBlock<T> overflowBlock = (LHBlock<T>) this.overflowFile.readBlock(addr);
            for (int i = 0; i < overflowBlock.getValidCount(); i++) {
                T rec = overflowBlock.getData()[i];
                if (rec.equals(record)) {
                    return rec;
                }
            }
            addr = overflowBlock.getNextBlock();
        }
        return null;
    }


    private boolean insertIntoMain(int bucket, T record) throws IOException {
        Block<T> block  = mainFile.readBlock(bucket);
        if (!block.isFull()) {
            block.getData()[block.getValidCount()] = record;
            block.setValidCount(block.getValidCount() + 1);

            if (block instanceof MainBlock) {
                MainBlock<T> mb = (MainBlock<T>) block;
                mb.setRecordCount(mb.getRecordCount() + 1);
            }

            this.mainFile.writeBlock(bucket, block);
            return true;
        }
        return false;
    }

    private void insertIntoOverflow(int bucket, T record) throws IOException {
        Block<T> blockFromMain = this.mainFile.readBlock(bucket);
        MainBlock<T> mb = (blockFromMain instanceof MainBlock) ? (MainBlock<T>) blockFromMain : null;

        int lastOverflowAddr = this.findLastOverflowBlock(bucket);

        if (lastOverflowAddr == -1) {
            int newAddr = this.overflowFile.getTotalBlockCount();
            LHBlock<T> newBlock = new LHBlock<>(this.blockCapacity, this.mainFile.getType());
            newBlock.getData()[0] = record;
            newBlock.setValidCount(1);
            newBlock.setNextBlock(-1);
            this.overflowFile.writeBlock(newAddr, newBlock);

            if (mb != null) {
                mb.setChainLength(1);
                mb.setRecordCount(mb.getRecordCount() + 1);
                this.mainFile.writeBlock(bucket, mb);
            }
        } else {
            LHBlock<T> lastBlock = (LHBlock<T>) this.overflowFile.readBlock(lastOverflowAddr);
            if (!lastBlock.isFull()) {
                lastBlock.getData()[lastBlock.getValidCount()] = record;
                lastBlock.setValidCount(lastBlock.getValidCount() + 1);
                this.overflowFile.writeBlock(lastOverflowAddr, lastBlock);

                if (mb != null) {
                    mb.setRecordCount(mb.getRecordCount() + 1);
                    this.mainFile.writeBlock(bucket, mb);
                }
            } else {
                int newAddr = this.overflowFile.getTotalBlockCount();
                LHBlock<T> newBlock = new LHBlock<>(this.blockCapacity, this.mainFile.getType());
                newBlock.getData()[0] = record;
                newBlock.setValidCount(1);
                newBlock.setNextBlock(-1);
                this.overflowFile.writeBlock(newAddr, newBlock);

                lastBlock.setNextBlock(newAddr);
                this.overflowFile.writeBlock(lastOverflowAddr, lastBlock);

                if (mb != null) {
                    mb.setChainLength(mb.getChainLength() + 1);
                    mb.setRecordCount(mb.getRecordCount() + 1);
                    this.mainFile.writeBlock(bucket, mb);
                }
            }
        }
        if (mb != null) {
            mb.setRecordCount(mb.getRecordCount() + 1);
            this.mainFile.writeBlock(bucket, mb);
        }
    }

    private int findLastOverflowBlock(int bucket) throws IOException {
        int current = findFirstOverflowBlock(bucket);
        if (current == -1) return -1;

        while (true) {
            LHBlock<T> block = (LHBlock<T>) this.overflowFile.readBlock(current);
            if (block.getNextBlock() == -1) return current;
            current = block.getNextBlock();
        }
    }

    private int findFirstOverflowBlock(int bucket) throws IOException {
        Block<T> blockFromMain = this.mainFile.readBlock(bucket);
        if (blockFromMain instanceof MainBlock) {
            return ((MainBlock<T>) blockFromMain).getChainLength() > 0 ? 0 : -1;
        }
        return -1;
    }

    private void split() throws IOException {
        int old = S;
        int neew = S + (M << u);

        this.mainFile.insertEmptyBlockAt(neew);
        List<T> records = collectBucket(old);
        clearBucket(old);

        for (T rec : records) {
            int target = (int) rec.getHashCode() % (M << (u + 1));
            boolean inserted = this.insertIntoMain(target, rec);
            if (!inserted) {
                this.insertIntoOverflow(target, rec);
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

        int curAddr = findFirstOverflowBlock(bucket);
        while (curAddr != -1) {
            LHBlock<T> overflowBlock = (LHBlock<T>) this.overflowFile.readBlock(curAddr);
            for (int i = 0; i < overflowBlock.getValidCount(); i++) {
                records.add(overflowBlock.getData()[i]);
            }
            curAddr = overflowBlock.getNextBlock();
        }
        return records;
    }

    private void clearBucket(int bucket) throws IOException {
        Block<T> blockFromMain = this.mainFile.readBlock(bucket);
        blockFromMain.setValidCount(0);

        if (blockFromMain instanceof MainBlock) {
            MainBlock<T> mb = (MainBlock<T>) blockFromMain;
            mb.setRecordCount(0);
            mb.setChainLength(0);
        }

        this.mainFile.writeBlock(bucket, blockFromMain);

        int curAddr = findFirstOverflowBlock(bucket);
        while (curAddr != -1) {
            LHBlock<T> overflowBlock = (LHBlock<T>) this.overflowFile.readBlock(curAddr);
            int nextAddr = overflowBlock.getNextBlock();
            overflowBlock.setValidCount(0);
            overflowBlock.setNextBlock(-1);
            this.overflowFile.writeBlock(curAddr, overflowBlock);
            curAddr = nextAddr;
        }
        this.overflowFile.cutEmptyFileBlocks();
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

package S2.linearHashing;

import S2.heapFile.HeapFile;
import S2.heapFile.IRecord;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class LinearHashing<T extends IRecord & IHash> {
    private HeapFile<T> mainFile;
    private HeapFile<T> overflowFile;
    private final int M; // initial count(buckets)
    private int u; // level
    private int S; // split pointer
    private long recordCount;

    private static final double MAX_DENSITY = 0.75;

    public LinearHashing(HeapFile<T> mainFile, HeapFile<T> overflowFile, int initialBucketCount) throws IOException {
        this.mainFile = mainFile;
        this.overflowFile = overflowFile;
        this.M = initialBucketCount;
        this.u = 0;
        this.S = 0;
        this.recordCount = 0;

        // Initialize main file with M empty buckets
        for (int i =0; i < M; i++) {
            LHBlock<T> block = new LHBlock<>(this.mainFile.getBlockingFactor(), mainFile.getType());
            this.mainFile.writeBlock(i, block);
        }
    }

    public void insert(T record) throws IOException {
        if (record == null) return;
        long key = record.getHashCode();
        int bucketAddr = findBucket(key);
        LHBlock<T> bucket = (LHBlock<T>) this.mainFile.readBlock(bucketAddr);
        //if block from main has space
        if (!bucket.isFull()) {
            bucket.getData()[bucket.getValidCount()] = record;
            bucket.setValidCount(bucket.getValidCount() + 1);
            this.mainFile.writeBlock(bucketAddr, bucket);
            this.recordCount++;
            return;
        }

        //if block from main has some ancestor in overflow file but does not handle method if all are full and none has space
        if (bucket.isFull() && bucket.getNextBlockAddr() != -1) {
            int overflowAddr = bucket.getNextBlockAddr();
            while (overflowAddr != -1) {
                LHBlock<T> overflowBucket = (LHBlock<T>) this.overflowFile.readBlock(overflowAddr);
                if (!overflowBucket.isFull()) {
                    overflowBucket.getData()[overflowBucket.getValidCount()] = record;
                    overflowBucket.setValidCount(overflowBucket.getValidCount() + 1);
                    this.overflowFile.writeBlock(overflowAddr, overflowBucket);
                    this.recordCount++;
                    return;
                } else {
                    overflowAddr = overflowBucket.getNextBlockAddr();
                }
            }
            //insert into new overflow file new block
            LHBlock<T> newOverflowBucket = new LHBlock<>(this.overflowFile.getBlockingFactor(), this.overflowFile.getType());
            newOverflowBucket.getData()[newOverflowBucket.getValidCount()] = record;
            newOverflowBucket.setValidCount(newOverflowBucket.getValidCount() + 1);
            newOverflowBucket.setNextBlockAddr(-1);
            /// TODO: get the address of new block in overflow file
        }

        //if block from main is full and has no ancestor in overflow file
        if(bucket.isFull() && bucket.getNextBlockAddr() == -1) {
            LHBlock<T> newOverflowBucket = new LHBlock<>(this.overflowFile.getBlockingFactor(), this.overflowFile.getType());
            newOverflowBucket.getData()[newOverflowBucket.getValidCount()] = record;
            newOverflowBucket.setValidCount(newOverflowBucket.getValidCount() + 1);
            newOverflowBucket.setNextBlockAddr(-1);
            /// TODO: get the address of new block in overflow file
        }

        /// TODO: split if needed
    }

    public T get(T wanted) throws IOException {
        if (wanted == null) return null;
        long key = wanted.getHashCode();
        int bucket = findBucket(key);
        LHBlock<T> mainBucket = (LHBlock<T>) this.mainFile.readBlock(bucket);
        for (int i = 0; i < mainBucket.getValidCount(); i++) {
            if (mainBucket.getData()[i].equals(wanted)) {
                return mainBucket.getData()[i];
            }
        }
        int overflowAddr = mainBucket.getNextBlockAddr();
        while (overflowAddr != -1) {
            LHBlock<T> overflowBucket = (LHBlock<T>) this.overflowFile.readBlock(overflowAddr);
            for (int i = 0; i < overflowBucket.getValidCount(); i++) {
                if (overflowBucket.getData()[i].equals(wanted)) {
                    return overflowBucket.getData()[i];
                }
            }
            overflowAddr = overflowBucket.getNextBlockAddr();
        }
        return null;
    }

    private void split() throws IOException {
        int oldBucketAddr = this.S;
        int newBucketAddr = (int) (S + (M * Math.pow(2, u)));

        // 1.a nacitanie blokov
        List<LHBlock<T>> oldBlocks = this.collectBlocks(oldBucketAddr);
        // 1.b nacitanie zaznamov z blokov
        List<T> records = new LinkedList<>();
        for (LHBlock<T> block : oldBlocks) {
            for (int i = 0; i < block.getValidCount(); i++) {
                if (block == null) {
                    throw new NullPointerException("Split: block is null");
                }
                records.add(block.getData()[i]);
            }
        }

        List<Integer> overflowAddresses = new LinkedList<>();
        for (int i = 1; i < oldBlocks.size(); i++) {
            overflowAddresses.add(oldBlocks.get(i).getNextBlockAddr());
        }

        this.S = this.S + 1;
        if (this.S >= (M * Math.pow(2, u))) {
            this.S = 0;
            this.u = this.u + 1;
        }

        //2 rozdelenie zaznamov
        List<T> recordsToOld = new LinkedList<>();
        List<T> recordsToNew = new LinkedList<>();
        for (T rec : records) {
            int bucketAddr = findBucket(rec.getHashCode());
            if (bucketAddr == oldBucketAddr) {
                recordsToOld.add(rec);
            } else if (bucketAddr == newBucketAddr){
                recordsToNew.add(rec);
            }
        }

        int usedByOld = fillConcatenation(recordsToOld, oldBucketAddr, overflowAddresses, 0);
        int usedByNew = fillConcatenation(recordsToNew, newBucketAddr, overflowAddresses, usedByOld);

        for (int i = usedByOld + usedByNew; i < overflowAddresses.size(); i++) {
            ///TODO: treba uvolnit nepouzite overflow bloky
        }
    }

    private int fillConcatenation(
            List<T> records,
            int bucketAddr,
            List<Integer> addresses,
            int startIndex
    ) throws IOException {
        //naplnenie hlavneho bloku
        LHBlock<T> newMainBlock = new LHBlock<>(this.mainFile.getBlockingFactor(), this.mainFile.getType());
        int recordIndex = 0;

        //naplnam iba pokym nie je plny, ci aj nejaky dalsi check?
        while(!newMainBlock.isFull()) {
            newMainBlock.getData()[newMainBlock.getValidCount()] = records.get(recordIndex);
            newMainBlock.setValidCount(newMainBlock.getValidCount() + 1);
            recordIndex++;
        }
        // overflow index, aby sme pouzili este dostupne adresy
        int overflowCount = startIndex;
        LHBlock<T> previousBlock = newMainBlock;

        //naplnenie overflow blokov
        while (recordIndex < records.size()){
            int overflowAddr = addresses.get(overflowCount);
            LHBlock<T> overflowBlock = new LHBlock<>(this.overflowFile.getBlockingFactor(), this.overflowFile.getType());

            //to iste co s mainom
            while (!overflowBlock.isFull()) {
                overflowBlock.getData()[overflowBlock.getValidCount()]  = records.get(recordIndex);
                overflowBlock.setValidCount(overflowBlock.getValidCount() + 1);
                recordIndex++;
            }
            previousBlock.setNextBlockAddr(overflowAddr);
            this.overflowFile.writeBlock(overflowAddr, overflowBlock);
            previousBlock = overflowBlock;
            overflowCount++;
        }

        this.mainFile.writeBlock(bucketAddr, newMainBlock);
        previousBlock.setNextBlockAddr(-1);
        return overflowCount;
    }

    private List<LHBlock<T>> collectBlocks(int bucketAddr) throws IOException {
        if (bucketAddr < 0) return null;
        List<LHBlock<T>> blocks = new LinkedList<>();
        LHBlock<T> mainBlock = (LHBlock<T>) this.mainFile.readBlock(bucketAddr);
        blocks.add(mainBlock);
        int overflowAddr = mainBlock.getNextBlockAddr();
        while (overflowAddr != -1) {
            LHBlock<T> overflowBlock = (LHBlock<T>) this.overflowFile.readBlock(overflowAddr);
            blocks.add(overflowBlock);
            overflowAddr = overflowBlock.getNextBlockAddr();
        }
        return blocks;
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

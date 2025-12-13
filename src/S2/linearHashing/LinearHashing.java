package S2.linearHashing;

import S2.heapFile.HeapFile;
import S2.heapFile.IRecord;

import javax.management.InvalidApplicationException;
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

    private static final double MAX_DENSITY = 0.8;

    public LinearHashing(HeapFile<T> mainFile, HeapFile<T> overflowFile, int initialBucketCount) throws IOException {
        this.mainFile = mainFile;
        this.overflowFile = overflowFile;
        this.M = initialBucketCount;
        this.u = 0;
        this.S = 0;
        this.recordCount = 0;

        //inicializacia hlavneho suboru s prazdnymi blokmi
        for (int i =0; i < M; i++) {
            this.mainFile.insertEmptyBlockAt(i);
        }
    }

    public void insert(T record) throws Exception {
        if (record == null) return;
        long key = record.getHashCode();
        int bucketAddr = findBucket(key);
        LHBlock<T> bucket = (LHBlock<T>) this.mainFile.readBlock(bucketAddr);
        //ak ma block v main file miesto
        if (!bucket.isFull()) {
            bucket.getData()[bucket.getValidCount()] = record;
            bucket.setValidCount(bucket.getValidCount() + 1);
            this.mainFile.writeBlock(bucketAddr, bucket);
            this.recordCount++;
            if (this.performSplit()) {
                this.split();
            }
            return;
        }

        //ak ma blok v maine nejakeho nasledovnika v overflow file
        if (bucket.isFull() && bucket.getNextBlockAddr() != -1) {
            int overflowAddr = bucket.getNextBlockAddr();
            int lastValidOverflowAddr = -1;
            LHBlock<T> overflowBucket = null;
            while (overflowAddr != -1) {
                overflowBucket = (LHBlock<T>) this.overflowFile.readBlock(overflowAddr);
                if (!overflowBucket.isFull()) {
                    overflowBucket.getData()[overflowBucket.getValidCount()] = record;
                    overflowBucket.setValidCount(overflowBucket.getValidCount() + 1);
                    this.overflowFile.writeBlock(overflowAddr, overflowBucket);
                    this.recordCount++;
                    if (this.performSplit()) {
                        this.split();
                    }
                    return;
                } else {
                    lastValidOverflowAddr = overflowAddr;
                    overflowAddr = overflowBucket.getNextBlockAddr();
                }
            }
            //ak blok v maine je plny a ma nasledovnika v overflow file ale vsetky su plne
            //do overflowu pridame novy blok
            int newOverflowAddr = this.overflowFile.insert(record);
            overflowBucket.setNextBlockAddr(newOverflowAddr);
            this.overflowFile.writeBlock(lastValidOverflowAddr, overflowBucket);
            this.recordCount++;
            if (this.performSplit()) {
                this.split();
            }
            return;
        }

        //ak blok v main file je plny a nema nasledovnika v overflow
        if(bucket.isFull() && bucket.getNextBlockAddr() == -1) {
            int newOverflowAddr = this.overflowFile.insert(record);
            bucket.setNextBlockAddr(newOverflowAddr);
            this.mainFile.writeBlock(bucketAddr, bucket);
            this.recordCount++;
            if (this.performSplit()) {
                this.split();
            }
        }
    }

    private boolean performSplit() {
        int mainBlocks = (int) (M * Math.pow(2, u)) + S;
        int mainCapacity = mainBlocks * this.mainFile.getBlockingFactor();
        int overflowCapacity = this.overflowFile.getTotalBlockCount() * this.overflowFile.getBlockingFactor();
        int totalCapacity = mainCapacity + overflowCapacity;
        double currentDensity = (double) this.recordCount / (double) totalCapacity;
        return currentDensity > MAX_DENSITY;
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

    private void split() throws Exception {
        int oldBucketAddr = this.S;
        int newBucketAddr = (int) (S + (M * Math.pow(2, u)));

        List<LHBlock<T>> oldBlocks = new LinkedList<>();
        List<Integer> overflowAddresses = new LinkedList<>();

        LHBlock<T> mainBlock = (LHBlock<T>) this.mainFile.readBlock(oldBucketAddr);
        oldBlocks.add(mainBlock);

        int overflowAddr = mainBlock.getNextBlockAddr();
        while (overflowAddr != -1) {
            overflowAddresses.add(overflowAddr);
            LHBlock<T> overflowBlock = (LHBlock<T>) this.overflowFile.readBlock(overflowAddr);
            oldBlocks.add(overflowBlock);
            overflowAddr = overflowBlock.getNextBlockAddr();
        }

        List<T> records = new LinkedList<>();
        for (LHBlock<T> block : oldBlocks) {
            for (int i = 0; i < block.getValidCount(); i++) {
                records.add(block.getData()[i]);
            }
        }

        this.S = this.S + 1;
        if (this.S >= (M * Math.pow(2, u))) {
            this.S = 0;
            this.u = this.u + 1;
        }

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

        int usedByOld = fillChain(recordsToOld, oldBucketAddr, overflowAddresses, 0);
        int usedByNew = fillChain(recordsToNew, newBucketAddr, overflowAddresses, usedByOld);
        for (int i = usedByOld + usedByNew; i < overflowAddresses.size(); i++) {
            this.overflowFile.insertEmptyBlockAt(overflowAddresses.get(i));
        }
        this.overflowFile.cutEmptyFileBlocks();
    }

    private int fillChain(
            List<T> records,
            int bucketAddr,
            List<Integer> addresses,
            int startIndex
    ) throws IOException {
        //naplnenie hlavneho bloku
        LHBlock<T> mainBlock;
        if(bucketAddr < this.mainFile.getTotalBlockCount()) {
            mainBlock = (LHBlock<T>) this.mainFile.readBlock(bucketAddr);
            mainBlock.setValidCount(0);
            mainBlock.setNextBlockAddr(-1);
        } else {
            this.mainFile.insertEmptyBlockAt(bucketAddr);
            mainBlock = new LHBlock<>(this.mainFile.getBlockingFactor(), this.mainFile.getType());
        }

        int recordIndex = 0;

        //naplnam iba pokym nie je plny
        while(!mainBlock.isFull() && recordIndex < records.size()) {
            mainBlock.getData()[mainBlock.getValidCount()] = records.get(recordIndex);
            mainBlock.setValidCount(mainBlock.getValidCount() + 1);
            recordIndex++;
        }

        //ak sa uz zmestili vsetky zaznamy koniec
        if (recordIndex >= records.size()) {
            mainBlock.setNextBlockAddr(-1);
            this.mainFile.writeBlock(bucketAddr, mainBlock);
            return 0;
        }

        // overflow index, aby sme pouzili este dostupne adresy
        int overflowCount = startIndex;
        LHBlock<T> previousBlock = mainBlock;

        //naplnenie overflow blokov
        while (recordIndex < records.size()){
            int overflowAddr;
            LHBlock<T> overflowBlock;
            if (overflowCount < addresses.size()) {
                overflowAddr = addresses.get(overflowCount);
                overflowBlock = (LHBlock<T>) this.overflowFile.readBlock(overflowAddr);
                overflowBlock.setValidCount(0);
                overflowBlock.setNextBlockAddr(-1);
            } else {
                overflowAddr = this.overflowFile.getTotalBlockCount();
                this.overflowFile.insertEmptyBlockAt(overflowAddr);
                overflowBlock = new LHBlock<>(this.overflowFile.getBlockingFactor(), this.overflowFile.getType());
                addresses.add(overflowAddr);
            }

            //to iste co s mainom
            while (!overflowBlock.isFull() && recordIndex < records.size()) {
                overflowBlock.getData()[overflowBlock.getValidCount()] = records.get(recordIndex);
                overflowBlock.setValidCount(overflowBlock.getValidCount() + 1);
                recordIndex++;
            }

            previousBlock.setNextBlockAddr(overflowAddr);
            if (previousBlock != mainBlock) {
                this.overflowFile.writeBlock(addresses.get(overflowCount -1), previousBlock);
            }
            previousBlock = overflowBlock;
            overflowCount++;
        }

        //zapisanie blokov
        previousBlock.setNextBlockAddr(-1);
        this.mainFile.writeBlock(bucketAddr, mainBlock);
        this.overflowFile.writeBlock(addresses.get(overflowCount -1), previousBlock);

        return overflowCount - startIndex;
    }

    private List<LHBlock<T>> collectBlocks(int bucketAddr) throws IOException {
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
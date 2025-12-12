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

    private static final double MAX_DENSITY = 0.75;

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
        double currentDensity = (double) this.recordCount /
                (double) (this.mainFile.getTotalBlockCount() + this.overflowFile.getTotalBlockCount()) /
                (double) this.mainFile.getBlockingFactor();
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

        // 1.a nacitanie blokov
        List<LHBlock<T>> oldBlocks = this.collectBlocks(oldBucketAddr);
        // 1.b nacitanie zaznamov z blokov
        List<T> records = new LinkedList<>();
        for (LHBlock<T> block : oldBlocks) {
            for (int i = 0; i < block.getValidCount(); i++) {
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

        int usedByOld = fillChain(recordsToOld, oldBucketAddr, overflowAddresses, 0);
        int usedByNew = fillChain(recordsToNew, newBucketAddr, overflowAddresses, usedByOld);
        if (usedByNew != 0) {
            throw new Exception("Chyba pri rozdeleni zaznamov do novych bucketov.");
        }
        for (int i = usedByOld + usedByNew; i < overflowAddresses.size(); i++) {
            this.overflowFile.insertEmptyBlockAt(overflowAddresses.get(i));
        }
        //odstranenie prazdnych blokov v overflow file
        this.overflowFile.cutEmptyFileBlocks();
    }

    private int fillChain(
            List<T> records,
            int bucketAddr,
            List<Integer> addresses,
            int startIndex
    ) throws IOException {
        //naplnenie hlavneho bloku
        LHBlock<T> newMainBlock = new LHBlock<>(this.mainFile.getBlockingFactor(), this.mainFile.getType());
        int recordIndex = 0;

        //naplnam iba pokym nie je plny, ci aj nejaky dalsi check?
        while(!newMainBlock.isFull() && recordIndex < records.size()) {
            newMainBlock.getData()[newMainBlock.getValidCount()] = records.get(recordIndex);
            newMainBlock.setValidCount(newMainBlock.getValidCount() + 1);
            recordIndex++;
        }
        // overflow index, aby sme pouzili este dostupne adresy
        int overflowCount = startIndex;
        LHBlock<T> previousBlock = newMainBlock;

        //naplnenie overflow blokov
        while (recordIndex < records.size()){
            int overflowAddr;
            /// TODO: spocitat ci staci jeden overflow blok
            if (overflowCount < addresses.size()) {
                overflowAddr = addresses.get(overflowCount);
            } else {
                overflowAddr = this.overflowFile.getTotalBlockCount();
                this.overflowFile.insertEmptyBlockAt(overflowAddr);
                addresses.add(overflowAddr);
            }
            LHBlock<T> overflowBlock = new LHBlock<>(this.overflowFile.getBlockingFactor(), this.overflowFile.getType());

            //to iste co s mainom
            while (!overflowBlock.isFull() && recordIndex < records.size()) {
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

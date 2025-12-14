package S2.heapFile;

import S1.trees.avl.AVLTree;
import S2.linearHashing.LHBlock;

import java.io.*;

public class HeapFile<T extends IRecord> {
    private final RandomAccessFile file;
    private final RandomAccessFile metadata;
    private final IData<T> type;
    private final int blockSize;
    private final int blockingFactor;
    private final AVLTree<Integer> partiallyFreeBlocks;
    private final AVLTree<Integer> fullyEmptyBlocks;
    private final boolean useLHBlock;
    private int totalBlocks;

    public HeapFile(RandomAccessFile file, IData<T> type, int recordSize, int blockSize,
                    String metadataPath, boolean useMainHeader) throws IOException {
        this.file = file;
        this.type = type;
        this.blockSize = blockSize;
        this.useLHBlock = useMainHeader;
        this.partiallyFreeBlocks = new AVLTree<>();
        this.fullyEmptyBlocks = new AVLTree<>();

        int headerSize = useMainHeader ? 8 : 4;
        this.blockingFactor = (blockSize - headerSize) / recordSize;

        if (blockingFactor <= 0) {
            throw new IllegalArgumentException("Block size is too small to hold any record!");
        }
        this.totalBlocks = 0;

        File metaFile = new File(metadataPath);
        if (!metaFile.exists()) {
            metaFile.createNewFile();
        }
        this.metadata = new RandomAccessFile(metadataPath, "rw");
        this.loadMetadata();
    }

    //kvoli spatnej kompatibilite
    public HeapFile(RandomAccessFile file, IData<T> type, int recordSize, int blockSize,
                    String metadataPath) throws IOException {
        this(file, type, recordSize, blockSize, metadataPath, false);
    }

    private Block<T> createNewBlock() {
        return this.useLHBlock
                ? new LHBlock<>(this.blockingFactor, this.type)
                : new Block<>(this.blockingFactor, this.type);
    }

    public int insert(T record) throws IOException {
        if (record == null) return -1;

        Block<T> block = null;
        int address = -1;

        if (!this.useLHBlock) {
            while (this.partiallyFreeBlocks.getNodeCount() > 0) {
                address = this.partiallyFreeBlocks.findMin(this.partiallyFreeBlocks.getRoot());
                block = readBlock(address);

                if (block.isFull()) {
                    this.partiallyFreeBlocks.delete(address);
                    block = null;
                    address = -1;
                    continue;
                }
                break;
            }
        }
        if (block == null) {
            if (!this.useLHBlock) {
                if (this.fullyEmptyBlocks.getNodeCount() > 0) {
                    address = this.fullyEmptyBlocks.findMin(this.fullyEmptyBlocks.getRoot());
                    this.fullyEmptyBlocks.delete(address);
                    block = this.createNewBlock();
                    this.partiallyFreeBlocks.insert(address);
                } else {
                    address = this.totalBlocks++;
                    block = this.createNewBlock();
                    this.partiallyFreeBlocks.insert(address);
                }
            } else {
                if (this.fullyEmptyBlocks.getNodeCount() > 0) {
                    address = this.fullyEmptyBlocks.findMin(this.fullyEmptyBlocks.getRoot());
                    this.fullyEmptyBlocks.delete(address);
                    block = this.createNewBlock();
                } else {
                    address = this.totalBlocks++;
                    block = this.createNewBlock();
                }
            }
        }

        block.getData()[block.getValidCount()] = record;
        block.setValidCount(block.getValidCount() + 1);
        if (this.useLHBlock) {
            ((LHBlock<T>) block).setNextBlockAddr(-1);
        }

        if (block.isFull() && !this.useLHBlock) {
            this.partiallyFreeBlocks.delete(address);
        }

        writeBlock(address, block);
        return address;
    }


    public boolean insertEmptyBlockAt(int address) throws IOException {
        if (address < 0 || address > this.totalBlocks) {
            return false;
        }
        //pouziva sa iba v linearnom hashovani, preto pouzivame rovno LHBlock
        LHBlock<T> block = (LHBlock<T>)this.createNewBlock();
        writeBlock(address, block);

        if (address == this.totalBlocks) {
            this.totalBlocks++;
        }
        return true;
    }


    public T get(int address, T data) throws IOException {
        if (address < 0 || address >= this.totalBlocks) {
            return null;
        }

        Block<T> block = readBlock(address);
        T[] records = block.getData();

        for (int i = 0; i < block.getValidCount(); i++) {
            if (records[i].equals(data)) {
                return records[i];
            }
        }
        return null;
    }

    public int getTotalBlockCount() {
        return this.totalBlocks;
    }

    public IData<T> getType() {
        return this.type;
    }


    public void delete(int address, T data) throws IOException {
        if (address < 0 || address >= totalBlocks) {
            throw new IndexOutOfBoundsException(); // vie tu byt return, ale kvoli testeru aby mi to padlo pri chybe
        }
        Block<T> block = readBlock(address);
        T[] records = block.getData();
        int validCount = block.getValidCount();

        for (int i = 0; i < validCount; i++) {
            if (records[i].equals(data)) {
                T temp = records[i];
                records[i] = records[validCount - 1];
                records[validCount - 1] = temp;

                block.setValidCount(validCount - 1);

                boolean wasFull = validCount == this.blockingFactor;
                boolean nowEmpty = validCount == 1;

                if (wasFull && !nowEmpty) {
                    this.partiallyFreeBlocks.insert(address);
                }

                if (nowEmpty) {
                    if (!wasFull) {
                        this.partiallyFreeBlocks.delete(address);
                    }
                    this.fullyEmptyBlocks.insert(address);
                    cutEmptyFileBlocks();
                    if (address < this.totalBlocks) {
                        writeBlock(address, block);
                    }
                } else {
                    writeBlock(address, block);
                }
                return;
            }
        }
    }

    public void cutEmptyFileBlocks() throws IOException {
        while (this.totalBlocks > 0) {
            Integer maxEmpty = this.fullyEmptyBlocks.findMax(this.fullyEmptyBlocks.getRoot());
            if (maxEmpty != null && maxEmpty == this.totalBlocks - 1) {
                this.fullyEmptyBlocks.delete(this.totalBlocks - 1);
                this.totalBlocks--;
            } else {
                break;
            }
        }
        this.file.setLength((long) this.totalBlocks * this.blockSize);
    }

    public void seek(int address) throws IOException {
        this.file.seek((long) address * this.blockSize);
    }

    public void close() throws IOException {
        saveMetadata();
        this.metadata.close();
        this.file.close();
    }

    public Block<T> readBlock(int address) throws IOException {
        if (((long) address * this.blockSize) >= this.file.length()) {
            return this.createNewBlock();
        }

        seek(address);
        byte[] bytes = new byte[this.blockSize];
        this.file.readFully(bytes);

        Block<T> block = this.createNewBlock();
        block.fromBytes(bytes);
        return block;
    }

    public void writeBlock(int blockAddress, Block<T> block) throws IOException {
        seek(blockAddress);
        byte[] blockData = block.getBytes();

        byte[] paddedBlock = new byte[this.blockSize];
        System.arraycopy(blockData, 0, paddedBlock, 0, Math.min(blockData.length, this.blockSize));

        this.file.write(paddedBlock);
    }


    private void saveMetadata() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeInt(this.totalBlocks);

            dos.writeInt(this.partiallyFreeBlocks.getNodeCount());
            for (int addr : this.partiallyFreeBlocks.inOrder(false)) {
                dos.writeInt(addr);
            }

            dos.writeInt(this.fullyEmptyBlocks.getNodeCount());
            for (int addr : this.fullyEmptyBlocks.inOrder(false)) {
                dos.writeInt(addr);
            }

            dos.flush();
            byte[] metadataBytes = baos.toByteArray();

            this.metadata.seek(0);
            this.metadata.write(metadataBytes);
            this.metadata.setLength(metadataBytes.length);

        }
    }

    private void loadMetadata() throws IOException {
        if (this.metadata.length() == 0) {
            this.totalBlocks = 0;
            return;
        }
        this.metadata.seek(0);

        byte[] metadataBytes = new byte[(int) this.metadata.length()];
        this.metadata.readFully(metadataBytes);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(metadataBytes);
             DataInputStream dis = new DataInputStream(bais)) {

            this.totalBlocks = dis.readInt();

            int partialCount = dis.readInt();
            for (int i = 0; i < partialCount; i++) {
                this.partiallyFreeBlocks.insert(dis.readInt());
            }

            int emptyCount = dis.readInt();
            for (int i = 0; i < emptyCount; i++) {
                this.fullyEmptyBlocks.insert(dis.readInt());
            }


            long actualSize = this.file.length();
            long expectedSize = (long) this.totalBlocks * this.blockSize;
            if (actualSize < expectedSize) {
                this.totalBlocks = 0;
                this.partiallyFreeBlocks.clear();
                this.fullyEmptyBlocks.clear();
                this.file.setLength(0);
            }
        }
    }

    public int getBlockingFactor() {
        return this.blockingFactor;
    }
}
package binary;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HeapFile<T extends IRecord> {
    private final RandomAccessFile file;
    private final IData<T> type;
    private final int blockSize;
    private final int blockingFactor;
    private final String metadadaPath;
    private final List<Integer> partiallyFreeBlocks;
    private final List<Integer> fullyEmptyBlocks;
    private int totalBlocks;

    public HeapFile(RandomAccessFile file, IData<T> type, int recordSize, int blockSize, String metadadaPath) throws IOException {
        this.file = file;
        this.type = type;
        this.blockSize = blockSize;
        this.metadadaPath = metadadaPath;
        this.partiallyFreeBlocks = new ArrayList<>();
        this.fullyEmptyBlocks = new ArrayList<>();

        this.blockingFactor = (blockSize - 4) / recordSize;

        if (blockingFactor <= 0) {
            throw new IllegalArgumentException("Block size is too small to hold any record!");
        }
        this.totalBlocks = 0;

        this.loadMetadata();
    }

    public int insert(T record) throws IOException {
        if (record == null) return -1;

        Block<T> block;
        int address;

        while (true) {
            if (!this.partiallyFreeBlocks.isEmpty()) {
                address = this.partiallyFreeBlocks.getFirst();
                block = readBlock(address);

                if (block.isFull()) {
                    this.partiallyFreeBlocks.removeFirst();
                    continue;
                }
            } else if (!this.fullyEmptyBlocks.isEmpty()) {
                address = this.fullyEmptyBlocks.removeFirst();
                block = new Block<>(this.blockingFactor, this.type);
                this.partiallyFreeBlocks.add(address);
            } else {
                address = this.totalBlocks++;
                block = new Block<>(this.blockingFactor, this.type);
                this.partiallyFreeBlocks.add(address);
            }
            break;
        }

        block.getData()[block.getValidCount()] = record;
        block.setValidCount(block.getValidCount() + 1);

        if (block.isFull()) {
            this.partiallyFreeBlocks.remove(Integer.valueOf(address));
        }

        writeBlock(address, block);
        return address;
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

    public void delete(int address, T data) throws IOException {
        if (address < 0 || address >= totalBlocks) {
            throw new IllegalArgumentException();
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

                if (wasFull) {
                    this.partiallyFreeBlocks.add(address);
                }

                if (nowEmpty) {
                    this.partiallyFreeBlocks.remove(Integer.valueOf(address));
                    this.fullyEmptyBlocks.add(address);

                    cutEmptyFileBlocks();
                }

                writeBlock(address, block);
            }
        }
    }

    private void cutEmptyFileBlocks() throws IOException {
        while (this.totalBlocks > 0 && this.fullyEmptyBlocks.contains(this.totalBlocks - 1)) {
            this.fullyEmptyBlocks.remove(Integer.valueOf(this.totalBlocks - 1));
            this.totalBlocks--;
        }
        this.file.setLength((long) this.totalBlocks * this.blockSize);
    }


    public void seek(int address) throws IOException {
        this.file.seek((long) address * this.blockSize);
    }

    public void close() throws IOException {
        saveMetadata();
        file.close();
    }

    public void print() throws IOException {
        System.out.println("=== Heap File Contents ===");
        System.out.println("Total blocks: " + this.totalBlocks);
        System.out.println("Blocking factor: " + this.blockingFactor);
        System.out.println("Partially free blocks: " + this.partiallyFreeBlocks);
        System.out.println("Fully empty blocks: " + this.fullyEmptyBlocks);
        System.out.println();

        for (int i = 0; i < this.totalBlocks; i++) {
            Block<T> block = readBlock(i);
            System.out.println("Block " + i + " (Valid: " + block.getValidCount() + "/" + this.blockingFactor + "):");

            T[] records = block.getData();
            for (int j = 0; j < this.blockingFactor; j++) {
                String status = j < block.getValidCount() ? "VALID" : "INVALID";
                System.out.println("  [" + j + "] " + status + ": " + records[j]);
            }
            System.out.println();
        }
    }

    private Block<T> readBlock(int address) throws IOException {
        seek(address);
        byte[] bytes = new byte[this.blockSize];
        this.file.readFully(bytes);

        Block<T> block = new Block<>(this.blockingFactor, this.type);
        block.fromBytes(bytes);
        return block;
    }

    private void writeBlock(int blockAddress, Block<T> block) throws IOException {
        seek(blockAddress);
        this.file.write(block.getBytes());
    }

    private void saveMetadata() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.metadadaPath))) {
            oos.writeInt(this.totalBlocks);
            oos.writeInt(this.partiallyFreeBlocks.size());
            for (int addr : this.partiallyFreeBlocks) {
                oos.writeInt(addr);
            }
            oos.writeInt(this.fullyEmptyBlocks.size());
            for (int addr : this.fullyEmptyBlocks) {
                oos.writeInt(addr);
            }
        }
    }

    private void loadMetadata() throws IOException {
        File metaFile = new File(this.metadadaPath);
        if (!metaFile.exists()) {
            this.totalBlocks = (int) (this.file.length() / this.blockSize);
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metaFile))) {
            this.totalBlocks = ois.readInt();

            int partialCount = ois.readInt();
            for (int i = 0; i < partialCount; i++) {
                this.partiallyFreeBlocks.add(ois.readInt());
            }

            int emptyCount = ois.readInt();
            for (int i = 0; i < emptyCount; i++) {
                this.fullyEmptyBlocks.add(ois.readInt());
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
}

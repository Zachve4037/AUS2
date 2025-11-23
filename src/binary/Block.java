package binary;

import java.nio.ByteBuffer;

public class Block<T extends IRecord> {
    private int validCount;
    private final int capacity;
    private T[] data;

    Block(int capacity, IData<T> type) {
        this.validCount = 0;
        this.capacity = capacity;
        this.data = (T[]) new IRecord[capacity];

        for (int i = 0; i < capacity; i++) {
            data[i] = type.createClass();
        }
    }

    public int getValidCount() {
        return this.validCount;
    }

    public void setValidCount(int count) {
        this.validCount = count;
    }

    public T[] getData() {
        return this.data;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public boolean isFull() {
        return this.validCount >= capacity;
    }

    public boolean isEmpty() {
        return this.validCount == 0;
    }

    public int getSize() {
        return 4 + (capacity * data[0].getSize());
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getSize());
        buffer.putInt(validCount);
        for (int i = 0; i < capacity; i++) {
            buffer.put(data[i].getBytes());
        }
        return buffer.array();
    }

    public void fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        validCount = buffer.getInt();
        for (int i = 0; i < capacity; i++) {
            byte[] recordBytes = new byte[data[i].getSize()];
            buffer.get(recordBytes);
            data[i].fromBytes(recordBytes);
        }
    }
}

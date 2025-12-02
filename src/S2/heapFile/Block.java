package S2.heapFile;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

public class Block<T extends IRecord> {
    private int validCount;
    private final int capacity;
    private T[] data;
    private final IData<T> type;

    public Block(int capacity, IData<T> type) {
        this.type = type;
        this.validCount = 0;
        this.capacity = capacity;
        T temp = this.type.createClass();
        this.data = (T[]) Array.newInstance(temp.getClass(), capacity);

        for (int i = 0; i < capacity; i++) {
            this.data[i] = type.createClass();
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

    public boolean isFull() {
        return this.validCount >= this.capacity;
    }

    public IData<T> getType() {
        return this.type;
    }

    public boolean isEmpty() {
        return this.validCount == 0;
    }

    public int getSize() {
        return 4 + (this.capacity * this.data[0].getSize());
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getSize());
        buffer.putInt(this.validCount);

        for (int i = 0; i < this.validCount; i++) {
            buffer.put(this.data[i].getBytes());
        }

        int recordSize = this.data[0].getSize();
        for (int i = this.validCount; i < this.capacity; i++) {
            buffer.put(new byte[recordSize]);
        }

        return buffer.array();
    }


    public void fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        this.validCount = buffer.getInt();

        for (int i = 0; i < this.capacity; i++) {
            byte[] recordBytes = new byte[this.data[i].getSize()];
            buffer.get(recordBytes);
            this.data[i].fromBytes(recordBytes);
        }
    }

    protected int getCapacity() {
        return this.capacity;
    }
}

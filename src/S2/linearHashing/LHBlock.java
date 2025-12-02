package S2.linearHashing;

import S2.heapFile.Block;
import S2.heapFile.IData;
import S2.heapFile.IRecord;

import java.nio.ByteBuffer;

public class LHBlock<T extends IRecord> extends Block<T> {
    private int nextBlockAddr;

    public LHBlock(int capacity, IData<T> type) {
        super(capacity, type);
        this.nextBlockAddr = -1;
    }

    public int getNextBlockAddr() {
        return nextBlockAddr;
    }

    public void setNextBlockAddr(int nextBlockAddr) {
        this.nextBlockAddr = nextBlockAddr;
    }

    @Override
    public int getSize() {
        return 8 + (this.getData()[0].getSize() * getData().length);
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getSize());
        buffer.putInt(this.getValidCount());
        buffer.putInt(this.nextBlockAddr);

        for (int i = 0; i < this.getValidCount(); i++) {
            buffer.put(this.getData()[i].getBytes());
        }

        int recordSize = this.getData()[0].getSize();
        for (int i = this.getValidCount(); i < this.getCapacity(); i++) {
            buffer.put(new byte[recordSize]);
        }

        return buffer.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        this.setValidCount(buffer.getInt());
        this.nextBlockAddr = buffer.getInt();

        for (int i = 0; i < this.getCapacity(); i++) {
            byte[] recordBytes = new byte[this.getData()[i].getSize()];
            buffer.get(recordBytes);
            this.getData()[i].fromBytes(recordBytes);
        }
    }
}
